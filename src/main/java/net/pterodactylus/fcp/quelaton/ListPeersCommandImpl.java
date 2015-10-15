package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.Peer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ListPeersCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListPeersCommandImpl implements ListPeersCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;
	private final AtomicBoolean includeMetadata = new AtomicBoolean(false);
	private final AtomicBoolean includeVolatile = new AtomicBoolean(false);

	public ListPeersCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
	}

	@Override
	public ListPeersCommand includeMetadata() {
		includeMetadata.set(true);
		return this;
	}

	@Override
	public ListPeersCommand includeVolatile() {
		includeVolatile.set(true);
		return this;
	}

	@Override
	public ListenableFuture<Collection<Peer>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Collection<Peer> executeDialog() throws InterruptedException, ExecutionException, IOException {
		ListPeers listPeers = new ListPeers(identifierGenerator.get(), includeMetadata.get(), includeVolatile.get());
		try (ListPeersDialog listPeersDialog = new ListPeersDialog()) {
			return listPeersDialog.send(listPeers).get();
		}
	}

	private class ListPeersDialog extends FcpDialog<Collection<Peer>> {

		private final Collection<Peer> peers = new HashSet<>();

		public ListPeersDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Collections.<Peer>emptyList());
		}

		@Override
		protected void consumePeer(Peer peer) {
			peers.add(peer);
		}

		@Override
		protected void consumeEndListPeers(EndListPeers endListPeers) {
			setResult(peers);
		}

	}

}
