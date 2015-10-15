package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

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

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final SubscribeUSK subscribeUSK;
	private final ActiveSubscriptions activeSubscriptions;

	public SubscribeUskCommandImpl(
		ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator,
		ActiveSubscriptions activeSubscriptions) {
		this.activeSubscriptions = activeSubscriptions;
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		subscribeUSK = new SubscribeUSK(identifierGenerator.get());
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

		public SubscribeUskDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), false);
		}

		@Override
		protected void consumeSubscribedUSK(SubscribedUSK subscribedUSK) {
			setResult(true);
		}

		@Override
		protected void consumeIdentifierCollision(IdentifierCollision identifierCollision) {
			finish();
		}

	}

}
