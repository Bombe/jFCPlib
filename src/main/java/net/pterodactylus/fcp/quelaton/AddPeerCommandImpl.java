package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
	private final Supplier<String> identifierSupplier;
	private final AtomicReference<File> file = new AtomicReference<>();
	private final AtomicReference<URL> url = new AtomicReference<>();
	private final AtomicReference<NodeRef> nodeRef = new AtomicReference<>();

	public AddPeerCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierSupplier) {
		this.identifierSupplier = identifierSupplier;
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
		return threadPool.submit(this::executeDialog);
	}

	private Optional<Peer> executeDialog() throws IOException, ExecutionException, InterruptedException {
		AddPeer addPeer;
		if (file.get() != null) {
			addPeer = new AddPeer(identifierSupplier.get(), file.get().getPath());
		} else if (url.get() != null) {
			addPeer = new AddPeer(identifierSupplier.get(), url.get());
		} else {
			addPeer = new AddPeer(identifierSupplier.get(), nodeRef.get());
		}
		try (AddPeerDialog addPeerDialog = new AddPeerDialog()) {
			return addPeerDialog.send(addPeer).get();
		}
	}

	private class AddPeerDialog extends FcpDialog<Optional<Peer>> {

		public AddPeerDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Optional.empty());
		}

		@Override
		protected void consumePeer(Peer peer) {
			setResult(Optional.ofNullable(peer));
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finish();
		}

	}

}
