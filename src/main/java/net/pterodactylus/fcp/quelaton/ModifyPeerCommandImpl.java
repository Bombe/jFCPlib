package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.ModifyPeer;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.UnknownNodeIdentifier;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ModifyPeerCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ModifyPeerCommandImpl implements ModifyPeerCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();
	private final AtomicReference<Boolean> enabled = new AtomicReference<>();
	private final AtomicReference<Boolean> allowLocalAddresses = new AtomicReference<>();
	private final AtomicReference<Boolean> burstOnly = new AtomicReference<>();

	public ModifyPeerCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public ModifyPeerCommand enable() {
		enabled.set(true);
		return this;
	}

	@Override
	public ModifyPeerCommand disable() {
		enabled.set(false);
		return this;
	}

	@Override
	public ModifyPeerCommand allowLocalAddresses() {
		allowLocalAddresses.set(true);
		return this;
	}

	@Override
	public ModifyPeerCommand disallowLocalAddresses() {
		allowLocalAddresses.set(false);
		return this;
	}

	@Override
	public ModifyPeerCommand setBurstOnly() {
		burstOnly.set(true);
		return this;
	}

	@Override
	public Executable<Optional<Peer>> byName(String name) {
		nodeIdentifier.set(name);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> byIdentity(String nodeIdentity) {
		nodeIdentifier.set(nodeIdentity);
		return this::execute;
	}

	@Override
	public Executable<Optional<Peer>> byHostAndPort(String host, int port) {
		nodeIdentifier.set(String.format("%s:%d", host, port));
		return this::execute;
	}

	private ListenableFuture<Optional<Peer>> execute() {
		return threadPool.submit(this::executeSequence);
	}

	private Optional<Peer> executeSequence() throws IOException, ExecutionException, InterruptedException {
		ModifyPeer modifyPeer = new ModifyPeer(new RandomIdentifierGenerator().generate(), nodeIdentifier.get());
		Optional.ofNullable(enabled.get()).ifPresent(enabled -> modifyPeer.setEnabled(enabled));
		Optional.ofNullable(allowLocalAddresses.get()).ifPresent(allowed -> modifyPeer.setAllowLocalAddresses(allowed));
		Optional.ofNullable(burstOnly.get()).ifPresent(burstOnly -> modifyPeer.setBurstOnly(burstOnly));
		try (ModifyPeerDialog modifyPeerDialog = new ModifyPeerDialog()) {
			return modifyPeerDialog.send(modifyPeer).get();
		}
	}

	private class ModifyPeerDialog extends FcpDialog<Optional<Peer>> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<Peer> peer = new AtomicReference<>();

		public ModifyPeerDialog() throws IOException {
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
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finished.set(true);
		}

	}

}
