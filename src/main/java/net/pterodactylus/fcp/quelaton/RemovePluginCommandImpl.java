package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

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

	private static final RandomIdentifierGenerator IDENTIFIER = new RandomIdentifierGenerator();
	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final RemovePlugin removePlugin = new RemovePlugin(IDENTIFIER.generate());

	public RemovePluginCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
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

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicBoolean pluginRemoved = new AtomicBoolean();

		public RemovePluginDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Boolean getResult() {
			return pluginRemoved.get();
		}

		@Override
		protected void consumePluginRemoved(PluginRemoved pluginRemoved) {
			this.pluginRemoved.set(true);
			finished.set(true);
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finished.set(true);
		}

	}

}
