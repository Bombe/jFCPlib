package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.GetConfig;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link GetConfigCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetConfigCommandImpl implements GetConfigCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicBoolean withCurrent = new AtomicBoolean();
	private final AtomicBoolean withDefaults = new AtomicBoolean();

	public GetConfigCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public GetConfigCommand withCurrent() {
		withCurrent.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withDefaults() {
		withDefaults.set(true);
		return this;
	}

	@Override
	public ListenableFuture<ConfigData> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private ConfigData executeDialog() throws IOException, ExecutionException, InterruptedException {
		GetConfig getConfig = new GetConfig(new RandomIdentifierGenerator().generate());
		getConfig.setWithCurrent(withCurrent.get());
		getConfig.setWithDefaults(withDefaults.get());
		try (GetConfigDialog getConfigDialog = new GetConfigDialog()) {
			return getConfigDialog.send(getConfig).get();
		}
	}

	private class GetConfigDialog extends FcpDialog<ConfigData> {

		private final AtomicReference<ConfigData> configData = new AtomicReference<>();

		public GetConfigDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return configData.get() != null;
		}

		@Override
		protected ConfigData getResult() {
			return configData.get();
		}

		@Override
		protected void consumeConfigData(ConfigData configData) {
			this.configData.set(configData);
		}

	}

}
