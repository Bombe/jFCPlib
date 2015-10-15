package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import net.pterodactylus.fcp.FcpKeyPair;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.SSKKeypair;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Implementation of the {@link GenerateKeypairCommand}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class GenerateKeypairCommandImpl implements GenerateKeypairCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;

	GenerateKeypairCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public ListenableFuture<FcpKeyPair> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private FcpKeyPair executeDialog() throws InterruptedException, ExecutionException, IOException {
		try (FcpKeyPairDialog fcpKeyPairDialog = new FcpKeyPairDialog()) {
			return fcpKeyPairDialog.send(new GenerateSSK()).get();
		}
	}

	private class FcpKeyPairDialog extends FcpDialog<FcpKeyPair> {

		public FcpKeyPairDialog() throws IOException {
			super(GenerateKeypairCommandImpl.this.threadPool, GenerateKeypairCommandImpl.this.connectionSupplier.get(), null);
		}

		@Override
		protected void consumeSSKKeypair(SSKKeypair sskKeypair) {
			setResult(new FcpKeyPair(sskKeypair.getRequestURI(), sskKeypair.getInsertURI()));
		}

	}

}
