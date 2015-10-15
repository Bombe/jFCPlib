package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.ListPeerNotes;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.UnknownNodeIdentifier;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ListPeerNotesCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ListPeerNotesCommandImpl implements ListPeerNotesCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();

	public ListPeerNotesCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
	}

	@Override
	public Executable<Optional<PeerNote>> byName(String name) {
		nodeIdentifier.set(name);
		return this::execute;
	}

	@Override
	public Executable<Optional<PeerNote>> byIdentity(String identity) {
		nodeIdentifier.set(identity);
		return this::execute;
	}

	@Override
	public Executable<Optional<PeerNote>> byHostAndPort(String host, int port) {
		nodeIdentifier.set(String.format("%s:%d", host, port));
		return this::execute;
	}

	private ListenableFuture<Optional<PeerNote>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<PeerNote> executeDialog() throws IOException, ExecutionException, InterruptedException {
		ListPeerNotes listPeerNotes = new ListPeerNotes(identifierGenerator.get(), nodeIdentifier.get());
		try (ListPeerNotesDialog listPeerNotesDialog = new ListPeerNotesDialog()) {
			return listPeerNotesDialog.send(listPeerNotes).get();
		}
	}

	private class ListPeerNotesDialog extends FcpDialog<Optional<PeerNote>> {

		public ListPeerNotesDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Optional.<PeerNote>empty());
		}

		@Override
		protected void consumePeerNote(PeerNote peerNote) {
			setResult(Optional.ofNullable(peerNote));
		}

		@Override
		protected void consumeEndListPeerNotes(EndListPeerNotes endListPeerNotes) {
			finish();
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finish();
		}

	}

}
