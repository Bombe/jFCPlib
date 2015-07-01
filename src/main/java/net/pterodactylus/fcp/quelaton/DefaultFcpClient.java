package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.ClientGet;
import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpKeyPair;
import net.pterodactylus.fcp.FcpUtils.TempInputStream;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.ReturnType;
import net.pterodactylus.fcp.SSKKeypair;

import com.google.common.io.ByteStreams;

/**
 * Default {@link FcpClient} implementation.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class DefaultFcpClient implements FcpClient {

	private final ExecutorService threadPool;
	private final String hostname;
	private final int port;
	private final AtomicReference<FcpConnection> fcpConnection = new AtomicReference<>();
	private final Supplier<String> clientName;
	private final Supplier<String> expectedVersion;

	public DefaultFcpClient(ExecutorService threadPool, String hostname, int port, Supplier<String> clientName,
			Supplier<String> expectedVersion) {
		this.threadPool = threadPool;
		this.hostname = hostname;
		this.port = port;
		this.clientName = clientName;
		this.expectedVersion = expectedVersion;
	}

	private void connect() throws IOException {
		if (fcpConnection.get() != null) {
			return;
		}
		fcpConnection.compareAndSet(null, createConnection());
	}

	private FcpConnection createConnection() throws IOException {
		FcpConnection connection = new FcpConnection(hostname, port);
		connection.connect();
		AtomicReference<NodeHello> receivedNodeHello = new AtomicReference<>();
		AtomicBoolean receivedClosed = new AtomicBoolean();
		FcpReplySequence nodeHelloSequence = new FcpReplySequence(threadPool, connection);
		nodeHelloSequence
				.handle(NodeHello.class)
				.with((nodeHello) -> receivedNodeHello.set(nodeHello));
		nodeHelloSequence
				.handle(CloseConnectionDuplicateClientName.class)
				.with((closeConnection) -> receivedClosed.set(true));
		nodeHelloSequence.waitFor(() -> receivedNodeHello.get() != null || receivedClosed.get());
		ClientHello clientHello = new ClientHello(clientName.get(), expectedVersion.get());
		try {
			nodeHelloSequence.send(clientHello).get();
		} catch (InterruptedException | ExecutionException e) {
			connection.close();
			throw new IOException(String.format("Could not connect to %s:%d.", hostname, port), e);
		}
		return connection;
	}

	@Override
	public GenerateKeypairCommand generateKeypair() {
		return new GenerateKeypairCommandImpl();
	}

	private class GenerateKeypairCommandImpl implements GenerateKeypairCommand {

		@Override
		public Future<FcpKeyPair> execute() {
			return threadPool.submit(() -> {
				connect();
				Sequence sequence = new Sequence();
				FcpReplySequence replySequence = new FcpReplySequence(threadPool, fcpConnection.get());
				replySequence.handle(SSKKeypair.class).with(sequence::handleSSKKeypair);
				replySequence.waitFor(sequence::isFinished);
				replySequence.send(new GenerateSSK()).get();
				return sequence.getKeyPair();
			});
		}

		private class Sequence {

			private AtomicReference<FcpKeyPair> keyPair = new AtomicReference<>();

			public void handleSSKKeypair(SSKKeypair sskKeypair) {
				keyPair.set(new FcpKeyPair(sskKeypair.getRequestURI(), sskKeypair.getInsertURI()));
			}

			public boolean isFinished() {
				return keyPair.get() != null;
			}

			public FcpKeyPair getKeyPair() {
				return keyPair.get();
			}

		}

	}

	@Override
	public ClientGetCommand clientGet() {
		return new ClientGetCommandImpl();
	}

	private class ClientGetCommandImpl implements ClientGetCommand {

		private String identifier;
		private boolean ignoreDataStore;
		private boolean dataStoreOnly;
		private Long maxSize;
		private Priority priority;
		private boolean realTime;
		private boolean global;

		@Override
		public ClientGetCommand identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		@Override
		public ClientGetCommand ignoreDataStore() {
			ignoreDataStore = true;
			return this;
		}

		@Override
		public ClientGetCommand dataStoreOnly() {
			dataStoreOnly = true;
			return this;
		}

		@Override
		public ClientGetCommand maxSize(long maxSize) {
			this.maxSize = maxSize;
			return this;
		}

		@Override
		public ClientGetCommand priority(Priority priority) {
			this.priority = priority;
			return this;
		}

		@Override
		public ClientGetCommand realTime() {
			realTime = true;
			return this;
		}

		@Override
		public ClientGetCommand global() {
			global = true;
			return this;
		}

		@Override
		public Future<Optional<Data>> uri(String uri) {
			return threadPool.submit(new Callable<Optional<Data>>() {
				@Override
				public Optional<Data> call() throws Exception {
					DefaultFcpClient.this.connect();
					ClientGet clientGet = new ClientGet(uri, identifier, ReturnType.direct);
					if (ignoreDataStore) {
						clientGet.setIgnoreDataStore(true);
					}
					if (dataStoreOnly) {
						clientGet.setDataStoreOnly(true);
					}
					if (maxSize != null) {
						clientGet.setMaxSize(maxSize);
					}
					if (priority != null) {
						clientGet.setPriority(priority);
					}
					if (realTime) {
						clientGet.setRealTimeFlag(true);
					}
					if (global) {
						clientGet.setGlobal(true);
					}
					try (FcpReplySequence replySequence = new FcpReplySequence(threadPool, fcpConnection.get())) {
						Sequence sequence = new Sequence(identifier);
						replySequence.handle(AllData.class).with(sequence::allData);
						replySequence.handle(GetFailed.class).with(sequence::getFailed);
						replySequence.handleClose().with(sequence::disconnect);
						replySequence.waitFor(sequence::isFinished);
						replySequence.send(clientGet).get();
						return sequence.isSuccessful() ? Optional.of(sequence.getData()) : Optional.empty();
					}
				}
			});
		}

		private class Sequence {

			private final AtomicBoolean finished = new AtomicBoolean();
			private final AtomicBoolean failed = new AtomicBoolean();

			private final String identifier;

			private String contentType;
			private long dataLength;
			private InputStream payload;

			private Sequence(String identifier) {
				this.identifier = identifier;
			}

			public boolean isFinished() {
				return finished.get() || failed.get();
			}

			public boolean isSuccessful() {
				return !failed.get();
			}

			public Data getData() {
				return new Data() {
					@Override
					public String getMimeType() {
						synchronized (Sequence.this) {
							return contentType;
						}
					}

					@Override
					public long size() {
						synchronized (Sequence.this) {
							return dataLength;
						}
					}

					@Override
					public InputStream getInputStream() {
						synchronized (Sequence.this) {
							return payload;
						}
					}
				};
			}

			public void allData(AllData allData) {
				if (allData.getIdentifier().equals(identifier)) {
					synchronized (this) {
						contentType = allData.getContentType();
						dataLength = allData.getDataLength();
						try {
							payload = new TempInputStream(allData.getPayloadInputStream(), dataLength);
							finished.set(true);
						} catch (IOException e) {
							// TODO – logging
							failed.set(true);
						}
					}
				}
			}

			public void getFailed(GetFailed getFailed) {
				if (getFailed.getIdentifier().equals(identifier)) {
					failed.set(true);
				}
			}

			public void disconnect(Throwable t) {
				failed.set(true);
			}

		}

	}

}

