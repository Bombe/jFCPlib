package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import net.pterodactylus.fcp.UnsubscribeUSK;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link UnsubscribeUskCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class UnsubscribeUskCommandImpl implements UnsubscribeUskCommand {

	private static final RandomIdentifierGenerator IDENTIFIER = new RandomIdentifierGenerator();
	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final UnsubscribeUSK unsubscribeUSK = new UnsubscribeUSK(IDENTIFIER.generate());

	public UnsubscribeUskCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public Executable<Void> identifier(String identifier) {
		return this::execute;
	}

	private ListenableFuture<Void> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Void executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (UnsubscribeUskDialog unsubscribeUskDialog = new UnsubscribeUskDialog()) {
			return unsubscribeUskDialog.send(unsubscribeUSK).get();
		}
	}

	private class UnsubscribeUskDialog extends FcpDialog<Void> {

		public UnsubscribeUskDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return true;
		}

	}

}
