package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.ModifyConfig;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ModifyConfigCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ModifyConfigCommandImpl implements ModifyConfigCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final ModifyConfig modifyConfig;

	public ModifyConfigCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		modifyConfig = new ModifyConfig(identifierGenerator.get());
	}

	@Override
	public WithValue set(String key) {
		return value -> to(key, value);
	}

	private ModifyConfigCommand to(String key, String value) {
		modifyConfig.setOption(key, value);
		return this;
	}

	@Override
	public ListenableFuture<ConfigData> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private ConfigData executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (ModifyConfigDialog modifyConfigDialog = new ModifyConfigDialog()) {
			return modifyConfigDialog.send(modifyConfig).get();
		}
	}

	private class ModifyConfigDialog extends FcpDialog<ConfigData> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<ConfigData> configData = new AtomicReference<>();

		public ModifyConfigDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected ConfigData getResult() {
			return configData.get();
		}

		@Override
		protected void consumeConfigData(ConfigData configData) {
			this.configData.set(configData);
			finished.set(true);
		}

	}

}
