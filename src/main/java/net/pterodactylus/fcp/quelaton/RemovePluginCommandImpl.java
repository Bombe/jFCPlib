package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import net.pterodactylus.fcp.PluginRemoved;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.RemovePlugin;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link RemovePluginCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class RemovePluginCommandImpl implements RemovePluginCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final RemovePlugin removePlugin;

	public RemovePluginCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		removePlugin = new RemovePlugin(identifierGenerator.get());
	}

	@Override
	public RemovePluginCommand waitFor(int milliseconds) {
		removePlugin.setMaxWaitTime(milliseconds);
		return this;
	}

	@Override
	public RemovePluginCommand purge() {
		removePlugin.setPurge(true);
		return this;
	}

	@Override
	public Executable<Boolean> plugin(String pluginClass) {
		removePlugin.setPluginName(pluginClass);
		return this::execute;
	}

	private ListenableFuture<Boolean> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private boolean executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (RemovePluginDialog removePluginDialog = new RemovePluginDialog()) {
			return removePluginDialog.send(removePlugin).get();
		}
	}

	private class RemovePluginDialog extends FcpDialog<Boolean> {

		public RemovePluginDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), false);
		}

		@Override
		protected void consumePluginRemoved(PluginRemoved pluginRemoved) {
			setResult(true);
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finish();
		}

	}

}
