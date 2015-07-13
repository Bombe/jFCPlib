package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();

	public ListPeerNotesCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
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
		return threadPool.submit(this::executeSequence);
	}

	private Optional<PeerNote> executeSequence() throws IOException, ExecutionException, InterruptedException {
		ListPeerNotes listPeerNotes =
			new ListPeerNotes(new RandomIdentifierGenerator().generate(), nodeIdentifier.get());
		try (ListPeerNotesDialog listPeerNotesDialog = new ListPeerNotesDialog()) {
			return listPeerNotesDialog.send(listPeerNotes).get();
		}
	}

	private class ListPeerNotesDialog extends FcpDialog<Optional<PeerNote>> {

		private final AtomicReference<PeerNote> peerNote = new AtomicReference<>();
		private final AtomicBoolean finished = new AtomicBoolean();

		public ListPeerNotesDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Optional<PeerNote> getResult() {
			return Optional.ofNullable(peerNote.get());
		}

		@Override
		protected void consumePeerNote(PeerNote peerNote) {
			this.peerNote.set(peerNote);
		}

		@Override
		protected void consumeEndListPeerNotes(EndListPeerNotes endListPeerNotes) {
			finished.set(true);
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finished.set(true);
		}

	}

}
