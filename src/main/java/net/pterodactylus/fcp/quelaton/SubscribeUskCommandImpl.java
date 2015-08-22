package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.SubscribeUSK;
import net.pterodactylus.fcp.SubscribedUSK;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link SubscribeUskCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class SubscribeUskCommandImpl implements SubscribeUskCommand {

	private static final RandomIdentifierGenerator IDENTIFIER = new RandomIdentifierGenerator();
	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final SubscribeUSK subscribeUSK = new SubscribeUSK(IDENTIFIER.generate());
	private final ActiveSubscriptions activeSubscriptions;

	public SubscribeUskCommandImpl(
		ExecutorService threadPool, ConnectionSupplier connectionSupplier,
		ActiveSubscriptions activeSubscriptions) {
		this.activeSubscriptions = activeSubscriptions;
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public Executable<Optional<UskSubscription>> uri(String uri) {
		subscribeUSK.setUri(uri);
		return this::execute;
	}

	private ListenableFuture<Optional<UskSubscription>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<UskSubscription> executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (SubscribeUskDialog subscribeUskDialog = new SubscribeUskDialog()) {
			if (subscribeUskDialog.send(subscribeUSK).get()) {
				UskSubscription uskSubscription = activeSubscriptions.createUskSubscription(subscribeUSK);
				return Optional.of(uskSubscription);
			}
			return Optional.empty();
		}
	}

	private class SubscribeUskDialog extends FcpDialog<Boolean> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicBoolean success = new AtomicBoolean();

		public SubscribeUskDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Boolean getResult() {
			return success.get();
		}

		@Override
		protected void consumeSubscribedUSK(SubscribedUSK subscribedUSK) {
			success.set(true);
			finished.set(true);
		}

		@Override
		protected void consumeIdentifierCollision(IdentifierCollision identifierCollision) {
			finished.set(true);
		}

	}

}
