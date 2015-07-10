package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.Peer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ListPeersCommand} implementation based on {@link FcpReplySequence}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListPeersCommandImpl implements ListPeersCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicBoolean includeMetadata = new AtomicBoolean(false);
	private final AtomicBoolean includeVolatile = new AtomicBoolean(false);

	public ListPeersCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
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
		return threadPool.submit(this::executeSequence);
	}

	private Collection<Peer> executeSequence() throws InterruptedException, ExecutionException, IOException {
		String identifier = new RandomIdentifierGenerator().generate();
		ListPeers listPeers = new ListPeers(identifier, includeMetadata.get(), includeVolatile.get());
		try (ListPeersReplySequence listPeersReplySequence = new ListPeersReplySequence()) {
			return listPeersReplySequence.send(listPeers).get();
		}
	}

	private class ListPeersReplySequence extends FcpReplySequence<Collection<Peer>> {

		private final Collection<Peer> peers = new HashSet<>();
		private final AtomicBoolean finished = new AtomicBoolean(false);

		public ListPeersReplySequence() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Collection<Peer> getResult() {
			return peers;
		}

		@Override
		protected void consumePeer(Peer peer) {
			peers.add(peer);
		}

		@Override
		protected void consumeEndListPeers(EndListPeers endListPeers) {
			finished.set(true);
		}

	}

}
