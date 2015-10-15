package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

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
	private final Supplier<String> identifierGenerator;
	private final AtomicBoolean withCurrent = new AtomicBoolean();
	private final AtomicBoolean withDefaults = new AtomicBoolean();
	private final AtomicBoolean withSortOrder = new AtomicBoolean();
	private final AtomicBoolean withExpertFlag = new AtomicBoolean();
	private final AtomicBoolean withForceWriteFlag = new AtomicBoolean();
	private final AtomicBoolean withShortDescription = new AtomicBoolean();
	private final AtomicBoolean withLongDescription = new AtomicBoolean();
	private final AtomicBoolean withDataTypes = new AtomicBoolean();

	public GetConfigCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
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
	public GetConfigCommand withSortOrder() {
		withSortOrder.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withExpertFlag() {
		withExpertFlag.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withForceWriteFlag() {
		withForceWriteFlag.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withShortDescription() {
		withShortDescription.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withLongDescription() {
		withLongDescription.set(true);
		return this;
	}

	@Override
	public GetConfigCommand withDataTypes() {
		withDataTypes.set(true);
		return this;
	}

	@Override
	public ListenableFuture<ConfigData> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private ConfigData executeDialog() throws IOException, ExecutionException, InterruptedException {
		GetConfig getConfig = new GetConfig(identifierGenerator.get());
		getConfig.setWithCurrent(withCurrent.get());
		getConfig.setWithDefaults(withDefaults.get());
		getConfig.setWithSortOrder(withSortOrder.get());
		getConfig.setWithExpertFlag(withExpertFlag.get());
		getConfig.setWithForceWriteFlag(withForceWriteFlag.get());
		getConfig.setWithShortDescription(withShortDescription.get());
		getConfig.setWithLongDescription(withLongDescription.get());
		getConfig.setWithDataTypes(withDataTypes.get());
		try (GetConfigDialog getConfigDialog = new GetConfigDialog()) {
			return getConfigDialog.send(getConfig).get();
		}
	}

	private class GetConfigDialog extends FcpDialog<ConfigData> {

		public GetConfigDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), null);
		}

		@Override
		protected void consumeConfigData(ConfigData configData) {
			setResult(configData);
		}

	}

}
