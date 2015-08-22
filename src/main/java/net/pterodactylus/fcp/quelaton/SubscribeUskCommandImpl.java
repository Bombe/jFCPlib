package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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

	public SubscribeUskCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
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
			return subscribeUskDialog.send(subscribeUSK).get();
		}
	}

	private class SubscribeUskDialog extends FcpDialog<Optional<UskSubscription>> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<SubscribedUSK> subscribedUSK = new AtomicReference<>();

		public SubscribeUskDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Optional<UskSubscription> getResult() {
			return Optional.ofNullable(subscribedUSK.get()).map(subscribedUSK -> new UskSubscription() {
				@Override
				public String getUri() {
					return subscribedUSK.getURI();
				}
			});
		}

		@Override
		protected void consumeSubscribedUSK(SubscribedUSK subscribedUSK) {
			this.subscribedUSK.set(subscribedUSK);
			finished.set(true);
		}

		@Override
		protected void consumeIdentifierCollision(IdentifierCollision identifierCollision) {
			finished.set(true);
		}

	}

}
