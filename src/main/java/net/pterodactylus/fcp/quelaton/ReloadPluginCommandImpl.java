package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.ReloadPlugin;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ReloadPluginCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ReloadPluginCommandImpl implements ReloadPluginCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final ReloadPlugin reloadPlugin;

	public ReloadPluginCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		reloadPlugin = new ReloadPlugin(identifierGenerator.get());
	}

	@Override
	public ReloadPluginCommand waitFor(int milliseconds) {
		reloadPlugin.setMaxWaitTime(milliseconds);
		return this;
	}

	@Override
	public ReloadPluginCommand purge() {
		reloadPlugin.setPurge(true);
		return this;
	}

	@Override
	public ReloadPluginCommand addToConfig() {
		reloadPlugin.setStore(true);
		return this;
	}

	@Override
	public Executable<Optional<PluginInfo>> plugin(String pluginClassName) {
		reloadPlugin.setPluginName(pluginClassName);
		return this::execute;
	}

	private ListenableFuture<Optional<PluginInfo>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<PluginInfo> executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (ReloadPluginDialog reloadPluginDialog = new ReloadPluginDialog()) {
			return reloadPluginDialog.send(reloadPlugin).get();
		}
	}

	private class ReloadPluginDialog extends FcpDialog<Optional<PluginInfo>> {

		public ReloadPluginDialog() throws IOException {
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
