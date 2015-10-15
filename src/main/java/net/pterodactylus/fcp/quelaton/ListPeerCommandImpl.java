package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ListPeer;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.UnknownNodeIdentifier;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ListPeerCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ListPeerCommandImpl implements ListPeerCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();

	public ListPeerCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
	}

	@Override
	public Executable<Optional<Peer>> byName(String name) {
		nodeIdentifier.set(name);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> byIdentity(String identity) {
		nodeIdentifier.set(identity);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> byHostAndPort(String host, int port) {
		nodeIdentifier.set(String.format("%s:%d", host, port));
		return this::execute;
	}

	private ListenableFuture<Optional<Peer>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<Peer> executeDialog() throws IOException, ExecutionException, InterruptedException {
		ListPeer listPeer = new ListPeer(identifierGenerator.get(), nodeIdentifier.get());
		try (ListPeerDialog listPeerDialog = new ListPeerDialog()) {
			return Optional.ofNullable(listPeerDialog.send(listPeer).get());
		}
	}

	private class ListPeerDialog extends FcpDialog<Peer> {

		public ListPeerDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), null);
		}

		@Override
		protected void consumePeer(Peer peer) {
			setResult(peer);
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finish();
		}

	}

}
