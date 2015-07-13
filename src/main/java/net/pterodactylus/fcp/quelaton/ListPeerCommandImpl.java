package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.ListPeer;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.UnknownNodeIdentifier;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

/**
 * Default {@link ListPeerCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ListPeerCommandImpl implements ListPeerCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();

	public ListPeerCommandImpl(ListeningExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = threadPool;
		this.connectionSupplier = connectionSupplier;
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
		ListPeer listPeer = new ListPeer(new RandomIdentifierGenerator().generate(), nodeIdentifier.get());
		try (ListPeerDialog listPeerDialog = new ListPeerDialog()) {
			return Optional.ofNullable(listPeerDialog.send(listPeer).get());
		}
	}

	private class ListPeerDialog extends FcpDialog<Peer> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<Peer> peer = new AtomicReference<>();

		public ListPeerDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Peer getResult() {
			return peer.get();
		}

		@Override
		protected void consumePeer(Peer peer) {
			this.peer.set(peer);
			finished.set(true);
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finished.set(true);
		}

	}

}
