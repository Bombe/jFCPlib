package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

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
	private final Supplier<String> identifierGenerator;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();
	private final AtomicReference<Boolean> enabled = new AtomicReference<>();
	private final AtomicReference<Boolean> allowLocalAddresses = new AtomicReference<>();
	private final AtomicReference<Boolean> burstOnly = new AtomicReference<>();
	private final AtomicReference<Boolean> listenOnly = new AtomicReference<>();
	private final AtomicReference<Boolean> ignoreSource = new AtomicReference<>();

	public ModifyPeerCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
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
	public ModifyPeerCommand clearBurstOnly() {
		burstOnly.set(false);
		return this;
	}

	@Override
	public ModifyPeerCommand setListenOnly() {
		listenOnly.set(true);
		return this;
	}

	@Override
	public ModifyPeerCommand clearListenOnly() {
		listenOnly.set(false);
		return this;
	}

	@Override
	public ModifyPeerCommand ignoreSource() {
		ignoreSource.set(true);
		return this;
	}

	@Override
	public ModifyPeerCommand useSource() {
		ignoreSource.set(false);
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
		return threadPool.submit(this::executeDialog);
	}

	private Optional<Peer> executeDialog() throws IOException, ExecutionException, InterruptedException {
		ModifyPeer modifyPeer = new ModifyPeer(identifierGenerator.get(), nodeIdentifier.get());
		Optional.ofNullable(enabled.get()).ifPresent(enabled -> modifyPeer.setEnabled(enabled));
		Optional.ofNullable(allowLocalAddresses.get()).ifPresent(allowed -> modifyPeer.setAllowLocalAddresses(allowed));
		Optional.ofNullable(burstOnly.get()).ifPresent(burstOnly -> modifyPeer.setBurstOnly(burstOnly));
		Optional.ofNullable(listenOnly.get()).ifPresent(listenOnly -> modifyPeer.setListenOnly(listenOnly));
		Optional.ofNullable(ignoreSource.get()).ifPresent(ignoreSource -> modifyPeer.setIgnoreSource(ignoreSource));
		try (ModifyPeerDialog modifyPeerDialog = new ModifyPeerDialog()) {
			return modifyPeerDialog.send(modifyPeer).get();
		}
	}

	private class ModifyPeerDialog extends FcpDialog<Optional<Peer>> {

		public ModifyPeerDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Optional.<Peer>empty());
		}

		@Override
		protected void consumePeer(Peer peer) {
			setResult(Optional.ofNullable(peer));
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finish();
		}

	}

}
