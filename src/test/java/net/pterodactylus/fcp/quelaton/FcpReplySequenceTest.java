package net.pterodactylus.fcp.quelaton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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

import org.junit.Test;

/**
 * Unit test for {@link FcpReplySequence}.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class FcpReplySequenceTest {

	private final FcpConnection fcpConnection = mock(FcpConnection.class);
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final FcpReplySequence replyWaiter = new FcpReplySequence(executorService, fcpConnection);
	private final FcpMessage fcpMessage = new FcpMessage("Test");

	@Test
	public void canSendMessage() throws IOException {
		replyWaiter.send(fcpMessage);
		verify(fcpConnection).sendMessage(fcpMessage);
	}

	@Test
	public void sendingAMessageRegistersTheWaiterAsFcpListener() throws IOException {
		replyWaiter.send(fcpMessage);
		verify(fcpConnection).addFcpListener(replyWaiter);
	}

	@Test
	public void closingTheReplyWaiterRemovesTheFcpListener() throws IOException {
		replyWaiter.send(fcpMessage);
		replyWaiter.close();
		verify(fcpConnection).removeFcpListener(replyWaiter);
	}

	private <M extends BaseMessage> void waitForASpecificMessage(MessageReceiver<M> messageReceiver,
			Class<M> messageClass, Supplier<M> message) throws IOException, InterruptedException, ExecutionException {
		AtomicBoolean gotMessage = setupMessage(messageClass);
		Future<?> result = replyWaiter.send(fcpMessage);
		sendMessage(messageReceiver, message.get());
		result.get();
		assertThat(gotMessage.get(), is(true));
	}

	private <M extends BaseMessage> void sendMessage(MessageReceiver<M> messageReceiver, M message) {
		messageReceiver.receive(fcpConnection, message);
	}

	private interface MessageReceiver<M extends BaseMessage> {

		void receive(FcpConnection fcpConnection, M message);
	}

	private <M extends BaseMessage> AtomicBoolean setupMessage(Class<M> messageClass) {
		AtomicBoolean gotMessage = new AtomicBoolean();
		replyWaiter.handle(messageClass).with((message) -> gotMessage.set(true));
		replyWaiter.waitFor(() -> gotMessage.get());
		return gotMessage;
	}

	@Test
	public void waitingForNodeHelloWorks() throws IOException, ExecutionException, InterruptedException {
		waitForASpecificMessage(replyWaiter::receivedNodeHello, NodeHello.class,
				() -> new NodeHello(new FcpMessage("NodeHello")));
	}

	@Test
	public void waitingForConnectionClosedDuplicateClientNameWorks()
	throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedCloseConnectionDuplicateClientName,
				CloseConnectionDuplicateClientName.class,
				() -> new CloseConnectionDuplicateClientName(new FcpMessage("CloseConnectionDuplicateClientName")));
	}

	@Test
	public void waitingForSSKKeypairWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedSSKKeypair, SSKKeypair.class,
				() -> new SSKKeypair(new FcpMessage("SSKKeypair")));
	}

	@Test
	public void waitForPeerWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPeer, Peer.class, () -> new Peer(new FcpMessage("Peer")));
	}

	@Test
	public void waitForEndListPeersWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedEndListPeers, EndListPeers.class,
				() -> new EndListPeers(new FcpMessage("EndListPeers")));
	}

	@Test
	public void waitForPeerNoteWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPeerNote, PeerNote.class,
				() -> new PeerNote(new FcpMessage("PeerNote")));
	}

	@Test
	public void waitForEndListPeerNotesWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedEndListPeerNotes, EndListPeerNotes.class,
				() -> new EndListPeerNotes(new FcpMessage("EndListPeerNotes")));
	}

	@Test
	public void waitForPeerRemovedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPeerRemoved, PeerRemoved.class,
				() -> new PeerRemoved(new FcpMessage("PeerRemoved")));
	}

	@Test
	public void waitForNodeDataWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedNodeData, NodeData.class,
				() -> new NodeData(new FcpMessage("NodeData").put("ark.pubURI", "")
						.put(
								"ark.number", "0")
						.put("auth.negTypes", "")
						.put("version", "0,0,0,0")
						.put("lastGoodVersion", "0,0,0,0")));
	}

	@Test
	public void waitForTestDDAReplyWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedTestDDAReply, TestDDAReply.class,
				() -> new TestDDAReply(new FcpMessage("TestDDAReply")));
	}

	@Test
	public void waitForTestDDACompleteWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedTestDDAComplete, TestDDAComplete.class,
				() -> new TestDDAComplete(new FcpMessage("TestDDAComplete")));
	}

	@Test
	public void waitForPersistentGetWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPersistentGet, PersistentGet.class,
				() -> new PersistentGet(new FcpMessage("PersistentGet")));
	}

	@Test
	public void waitForPersistentPutWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPersistentPut, PersistentPut.class,
				() -> new PersistentPut(new FcpMessage("PersistentPut")));
	}

	@Test
	public void waitForEndListPersistentRequestsWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedEndListPersistentRequests, EndListPersistentRequests.class,
				() -> new EndListPersistentRequests(new FcpMessage("EndListPersistentRequests")));
	}

	@Test
	public void waitForURIGeneratedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedURIGenerated, URIGenerated.class,
				() -> new URIGenerated(new FcpMessage("URIGenerated")));
	}

	@Test
	public void waitForDataFoundWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedDataFound, DataFound.class,
				() -> new DataFound(new FcpMessage("DataFound")));
	}

	@Test
	public void waitForAllDataWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedAllData, AllData.class,
				() -> new AllData(new FcpMessage("AllData"), null));
	}

	@Test
	public void waitForSimpleProgressWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedSimpleProgress, SimpleProgress.class,
				() -> new SimpleProgress(new FcpMessage("SimpleProgress")));
	}

	@Test
	public void waitForStartedCompressionWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedStartedCompression, StartedCompression.class,
				() -> new StartedCompression(new FcpMessage("StartedCompression")));
	}

	@Test
	public void waitForFinishedCompressionWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedFinishedCompression, FinishedCompression.class,
				() -> new FinishedCompression(new FcpMessage("FinishedCompression")));
	}

	@Test
	public void waitForUnknownPeerNoteTypeWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedUnknownPeerNoteType, UnknownPeerNoteType.class,
				() -> new UnknownPeerNoteType(new FcpMessage("UnknownPeerNoteType")));
	}

	@Test
	public void waitForUnknownNodeIdentifierWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedUnknownNodeIdentifier, UnknownNodeIdentifier.class,
				() -> new UnknownNodeIdentifier(new FcpMessage("UnknownNodeIdentifier")));
	}

	@Test
	public void waitForConfigDataWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedConfigData, ConfigData.class,
				() -> new ConfigData(new FcpMessage("ConfigData")));
	}

	@Test
	public void waitForGetFailedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedGetFailed, GetFailed.class,
				() -> new GetFailed(new FcpMessage("GetFailed")));
	}

	@Test
	public void waitForPutFailedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPutFailed, PutFailed.class,
				() -> new PutFailed(new FcpMessage("PutFailed")));
	}

	@Test
	public void waitForIdentifierCollisionWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedIdentifierCollision, IdentifierCollision.class,
				() -> new IdentifierCollision(new FcpMessage("IdentifierCollision")));
	}

	@Test
	public void waitForPersistentPutDirWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPersistentPutDir, PersistentPutDir.class,
				() -> new PersistentPutDir(new FcpMessage("PersistentPutDir")));
	}

	@Test
	public void waitForPersistentRequestRemovedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPersistentRequestRemoved, PersistentRequestRemoved.class,
				() -> new PersistentRequestRemoved(new FcpMessage("PersistentRequestRemoved")));
	}

	@Test
	public void waitForSubscribedUSKUpdateWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedSubscribedUSKUpdate, SubscribedUSKUpdate.class,
				() -> new SubscribedUSKUpdate(new FcpMessage("SubscribedUSKUpdate")));
	}

	@Test
	public void waitForPluginInfoWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPluginInfo, PluginInfo.class,
				() -> new PluginInfo(new FcpMessage("PluginInfo")));
	}

	@Test
	public void waitForFCPPluginReply() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedFCPPluginReply, FCPPluginReply.class,
				() -> new FCPPluginReply(new FcpMessage("FCPPluginReply"), null));
	}

	@Test
	public void waitForPersistentRequestModifiedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPersistentRequestModified, PersistentRequestModified.class,
				() -> new PersistentRequestModified(new FcpMessage("PersistentRequestModified")));
	}

	@Test
	public void waitForPutSuccessfulWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPutSuccessful, PutSuccessful.class,
				() -> new PutSuccessful(new FcpMessage("PutSuccessful")));
	}

	@Test
	public void waitForPutFetchableWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedPutFetchable, PutFetchable.class,
				() -> new PutFetchable(new FcpMessage("PutFetchable")));
	}

	@Test
	public void waitForSentFeedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedSentFeed, SentFeed.class,
				() -> new SentFeed(new FcpMessage("SentFeed")));
	}

	@Test
	public void waitForReceivedBookmarkFeedWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedBookmarkFeed, ReceivedBookmarkFeed.class,
				() -> new ReceivedBookmarkFeed(new FcpMessage("ReceivedBookmarkFeed")));
	}

	@Test
	public void waitForProtocolErrorWorks() throws InterruptedException, ExecutionException, IOException {
		waitForASpecificMessage(replyWaiter::receivedProtocolError, ProtocolError.class,
				() -> new ProtocolError(new FcpMessage("ProtocolError")));
	}

	@Test
	public void waitForUnknownMessageWorks() throws IOException, ExecutionException, InterruptedException {
		AtomicReference<FcpMessage> receivedMessage = new AtomicReference<>();
		replyWaiter.handleUnknown().with((message) -> receivedMessage.set(message));
		replyWaiter.waitFor(() -> receivedMessage.get() != null);
		Future<?> result = replyWaiter.send(fcpMessage);
		replyWaiter.receivedMessage(fcpConnection, fcpMessage);
		result.get();
		assertThat(receivedMessage.get(), is(fcpMessage));
	}

	@Test
	public void waitingForMultipleMessagesWorks() throws IOException, ExecutionException, InterruptedException {
		AtomicBoolean gotPutFailed = new AtomicBoolean();
		replyWaiter.handle(PutFailed.class).with((getFailed) -> gotPutFailed.set(true));
		AtomicBoolean gotGetFailed = new AtomicBoolean();
		replyWaiter.handle(GetFailed.class).with((getFailed) -> gotGetFailed.set(true));
		replyWaiter.waitFor(() -> gotGetFailed.get() && gotPutFailed.get());
		Future<?> result = replyWaiter.send(fcpMessage);
		assertThat(result.isDone(), is(false));
		replyWaiter.receivedGetFailed(fcpConnection, new GetFailed(new FcpMessage("GetFailed")));
		assertThat(result.isDone(), is(false));
		replyWaiter.receivedPutFailed(fcpConnection, new PutFailed(new FcpMessage("PutFailed")));
		result.get();
	}

	@Test
	public void waitingForConnectionClosureWorks() throws IOException, ExecutionException, InterruptedException {
		AtomicReference<Throwable> receivedThrowable = new AtomicReference<>();
		replyWaiter.handleClose().with((e) -> receivedThrowable.set(e));
		replyWaiter.waitFor(() -> receivedThrowable.get() != null);
		Future<?> result = replyWaiter.send(fcpMessage);
		Throwable throwable = new Throwable();
		replyWaiter.connectionClosed(fcpConnection, throwable);
		result.get();
		assertThat(receivedThrowable.get(), is(throwable));
	}

}
