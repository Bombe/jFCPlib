package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * An FCP reply sequence enables you to conveniently wait for a specific set of FCP replies.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public abstract class FcpReplySequence<R> implements AutoCloseable, FcpListener {

	private final Object syncObject = new Object();
	private final ListeningExecutorService executorService;
	private final FcpConnection fcpConnection;
	private final Queue<FcpMessage> messages = new ConcurrentLinkedQueue<>();
	private final AtomicReference<String> identifier = new AtomicReference<>();
	private final AtomicBoolean connectionClosed = new AtomicBoolean();
	private final AtomicReference<Throwable> connectionFailureReason = new AtomicReference<>();

	public FcpReplySequence(ExecutorService executorService, FcpConnection fcpConnection) {
		this.executorService = MoreExecutors.listeningDecorator(executorService);
		this.fcpConnection = fcpConnection;
	}

	protected void setIdentifier(String identifier) {
		this.identifier.set(identifier);
	}

	protected abstract boolean isFinished();

	public ListenableFuture<R> send(FcpMessage fcpMessage) throws IOException {
		setIdentifier(fcpMessage.getField("Identifier"));
		fcpConnection.addFcpListener(this);
		messages.add(fcpMessage);
		return executorService.submit(() -> {
			synchronized (syncObject) {
				while (!connectionClosed.get() && (!isFinished() || !messages.isEmpty())) {
					while (messages.peek() != null) {
						FcpMessage message = messages.poll();
						fcpConnection.sendMessage(message);
					}
					if (isFinished() || connectionClosed.get()) {
						continue;
					}
					syncObject.wait();
				}
			}
			Throwable throwable = connectionFailureReason.get();
			if (throwable != null) {
				throw new ExecutionException(throwable);
			}
			return getResult();
		});
	}

	protected void sendMessage(FcpMessage fcpMessage) {
		messages.add(fcpMessage);
		notifySyncObject();
	}

	private void notifySyncObject() {
		synchronized (syncObject) {
			syncObject.notifyAll();
		}
	}

	protected R getResult() {
		return null;
	}

	@Override
	public void close() {
		fcpConnection.removeFcpListener(this);
	}

	private <M extends BaseMessage> void consume(Consumer<M> consumer, M message) {
		consume(consumer, message, "Identifier");
	}

	private <M extends BaseMessage> void consume(Consumer<M> consumer, M message,
			String identifier) {
		if (Objects.equals(message.getField(identifier), this.identifier.get())) {
			consumeAlways(consumer, message);
		}
	}

	private <M extends BaseMessage> void consumeAlways(Consumer<M> consumer, M message) {
		consumer.accept(message);
		notifySyncObject();
	}

	private void consumeUnknown(FcpMessage fcpMessage) {
		consumeUnknownMessage(fcpMessage);
		notifySyncObject();
	}

	private void consumeClose(Throwable throwable) {
		connectionFailureReason.set(throwable);
		connectionClosed.set(true);
		notifySyncObject();
	}

	@Override
	public final void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
		consume(this::consumeNodeHello, nodeHello);
	}

	protected void consumeNodeHello(NodeHello nodeHello) { }

	@Override
	public final void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection,
			CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		connectionFailureReason.set(new IOException("duplicate client name"));
		connectionClosed.set(true);
		notifySyncObject();
	}

	@Override
	public final void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
		consume(this::consumeSSKKeypair, sskKeypair);
	}

	protected void consumeSSKKeypair(SSKKeypair sskKeypair) { }

	@Override
	public final void receivedPeer(FcpConnection fcpConnection, Peer peer) {
		consume(this::consumePeer, peer);
	}

	protected void consumePeer(Peer peer) { }

	@Override
	public final void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		consume(this::consumeEndListPeers, endListPeers);
	}

	protected void consumeEndListPeers(EndListPeers endListPeers) { }

	@Override
	public final void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		consume(this::consumePeerNote, peerNote);
	}

	protected void consumePeerNote(PeerNote peerNote) { }

	@Override
	public final void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		consume(this::consumeEndListPeerNotes, endListPeerNotes);
	}

	protected void consumeEndListPeerNotes(EndListPeerNotes endListPeerNotes) { }

	@Override
	public final void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		consume(this::consumePeerRemoved, peerRemoved);
	}

	protected void consumePeerRemoved(PeerRemoved peerRemoved) { }

	@Override
	public final void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
		consume(this::consumeNodeData, nodeData);
	}

	protected void consumeNodeData(NodeData nodeData) { }

	@Override
	public final void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		consume(this::consumeTestDDAReply, testDDAReply, "Directory");
	}

	protected void consumeTestDDAReply(TestDDAReply testDDAReply) { }

	@Override
	public final void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		consume(this::consumeTestDDAComplete, testDDAComplete, "Directory");
	}

	protected void consumeTestDDAComplete(TestDDAComplete testDDAComplete) { }

	@Override
	public final void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		consume(this::consumePersistentGet, persistentGet);
	}

	protected void consumePersistentGet(PersistentGet persistentGet) { }

	@Override
	public final void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		consume(this::consumePersistentPut, persistentPut);
	}

	protected void consumePersistentPut(PersistentPut persistentPut) { }

	@Override
	public final void receivedEndListPersistentRequests(FcpConnection fcpConnection,
			EndListPersistentRequests endListPersistentRequests) {
		consume(this::consumeEndListPersistentRequests, endListPersistentRequests);
	}

	protected void consumeEndListPersistentRequests(EndListPersistentRequests endListPersistentRequests) { }

	@Override
	public final void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		consume(this::consumeURIGenerated, uriGenerated);
	}

	protected void consumeURIGenerated(URIGenerated uriGenerated) { }

	@Override
	public final void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		consume(this::consumeDataFound, dataFound);
	}

	protected void consumeDataFound(DataFound dataFound) { }

	@Override
	public final void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		consume(this::consumeAllData, allData);
	}

	protected void consumeAllData(AllData allData) { }

	@Override
	public final void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		consume(this::consumeSimpleProgress, simpleProgress);
	}

	protected void consumeSimpleProgress(SimpleProgress simpleProgress) { }

	@Override
	public final void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		consume(this::consumeStartedCompression, startedCompression);
	}

	protected void consumeStartedCompression(StartedCompression startedCompression) { }

	@Override
	public final void receivedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		consume(this::consumeFinishedCompression, finishedCompression);
	}

	protected void consumeFinishedCompression(FinishedCompression finishedCompression) { }

	@Override
	public final void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		consume(this::consumeUnknownPeerNoteType, unknownPeerNoteType);
	}

	protected void consumeUnknownPeerNoteType(UnknownPeerNoteType unknownPeerNoteType) { }

	@Override
	public final void receivedUnknownNodeIdentifier(FcpConnection fcpConnection,
			UnknownNodeIdentifier unknownNodeIdentifier) {
		consume(this::consumeUnknownNodeIdentifier, unknownNodeIdentifier);
	}

	protected void consumeUnknownNodeIdentifier(UnknownNodeIdentifier unknownNodeIdentifier) { }

	@Override
	public final void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		consume(this::consumeConfigData, configData);
	}

	protected void consumeConfigData(ConfigData configData) { }

	@Override
	public final void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		consume(this::consumeGetFailed, getFailed);
	}

	protected void consumeGetFailed(GetFailed getFailed) { }

	@Override
	public final void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		consume(this::consumePutFailed, putFailed);
	}

	protected void consumePutFailed(PutFailed putFailed) { }

	@Override
	public final void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		consume(this::consumeIdentifierCollision, identifierCollision);
	}

	protected void consumeIdentifierCollision(IdentifierCollision identifierCollision) { }

	@Override
	public final void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		consume(this::consumePersistentPutDir, persistentPutDir);
	}

	protected void consumePersistentPutDir(PersistentPutDir persistentPutDir) { }

	@Override
	public final void receivedPersistentRequestRemoved(FcpConnection fcpConnection,
			PersistentRequestRemoved persistentRequestRemoved) {
		consume(this::consumePersistentRequestRemoved, persistentRequestRemoved);
	}

	protected void consumePersistentRequestRemoved(PersistentRequestRemoved persistentRequestRemoved) { }

	@Override
	public final void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		consume(this::consumeSubscribedUSKUpdate, subscribedUSKUpdate);
	}

	protected void consumeSubscribedUSKUpdate(SubscribedUSKUpdate subscribedUSKUpdate) { }

	@Override
	public final void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		consume(this::consumePluginInfo, pluginInfo);
	}

	protected void consumePluginInfo(PluginInfo pluginInfo) { }

	@Override
	public final void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		consume(this::consumeFCPPluginReply, fcpPluginReply);
	}

	protected void consumeFCPPluginReply(FCPPluginReply fcpPluginReply) { }

	@Override
	public final void receivedPersistentRequestModified(FcpConnection fcpConnection,
			PersistentRequestModified persistentRequestModified) {
		consume(this::consumePersistentRequestModified, persistentRequestModified);
	}

	protected void consumePersistentRequestModified(PersistentRequestModified persistentRequestModified) { }

	@Override
	public final void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
		consume(this::consumePutSuccessful, putSuccessful);
	}

	protected void consumePutSuccessful(PutSuccessful putSuccessful) { }

	@Override
	public final void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		consume(this::consumePutFetchable, putFetchable);
	}

	protected void consumePutFetchable(PutFetchable putFetchable) { }

	@Override
	public final void receivedSentFeed(FcpConnection source, SentFeed sentFeed) {
		consume(this::consumeSentFeed, sentFeed);
	}

	protected void consumeSentFeed(SentFeed sentFeed) { }

	@Override
	public final void receivedBookmarkFeed(FcpConnection fcpConnection, ReceivedBookmarkFeed receivedBookmarkFeed) {
		consume(this::consumeReceivedBookmarkFeed, receivedBookmarkFeed);
	}

	protected void consumeReceivedBookmarkFeed(ReceivedBookmarkFeed receivedBookmarkFeed) { }

	@Override
	public final void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		consume(this::consumeProtocolError, protocolError);
	}

	protected void consumeProtocolError(ProtocolError protocolError) { }

	@Override
	public final void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		consumeUnknown(fcpMessage);
	}

	protected void consumeUnknownMessage(FcpMessage fcpMessage) { }

	@Override
	public final void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
		consumeClose(throwable);
	}

}
