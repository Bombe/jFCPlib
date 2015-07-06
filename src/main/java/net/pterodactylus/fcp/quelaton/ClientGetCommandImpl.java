package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.ClientGet;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.FcpUtils.TempInputStream;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.ReturnType;

/**
 * Implementation of the {@link ClientGetCommand}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class ClientGetCommandImpl implements ClientGetCommand {

	private final ExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;

	private boolean ignoreDataStore;
	private boolean dataStoreOnly;
	private Long maxSize;
	private Priority priority;
	private boolean realTime;
	private boolean global;

	public ClientGetCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = threadPool;
		this.connectionSupplier = connectionSupplier;
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
		ClientGet clientGet = createClientGetCommand(uri);
		return threadPool.submit(() -> new ClientGetReplySequence().send(clientGet).get());
	}

	private ClientGet createClientGetCommand(String uri) {
		String identifier = new RandomIdentifierGenerator().generate();
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
		return clientGet;
	}

	private class ClientGetReplySequence extends FcpReplySequence<Optional<Data>> {

		private final AtomicReference<String> identifier = new AtomicReference<>();
		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicBoolean failed = new AtomicBoolean();

		private String contentType;
		private long dataLength;
		private InputStream payload;

		public ClientGetReplySequence() throws IOException {
			super(ClientGetCommandImpl.this.threadPool, ClientGetCommandImpl.this.connectionSupplier.get());
		}

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
			if (allData.getIdentifier().equals(identifier.get())) {
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
			if (getFailed.getIdentifier().equals(identifier.get())) {
				failed.set(true);
			}
		}

		@Override
		protected void consumeConnectionClosed(Throwable throwable) {
			failed.set(true);
		}

		@Override
		public Future<Optional<Data>> send(FcpMessage fcpMessage) throws IOException {
			identifier.set(fcpMessage.getField("Identifier"));
			return super.send(fcpMessage);
		}

	}

}
