package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.BaseMessage;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.EndListPersistentRequests;
import net.pterodactylus.fcp.FCPPluginReply;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerRemoved;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.PersistentRequestModified;
import net.pterodactylus.fcp.PersistentRequestRemoved;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.ReceivedBookmarkFeed;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SentFeed;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.SubscribedUSKUpdate;
import net.pterodactylus.fcp.TestDDAComplete;
import net.pterodactylus.fcp.TestDDAReply;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.UnknownNodeIdentifier;
import net.pterodactylus.fcp.UnknownPeerNoteType;

/**
 * An FCP reply sequence enables you to conveniently wait for a specific set of FCP replies.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class FcpReplySequence implements AutoCloseable, FcpListener {

	private final ExecutorService executorService;
	private final FcpConnection fcpConnection;
	private final Map<Class<? extends BaseMessage>, Consumer<BaseMessage>> expectedMessageActions = new HashMap<>();
	private final List<Consumer<FcpMessage>> unknownMessageHandlers = new ArrayList<>();
	private final List<Consumer<Throwable>> closeHandlers = new ArrayList<>();
	private Supplier<Boolean> endPredicate;

	public FcpReplySequence(ExecutorService executorService, FcpConnection fcpConnection) {
		this.executorService = executorService;
		this.fcpConnection = fcpConnection;
	}

	public <M extends BaseMessage> $1<M> handle(Class<M> messageClass) {
		return new $1<>(messageClass);
	}

	public class $1<M extends BaseMessage> {

		private Class<M> messageClass;

		private $1(Class<M> messageClass) {
			this.messageClass = messageClass;
		}

		public FcpReplySequence with(Consumer<M> action) {
			expectedMessageActions.put(messageClass, (Consumer<BaseMessage>) action);
			return FcpReplySequence.this;
		}

	}

	public $2 handleUnknown() {
		return new $2();
	}

	public class $2 {

		public FcpReplySequence with(Consumer<FcpMessage> consumer) {
			unknownMessageHandlers.add(consumer);
			return FcpReplySequence.this;
		}

	}

	public $3 handleClose() {
		return new $3();
	}

	public class $3 {

		public FcpReplySequence with(Consumer<Throwable> consumer) {
			closeHandlers.add(consumer);
			return FcpReplySequence.this;
		}

	}

	public void waitFor(Supplier<Boolean> endPredicate) {
		this.endPredicate = endPredicate;
	}

	public Future<?> send(FcpMessage fcpMessage) throws IOException {
		fcpConnection.addFcpListener(this);
		fcpConnection.sendMessage(fcpMessage);
		return executorService.submit(() -> {
			synchronized (endPredicate) {
				while (!endPredicate.get()) {
					endPredicate.wait();
				}
			}
			return null;
		});
	}

	@Override
	public void close() {
		fcpConnection.removeFcpListener(this);
	}

	private <M extends BaseMessage> void consume(Class<M> fcpMessageClass, BaseMessage fcpMessage) {
		if (expectedMessageActions.containsKey(fcpMessageClass)) {
			expectedMessageActions.get(fcpMessageClass).accept(fcpMessage);
		}
		synchronized (endPredicate) {
			endPredicate.notifyAll();
		}
	}

	private void consumeUnknown(FcpMessage fcpMessage) {
		for (Consumer<FcpMessage> unknownMessageHandler : unknownMessageHandlers) {
			unknownMessageHandler.accept(fcpMessage);
		}
		synchronized (endPredicate) {
			endPredicate.notifyAll();
		}
	}

	private void consumeClose(Throwable throwable) {
		for (Consumer<Throwable> closeHandler : closeHandlers) {
			closeHandler.accept(throwable);
		}
		synchronized (endPredicate) {
			endPredicate.notifyAll();
		}
	}

	@Override
	public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
		consume(NodeHello.class, nodeHello);
	}

	@Override
	public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection,
			CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		consume(CloseConnectionDuplicateClientName.class, closeConnectionDuplicateClientName);
	}

	@Override
	public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
		consume(SSKKeypair.class, sskKeypair);
	}

	@Override
	public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
		consume(Peer.class, peer);
	}

	@Override
	public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		consume(EndListPeers.class, endListPeers);
	}

	@Override
	public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		consume(PeerNote.class, peerNote);
	}

	@Override
	public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		consume(EndListPeerNotes.class, endListPeerNotes);
	}

	@Override
	public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		consume(PeerRemoved.class, peerRemoved);
	}

	@Override
	public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
		consume(NodeData.class, nodeData);
	}

	@Override
	public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		consume(TestDDAReply.class, testDDAReply);
	}

	@Override
	public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		consume(TestDDAComplete.class, testDDAComplete);
	}

	@Override
	public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		consume(PersistentGet.class, persistentGet);
	}

	@Override
	public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		consume(PersistentPut.class, persistentPut);
	}

	@Override
	public void receivedEndListPersistentRequests(FcpConnection fcpConnection,
			EndListPersistentRequests endListPersistentRequests) {
		consume(EndListPersistentRequests.class, endListPersistentRequests);
	}

	@Override
	public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		consume(URIGenerated.class, uriGenerated);
	}

	@Override
	public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		consume(DataFound.class, dataFound);
	}

	@Override
	public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		consume(AllData.class, allData);
	}

	@Override
	public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		consume(SimpleProgress.class, simpleProgress);
	}

	@Override
	public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		consume(StartedCompression.class, startedCompression);
	}

	@Override
	public void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		consume(FinishedCompression.class, finishedCompression);
	}

	@Override
	public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		consume(UnknownPeerNoteType.class, unknownPeerNoteType);
	}

	@Override
	public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection,
			UnknownNodeIdentifier unknownNodeIdentifier) {
		consume(UnknownNodeIdentifier.class, unknownNodeIdentifier);
	}

	@Override
	public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		consume(ConfigData.class, configData);
	}

	@Override
	public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		consume(GetFailed.class, getFailed);
	}

	@Override
	public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		consume(PutFailed.class, putFailed);
	}

	@Override
	public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		consume(IdentifierCollision.class, identifierCollision);
	}

	@Override
	public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		consume(PersistentPutDir.class, persistentPutDir);
	}

	@Override
	public void receivedPersistentRequestRemoved(FcpConnection fcpConnection,
			PersistentRequestRemoved persistentRequestRemoved) {
		consume(PersistentRequestRemoved.class, persistentRequestRemoved);
	}

	@Override
	public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		consume(SubscribedUSKUpdate.class, subscribedUSKUpdate);
	}

	@Override
	public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		consume(PluginInfo.class, pluginInfo);
	}

	@Override
	public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		consume(FCPPluginReply.class, fcpPluginReply);
	}

	@Override
	public void receivedPersistentRequestModified(FcpConnection fcpConnection,
			PersistentRequestModified persistentRequestModified) {
		consume(PersistentRequestModified.class, persistentRequestModified);
	}

	@Override
	public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
		consume(PutSuccessful.class, putSuccessful);
	}

	@Override
	public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		consume(PutFetchable.class, putFetchable);
	}

	@Override
	public void receivedSentFeed(FcpConnection source, SentFeed sentFeed) {
		consume(SentFeed.class, sentFeed);
	}

	@Override
	public void receivedBookmarkFeed(FcpConnection fcpConnection, ReceivedBookmarkFeed receivedBookmarkFeed) {
		consume(ReceivedBookmarkFeed.class, receivedBookmarkFeed);
	}

	@Override
	public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		consume(ProtocolError.class, protocolError);
	}

	@Override
	public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		consumeUnknown(fcpMessage);
	}

	@Override
	public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
		consumeClose(throwable);
	}

}
