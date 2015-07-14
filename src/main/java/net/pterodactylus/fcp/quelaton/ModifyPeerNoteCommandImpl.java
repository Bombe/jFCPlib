package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.FreenetBase64;
import net.pterodactylus.fcp.ModifyPeerNote;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerNoteType;
import net.pterodactylus.fcp.UnknownNodeIdentifier;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ModifyPeerNoteCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ModifyPeerNoteCommandImpl implements ModifyPeerNoteCommand {

	private static final FreenetBase64 BASE_64 = new FreenetBase64();
	private static final RandomIdentifierGenerator RANDOM_IDENTIFIER_GENERATOR = new RandomIdentifierGenerator();
	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicReference<String> nodeIdentifier = new AtomicReference<>();
	private final AtomicReference<String> darknetComment = new AtomicReference<>();

	public ModifyPeerNoteCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public ModifyPeerNoteCommand darknetComment(String text) {
		darknetComment.set(text);
		return this;
	}

	@Override
	public Executable<Boolean> byName(String name) {
		nodeIdentifier.set(name);
		return this::execute;
	}

	private ListenableFuture<Boolean> execute() {
		if (darknetComment.get() == null) {
			return Futures.immediateFuture(false);
		}
		return threadPool.submit(this::executeDialog);
	}

	private Boolean executeDialog() throws IOException, ExecutionException, InterruptedException {
		ModifyPeerNote modifyPeerNote =
			new ModifyPeerNote(RANDOM_IDENTIFIER_GENERATOR.generate(), nodeIdentifier.get());
		modifyPeerNote.setPeerNoteType(PeerNoteType.PRIVATE_DARKNET_COMMENT);
		modifyPeerNote.setNoteText(BASE_64.encode(darknetComment.get().getBytes(StandardCharsets.UTF_8)));
		try (ModifyPeerNoteDialog modifyPeerNoteDialog = new ModifyPeerNoteDialog()) {
			return modifyPeerNoteDialog.send(modifyPeerNote).get();
		}
	}

	private class ModifyPeerNoteDialog extends FcpDialog<Boolean> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicBoolean successful = new AtomicBoolean();

		public ModifyPeerNoteDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Boolean getResult() {
			return successful.get();
		}

		@Override
		protected void consumePeerNote(PeerNote peerNote) {
			successful.set(true);
			finished.set(true);
		}

		@Override
		protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) {
			finished.set(true);
		}

	}

}
