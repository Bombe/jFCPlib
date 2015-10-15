package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.ClientGet;
import net.pterodactylus.fcp.FcpUtils.TempInputStream;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.ReturnType;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Implementation of the {@link ClientGetCommand}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class ClientGetCommandImpl implements ClientGetCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;

	private boolean ignoreDataStore;
	private boolean dataStoreOnly;
	private Long maxSize;
	private Priority priority;
	private boolean realTime;
	private boolean global;

	public ClientGetCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
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
	public Executable<Optional<Data>> uri(String uri) {
		return () -> threadPool.submit(() -> execute(uri));
	}

	private Optional<Data> execute(String uri) throws InterruptedException, ExecutionException, IOException {
		ClientGet clientGet = createClientGetCommand(uri);
		try (ClientGetDialog clientGetDialog = new ClientGetDialog()) {
			return clientGetDialog.send(clientGet).get();
		}
	}

	private ClientGet createClientGetCommand(String uri) {
		String identifier = identifierGenerator.get();
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

	private class ClientGetDialog extends FcpDialog<Optional<Data>> {

		public ClientGetDialog() throws IOException {
			super(ClientGetCommandImpl.this.threadPool, ClientGetCommandImpl.this.connectionSupplier.get(), Optional.<Data>empty());
		}

		@Override
		protected void consumeAllData(AllData allData) {
			synchronized (this) {
				String contentType = allData.getContentType();
				long dataLength = allData.getDataLength();
				try {
					InputStream payload = new TempInputStream(allData.getPayloadInputStream(), dataLength);
					setResult(Optional.of(createData(contentType, dataLength, payload)));
				} catch (IOException e) {
					// TODO – logging
					finish();
				}
			}
		}

		private Data createData(String contentType, long dataLength, InputStream payload) {
			return new Data() {
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
			};
		}

		@Override
		protected void consumeGetFailed(GetFailed getFailed) {
			finish();
		}

	}

}
