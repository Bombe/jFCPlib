package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.AddPeer;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.ProtocolError;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link AddPeerCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class AddPeerCommandImpl implements AddPeerCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicReference<File> file = new AtomicReference<>();
	private final AtomicReference<URL> url = new AtomicReference<>();
	private final AtomicReference<NodeRef> nodeRef = new AtomicReference<>();

	public AddPeerCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public Executable<Optional<Peer>> fromFile(File file) {
		this.file.set(file);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> fromURL(URL url) {
		this.url.set(url);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> fromNodeRef(NodeRef nodeRef) {
		this.nodeRef.set(nodeRef);
		return this::execute;
	}

	private ListenableFuture<Optional<Peer>> execute() {
		return threadPool.submit(this::executeSequence);
	}

	private Optional<Peer> executeSequence() throws IOException, ExecutionException, InterruptedException {
		AddPeer addPeer = null;
		if (file.get() != null) {
			addPeer = new AddPeer(new RandomIdentifierGenerator().generate(), file.get().getPath());
		} else if (url.get() != null) {
			addPeer = new AddPeer(new RandomIdentifierGenerator().generate(), url.get());
		} else {
			addPeer = new AddPeer(new RandomIdentifierGenerator().generate(), nodeRef.get());
		}
		try (AddPeerSequence addPeerSequence = new AddPeerSequence()) {
			return addPeerSequence.send(addPeer).get();
		}
	}

	private class AddPeerSequence extends FcpDialog<Optional<Peer>> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<Peer> peer = new AtomicReference<>();

		public AddPeerSequence() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Optional<Peer> getResult() {
			return Optional.ofNullable(peer.get());
		}

		@Override
		protected void consumePeer(Peer peer) {
			this.peer.set(peer);
			finished.set(true);
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finished.set(true);
		}

	}

}
