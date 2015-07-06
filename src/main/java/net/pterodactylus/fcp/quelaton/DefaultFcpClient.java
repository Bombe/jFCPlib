package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
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

	private FcpConnection connect() throws IOException {
		FcpConnection fcpConnection = this.fcpConnection.get();
		if (fcpConnection != null) {
			return fcpConnection;
		}
		fcpConnection = createConnection();
		this.fcpConnection.compareAndSet(null, fcpConnection);
		return fcpConnection;
	}

	private FcpConnection createConnection() throws IOException {
		FcpConnection connection = new FcpConnection(hostname, port);
		connection.connect();
		FcpReplySequence<?> nodeHelloSequence = new FcpReplySequence<Void>(threadPool, connection) {
			private final AtomicReference<NodeHello> receivedNodeHello = new AtomicReference<>();
			private final AtomicBoolean receivedClosed = new AtomicBoolean();
			@Override
			protected boolean isFinished() {
				return receivedNodeHello.get() != null || receivedClosed.get();
			}

			@Override
			protected void consumeNodeHello(NodeHello nodeHello) {
				receivedNodeHello.set(nodeHello);
			}

			@Override
			protected void consumeCloseConnectionDuplicateClientName(
				CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
				receivedClosed.set(true);
			}
		};
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
				return new FcpReplySequence<FcpKeyPair>(threadPool, connect()) {
					private AtomicReference<FcpKeyPair> keyPair = new AtomicReference<>();

					@Override
					protected boolean isFinished() {
						return keyPair.get() != null;
					}

					@Override
					protected FcpKeyPair getResult() {
						return keyPair.get();
					}

					@Override
					protected void consumeSSKKeypair(SSKKeypair sskKeypair) {
						keyPair.set(new FcpKeyPair(sskKeypair.getRequestURI(), sskKeypair.getInsertURI()));
					}
				}.send(new GenerateSSK()).get();
			});
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
			return threadPool.submit(() -> {
				FcpReplySequence<Optional<Data>> replySequence = new FcpReplySequence<Optional<Data>>(threadPool, connect()) {
					private final AtomicBoolean finished = new AtomicBoolean();
					private final AtomicBoolean failed = new AtomicBoolean();

					private final String identifier = ClientGetCommandImpl.this.identifier;

					private String contentType;
					private long dataLength;
					private InputStream payload;

					@Override
					protected boolean isFinished() {
						return finished.get() || failed.get();
					}

					@Override
					protected Optional<Data> getResult() {
						return failed.get() ? Optional.empty() : Optional.of(new Data() {
							@Override
							public String getMimeType() {
								return contentType;
							}

							@Override
							public long size() {
								return dataLength;
							}

							@Override
							public InputStream getInputStream() {
								return payload;
							}
						});
					}

					@Override
					protected void consumeAllData(AllData allData) {
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

					@Override
					protected void consumeGetFailed(GetFailed getFailed) {
						if (getFailed.getIdentifier().equals(identifier)) {
							failed.set(true);
						}
					}

					@Override
					protected void consumeConnectionClosed(Throwable throwable) {
						failed.set(true);
					}
				};
				return replySequence.send(clientGet).get();
			});
		}

	}

}

