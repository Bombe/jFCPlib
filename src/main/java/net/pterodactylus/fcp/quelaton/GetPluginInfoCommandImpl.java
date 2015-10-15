package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.pterodactylus.fcp.GetPluginInfo;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link GetPluginInfoCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class GetPluginInfoCommandImpl implements GetPluginInfoCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final GetPluginInfo getPluginInfo;

	public GetPluginInfoCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		getPluginInfo = new GetPluginInfo(identifierGenerator.get());
	}

	@Override
	public GetPluginInfoCommand detailed() {
		getPluginInfo.setDetailed(true);
		return this;
	}

	@Override
	public Executable<Optional<PluginInfo>> plugin(String pluginName) {
		getPluginInfo.setPluginName(pluginName);
		return this::execute;
	}

	private ListenableFuture<Optional<PluginInfo>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<PluginInfo> executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (GetPluginInfoDialog getPluginInfoDialog = new GetPluginInfoDialog()) {
			return getPluginInfoDialog.send(getPluginInfo).get();
		}
	}

	private class GetPluginInfoDialog extends FcpDialog<Optional<PluginInfo>> {

		public GetPluginInfoDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Optional.<PluginInfo>empty());
		}

		@Override
		protected void consumePluginInfo(PluginInfo pluginInfo) {
			setResult(Optional.ofNullable(pluginInfo));
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finish();
		}

	}

}
