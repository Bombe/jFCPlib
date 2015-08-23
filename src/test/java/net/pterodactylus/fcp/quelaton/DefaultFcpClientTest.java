package net.pterodactylus.fcp.quelaton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.pterodactylus.fcp.ARK;
import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.DSAGroup;
import net.pterodactylus.fcp.FcpKeyPair;
import net.pterodactylus.fcp.Key;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.fake.FakeTcpServer;
import net.pterodactylus.fcp.quelaton.ClientGetCommand.Data;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.nitorcreations.junit.runners.NestedRunner;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link DefaultFcpClient}.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
@RunWith(NestedRunner.class)
public class DefaultFcpClientTest {

	private static final String INSERT_URI =
		"SSK@RVCHbJdkkyTCeNN9AYukEg76eyqmiosSaNKgE3U9zUw,7SHH53gletBVb9JD7nBsyClbLQsBubDPEIcwg908r7Y,AQECAAE/";
	private static final String REQUEST_URI =
		"SSK@wtbgd2loNcJCXvtQVOftl2tuWBomDQHfqS6ytpPRhfw,7SHH53gletBVb9JD7nBsyClbLQsBubDPEIcwg908r7Y,AQACAAE/";

	private int threadCounter = 0;
	private final ExecutorService threadPool =
		Executors.newCachedThreadPool(r -> new Thread(r, "Test-Thread-" + threadCounter++));
	private final FakeTcpServer fcpServer;
	private final DefaultFcpClient fcpClient;

	public DefaultFcpClientTest() throws IOException {
		fcpServer = new FakeTcpServer(threadPool);
		fcpClient = new DefaultFcpClient(threadPool, "localhost", fcpServer.getPort(), () -> "Test");
	}

	@After
	public void tearDown() throws IOException {
		fcpServer.close();
		threadPool.shutdown();
	}

	@Test(expected = ExecutionException.class)
	public void defaultFcpClientThrowsExceptionIfItCanNotConnect()
	throws IOException, ExecutionException, InterruptedException {
		Future<FcpKeyPair> keyPairFuture = fcpClient.generateKeypair().execute();
		fcpServer.connect().get();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.writeLine(
			"CloseConnectionDuplicateClientName",
			"EndMessage"
		);
		keyPairFuture.get();
	}

	@Test(expected = ExecutionException.class)
	public void defaultFcpClientThrowsExceptionIfConnectionIsClosed()
	throws IOException, ExecutionException, InterruptedException {
		Future<FcpKeyPair> keyPairFuture = fcpClient.generateKeypair().execute();
		fcpServer.connect().get();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.close();
		keyPairFuture.get();
	}

	@Test
	public void defaultFcpClientCanGenerateKeypair() throws ExecutionException, InterruptedException, IOException {
		Future<FcpKeyPair> keyPairFuture = fcpClient.generateKeypair().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine("SSKKeypair",
			"InsertURI=" + INSERT_URI + "",
			"RequestURI=" + REQUEST_URI + "",
			"Identifier=" + identifier,
			"EndMessage");
		FcpKeyPair keyPair = keyPairFuture.get();
		assertThat(keyPair.getPublicKey(), is(REQUEST_URI));
		assertThat(keyPair.getPrivateKey(), is(INSERT_URI));
	}

	private void connectNode() throws InterruptedException, ExecutionException, IOException {
		fcpServer.connect().get();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.writeLine("NodeHello",
			"CompressionCodecs=4 - GZIP(0), BZIP2(1), LZMA(2), LZMA_NEW(3)",
			"Revision=build01466",
			"Testnet=false",
			"Version=Fred,0.7,1.0,1466",
			"Build=1466",
			"ConnectionIdentifier=14318898267048452a81b36e7f13a3f0",
			"Node=Fred",
			"ExtBuild=29",
			"FCPVersion=2.0",
			"NodeLanguage=ENGLISH",
			"ExtRevision=v29",
			"EndMessage"
		);
	}

	@Test
	public void clientGetCanDownloadData() throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Data>> dataFuture = fcpClient.clientGet().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "ReturnType=direct", "URI=KSK@foo.txt"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"AllData",
			"Identifier=" + identifier,
			"DataLength=6",
			"StartupTime=1435610539000",
			"CompletionTime=1435610540000",
			"Metadata.ContentType=text/plain;charset=utf-8",
			"Data",
			"Hello"
		);
		Optional<Data> data = dataFuture.get();
		assertThat(data.get().getMimeType(), is("text/plain;charset=utf-8"));
		assertThat(data.get().size(), is(6L));
		assertThat(ByteStreams.toByteArray(data.get().getInputStream()),
			is("Hello\n".getBytes(StandardCharsets.UTF_8)));
	}

	private String extractIdentifier(List<String> lines) {
		return lines.stream()
			.filter(s -> s.startsWith("Identifier="))
			.map(s -> s.substring(s.indexOf('=') + 1))
			.findFirst()
			.orElse("");
	}

	@Test
	public void clientGetDownloadsDataForCorrectIdentifier()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Data>> dataFuture = fcpClient.clientGet().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"AllData",
			"Identifier=not-test",
			"DataLength=12",
			"StartupTime=1435610539000",
			"CompletionTime=1435610540000",
			"Metadata.ContentType=text/plain;charset=latin-9",
			"Data",
			"Hello World"
		);
		fcpServer.writeLine(
			"AllData",
			"Identifier=" + identifier,
			"DataLength=6",
			"StartupTime=1435610539000",
			"CompletionTime=1435610540000",
			"Metadata.ContentType=text/plain;charset=utf-8",
			"Data",
			"Hello"
		);
		Optional<Data> data = dataFuture.get();
		assertThat(data.get().getMimeType(), is("text/plain;charset=utf-8"));
		assertThat(data.get().size(), is(6L));
		assertThat(ByteStreams.toByteArray(data.get().getInputStream()),
			is("Hello\n".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void clientGetRecognizesGetFailed() throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Data>> dataFuture = fcpClient.clientGet().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"GetFailed",
			"Identifier=" + identifier,
			"Code=3",
			"EndMessage"
		);
		Optional<Data> data = dataFuture.get();
		assertThat(data.isPresent(), is(false));
	}

	@Test
	public void clientGetRecognizesGetFailedForCorrectIdentifier()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Data>> dataFuture = fcpClient.clientGet().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"GetFailed",
			"Identifier=not-test",
			"Code=3",
			"EndMessage"
		);
		fcpServer.writeLine(
			"GetFailed",
			"Identifier=" + identifier,
			"Code=3",
			"EndMessage"
		);
		Optional<Data> data = dataFuture.get();
		assertThat(data.isPresent(), is(false));
	}

	@Test(expected = ExecutionException.class)
	public void clientGetRecognizesConnectionClosed() throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Data>> dataFuture = fcpClient.clientGet().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt"));
		fcpServer.close();
		dataFuture.get();
	}

	@Test
	public void defaultFcpClientReusesConnection() throws InterruptedException, ExecutionException, IOException {
		Future<FcpKeyPair> keyPair = fcpClient.generateKeypair().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"SSKKeypair",
			"InsertURI=" + INSERT_URI + "",
			"RequestURI=" + REQUEST_URI + "",
			"Identifier=" + identifier,
			"EndMessage"
		);
		keyPair.get();
		keyPair = fcpClient.generateKeypair().execute();
		lines = fcpServer.collectUntil(is("EndMessage"));
		identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"SSKKeypair",
			"InsertURI=" + INSERT_URI + "",
			"RequestURI=" + REQUEST_URI + "",
			"Identifier=" + identifier,
			"EndMessage"
		);
		keyPair.get();
	}

	@Test
	public void defaultFcpClientCanReconnectAfterConnectionHasBeenClosed()
	throws InterruptedException, ExecutionException, IOException {
		Future<FcpKeyPair> keyPair = fcpClient.generateKeypair().execute();
		connectNode();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.close();
		try {
			keyPair.get();
			Assert.fail();
		} catch (ExecutionException e) {
		}
		keyPair = fcpClient.generateKeypair().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"SSKKeypair",
			"InsertURI=" + INSERT_URI + "",
			"RequestURI=" + REQUEST_URI + "",
			"Identifier=" + identifier,
			"EndMessage"
		);
		keyPair.get();
	}

	@Test
	public void clientGetWithIgnoreDataStoreSettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().ignoreDataStore().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "IgnoreDS=true"));
	}

	@Test
	public void clientGetWithDataStoreOnlySettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().dataStoreOnly().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "DSonly=true"));
	}

	@Test
	public void clientGetWithMaxSizeSettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().maxSize(1048576).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "MaxSize=1048576"));
	}

	@Test
	public void clientGetWithPrioritySettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().priority(Priority.interactive).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "PriorityClass=1"));
	}

	@Test
	public void clientGetWithRealTimeSettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().realTime().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "RealTimeFlag=true"));
	}

	@Test
	public void clientGetWithGlobalSettingSendsCorrectCommands()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientGet().global().uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage("ClientGet", "URI=KSK@foo.txt", "Global=true"));
	}

	private Matcher<List<String>> matchesFcpMessage(String name, String... requiredLines) {
		return new TypeSafeDiagnosingMatcher<List<String>>() {
			@Override
			protected boolean matchesSafely(List<String> item, Description mismatchDescription) {
				if (!item.get(0).equals(name)) {
					mismatchDescription.appendText("FCP message is named ").appendValue(item.get(0));
					return false;
				}
				for (String requiredLine : requiredLines) {
					if (item.indexOf(requiredLine) < 1) {
						mismatchDescription.appendText("FCP message does not contain ").appendValue(requiredLine);
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("FCP message named ").appendValue(name);
				description.appendValueList(", containing the lines ", ", ", "", requiredLines);
			}
		};
	}

	@Test
	public void clientPutWithDirectDataSendsCorrectCommand()
	throws IOException, ExecutionException, InterruptedException {
		fcpClient.clientPut()
			.from(new ByteArrayInputStream("Hello\n".getBytes()))
			.length(6)
			.uri("KSK@foo.txt")
			.execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("Hello"));
		assertThat(lines, matchesFcpMessage("ClientPut", "UploadFrom=direct", "DataLength=6", "URI=KSK@foo.txt"));
	}

	@Test
	public void clientPutWithDirectDataSucceedsOnCorrectIdentifier()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Key>> key = fcpClient.clientPut()
			.from(new ByteArrayInputStream("Hello\n".getBytes()))
			.length(6)
			.uri("KSK@foo.txt")
			.execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("Hello"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"PutFailed",
			"Identifier=not-the-right-one",
			"EndMessage"
		);
		fcpServer.writeLine(
			"PutSuccessful",
			"URI=KSK@foo.txt",
			"Identifier=" + identifier,
			"EndMessage"
		);
		assertThat(key.get().get().getKey(), is("KSK@foo.txt"));
	}

	@Test
	public void clientPutWithDirectDataFailsOnCorrectIdentifier()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Key>> key = fcpClient.clientPut()
			.from(new ByteArrayInputStream("Hello\n".getBytes()))
			.length(6)
			.uri("KSK@foo.txt")
			.execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("Hello"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"PutSuccessful",
			"Identifier=not-the-right-one",
			"URI=KSK@foo.txt",
			"EndMessage"
		);
		fcpServer.writeLine(
			"PutFailed",
			"Identifier=" + identifier,
			"EndMessage"
		);
		assertThat(key.get().isPresent(), is(false));
	}

	@Test
	public void clientPutWithRenamedDirectDataSendsCorrectCommand()
	throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientPut()
			.named("otherName.txt")
			.from(new ByteArrayInputStream("Hello\n".getBytes()))
			.length(6)
			.uri("KSK@foo.txt")
			.execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("Hello"));
		assertThat(lines, matchesFcpMessage("ClientPut", "TargetFilename=otherName.txt", "UploadFrom=direct",
			"DataLength=6", "URI=KSK@foo.txt"));
	}

	@Test
	public void clientPutWithRedirectSendsCorrectCommand()
	throws IOException, ExecutionException, InterruptedException {
		fcpClient.clientPut().redirectTo("KSK@bar.txt").uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines,
			matchesFcpMessage("ClientPut", "UploadFrom=redirect", "URI=KSK@foo.txt", "TargetURI=KSK@bar.txt"));
	}

	@Test
	public void clientPutWithFileSendsCorrectCommand() throws InterruptedException, ExecutionException, IOException {
		fcpClient.clientPut().from(new File("/tmp/data.txt")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines,
			matchesFcpMessage("ClientPut", "UploadFrom=disk", "URI=KSK@foo.txt", "Filename=/tmp/data.txt"));
	}

	@Test
	public void clientPutWithFileCanCompleteTestDdaSequence()
	throws IOException, ExecutionException, InterruptedException {
		File tempFile = createTempFile();
		fcpClient.clientPut().from(new File(tempFile.getParent(), "test.dat")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=" + identifier,
			"Code=25",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDARequest",
			"Directory=" + tempFile.getParent(),
			"WantReadDirectory=true",
			"WantWriteDirectory=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"TestDDAReply",
			"Directory=" + tempFile.getParent(),
			"ReadFilename=" + tempFile,
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDAResponse",
			"Directory=" + tempFile.getParent(),
			"ReadContent=test-content",
			"EndMessage"
		));
		fcpServer.writeLine(
			"TestDDAComplete",
			"Directory=" + tempFile.getParent(),
			"ReadDirectoryAllowed=true",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines,
			matchesFcpMessage("ClientPut", "UploadFrom=disk", "URI=KSK@foo.txt",
				"Filename=" + new File(tempFile.getParent(), "test.dat")));
	}

	private File createTempFile() throws IOException {
		File tempFile = File.createTempFile("test-dda-", ".dat");
		tempFile.deleteOnExit();
		Files.write("test-content", tempFile, StandardCharsets.UTF_8);
		return tempFile;
	}

	@Test
	public void clientPutDoesNotReactToProtocolErrorForDifferentIdentifier()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Key>> key = fcpClient.clientPut().from(new File("/tmp/data.txt")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=not-the-right-one",
			"Code=25",
			"EndMessage"
		);
		fcpServer.writeLine(
			"PutSuccessful",
			"Identifier=" + identifier,
			"URI=KSK@foo.txt",
			"EndMessage"
		);
		assertThat(key.get().get().getKey(), is("KSK@foo.txt"));
	}

	@Test
	public void clientPutAbortsOnProtocolErrorOtherThan25()
	throws InterruptedException, ExecutionException, IOException {
		Future<Optional<Key>> key = fcpClient.clientPut().from(new File("/tmp/data.txt")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=" + identifier,
			"Code=1",
			"EndMessage"
		);
		assertThat(key.get().isPresent(), is(false));
	}

	@Test
	public void clientPutDoesNotReplyToWrongTestDdaReply() throws IOException, ExecutionException,
	InterruptedException {
		File tempFile = createTempFile();
		fcpClient.clientPut().from(new File(tempFile.getParent(), "test.dat")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=" + identifier,
			"Code=25",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDARequest",
			"Directory=" + tempFile.getParent(),
			"WantReadDirectory=true",
			"WantWriteDirectory=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"TestDDAReply",
			"Directory=/some-other-directory",
			"ReadFilename=" + tempFile,
			"EndMessage"
		);
		fcpServer.writeLine(
			"TestDDAReply",
			"Directory=" + tempFile.getParent(),
			"ReadFilename=" + tempFile,
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDAResponse",
			"Directory=" + tempFile.getParent(),
			"ReadContent=test-content",
			"EndMessage"
		));
	}

	@Test
	public void clientPutSendsResponseEvenIfFileCanNotBeRead()
	throws IOException, ExecutionException, InterruptedException {
		File tempFile = createTempFile();
		fcpClient.clientPut().from(new File(tempFile.getParent(), "test.dat")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=" + identifier,
			"Code=25",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDARequest",
			"Directory=" + tempFile.getParent(),
			"WantReadDirectory=true",
			"WantWriteDirectory=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"TestDDAReply",
			"Directory=" + tempFile.getParent(),
			"ReadFilename=" + tempFile + ".foo",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDAResponse",
			"Directory=" + tempFile.getParent(),
			"ReadContent=failed-to-read",
			"EndMessage"
		));
	}

	@Test
	public void clientPutDoesNotResendOriginalClientPutOnTestDDACompleteWithWrongDirectory()
	throws IOException, ExecutionException, InterruptedException {
		File tempFile = createTempFile();
		fcpClient.clientPut().from(new File(tempFile.getParent(), "test.dat")).uri("KSK@foo.txt").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"TestDDAComplete",
			"Directory=/some-other-directory",
			"EndMessage"
		);
		fcpServer.writeLine(
			"ProtocolError",
			"Identifier=" + identifier,
			"Code=25",
			"EndMessage"
		);
		lines = fcpServer.collectUntil(is("EndMessage"));
		assertThat(lines, matchesFcpMessage(
			"TestDDARequest",
			"Directory=" + tempFile.getParent(),
			"WantReadDirectory=true",
			"WantWriteDirectory=false",
			"EndMessage"
		));
	}

	@Test
	public void clientPutSendsNotificationsForGeneratedKeys()
	throws InterruptedException, ExecutionException, IOException {
		List<String> generatedKeys = new CopyOnWriteArrayList<>();
		Future<Optional<Key>> key = fcpClient.clientPut()
			.onKeyGenerated(generatedKeys::add)
			.from(new ByteArrayInputStream("Hello\n".getBytes()))
			.length(6)
			.uri("KSK@foo.txt")
			.execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("Hello"));
		String identifier = extractIdentifier(lines);
		fcpServer.writeLine(
			"URIGenerated",
			"Identifier=" + identifier,
			"URI=KSK@foo.txt",
			"EndMessage"
		);
		fcpServer.writeLine(
			"PutSuccessful",
			"URI=KSK@foo.txt",
			"Identifier=" + identifier,
			"EndMessage"
		);
		assertThat(key.get().get().getKey(), is("KSK@foo.txt"));
		assertThat(generatedKeys, contains("KSK@foo.txt"));
	}

	@Test
	public void defaultFcpClientCanGetNodeInformation() throws InterruptedException, ExecutionException, IOException {
		Future<NodeData> nodeData = fcpClient.getNode().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetNode",
			"Identifier=" + identifier,
			"GiveOpennetRef=false",
			"WithPrivate=false",
			"WithVolatile=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"NodeData",
			"Identifier=" + identifier,
			"ark.pubURI=SSK@3YEf.../ark",
			"ark.number=78",
			"auth.negTypes=2",
			"version=Fred,0.7,1.0,1466",
			"lastGoodVersion=Fred,0.7,1.0,1466",
			"EndMessage"
		);
		assertThat(nodeData.get(), notNullValue());
	}

	@Test
	public void defaultFcpClientCanGetNodeInformationWithOpennetRef()
	throws InterruptedException, ExecutionException, IOException {
		Future<NodeData> nodeData = fcpClient.getNode().opennetRef().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetNode",
			"Identifier=" + identifier,
			"GiveOpennetRef=true",
			"WithPrivate=false",
			"WithVolatile=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"NodeData",
			"Identifier=" + identifier,
			"opennet=true",
			"ark.pubURI=SSK@3YEf.../ark",
			"ark.number=78",
			"auth.negTypes=2",
			"version=Fred,0.7,1.0,1466",
			"lastGoodVersion=Fred,0.7,1.0,1466",
			"EndMessage"
		);
		assertThat(nodeData.get().getVersion().toString(), is("Fred,0.7,1.0,1466"));
	}

	@Test
	public void defaultFcpClientCanGetNodeInformationWithPrivateData()
	throws InterruptedException, ExecutionException, IOException {
		Future<NodeData> nodeData = fcpClient.getNode().includePrivate().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetNode",
			"Identifier=" + identifier,
			"GiveOpennetRef=false",
			"WithPrivate=true",
			"WithVolatile=false",
			"EndMessage"
		));
		fcpServer.writeLine(
			"NodeData",
			"Identifier=" + identifier,
			"opennet=false",
			"ark.pubURI=SSK@3YEf.../ark",
			"ark.number=78",
			"auth.negTypes=2",
			"version=Fred,0.7,1.0,1466",
			"lastGoodVersion=Fred,0.7,1.0,1466",
			"ark.privURI=SSK@XdHMiRl",
			"EndMessage"
		);
		assertThat(nodeData.get().getARK().getPrivateURI(), is("SSK@XdHMiRl"));
	}

	@Test
	public void defaultFcpClientCanGetNodeInformationWithVolatileData()
	throws InterruptedException, ExecutionException, IOException {
		Future<NodeData> nodeData = fcpClient.getNode().includeVolatile().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetNode",
			"Identifier=" + identifier,
			"GiveOpennetRef=false",
			"WithPrivate=false",
			"WithVolatile=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"NodeData",
			"Identifier=" + identifier,
			"opennet=false",
			"ark.pubURI=SSK@3YEf.../ark",
			"ark.number=78",
			"auth.negTypes=2",
			"version=Fred,0.7,1.0,1466",
			"lastGoodVersion=Fred,0.7,1.0,1466",
			"volatile.freeJavaMemory=205706528",
			"EndMessage"
		);
		assertThat(nodeData.get().getVolatile("freeJavaMemory"), is("205706528"));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithoutDetails()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"EndMessage"
		);
		assertThat(configData.get(), notNullValue());
	}

	@Test
	public void defaultFcpClientCanGetConfigWithCurrent()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withCurrent().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithCurrent=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"current.foo=bar",
			"EndMessage"
		);
		assertThat(configData.get().getCurrent("foo"), is("bar"));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithDefaults()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withDefaults().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithDefaults=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"default.foo=bar",
			"EndMessage"
		);
		assertThat(configData.get().getDefault("foo"), is("bar"));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithSortOrder()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withSortOrder().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithSortOrder=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"sortOrder.foo=17",
			"EndMessage"
		);
		assertThat(configData.get().getSortOrder("foo"), is(17));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithExpertFlag()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withExpertFlag().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithExpertFlag=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"expertFlag.foo=true",
			"EndMessage"
		);
		assertThat(configData.get().getExpertFlag("foo"), is(true));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithForceWriteFlag()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withForceWriteFlag().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithForceWriteFlag=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"forceWriteFlag.foo=true",
			"EndMessage"
		);
		assertThat(configData.get().getForceWriteFlag("foo"), is(true));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithShortDescription()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withShortDescription().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithShortDescription=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"shortDescription.foo=bar",
			"EndMessage"
		);
		assertThat(configData.get().getShortDescription("foo"), is("bar"));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithLongDescription()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withLongDescription().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithLongDescription=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"longDescription.foo=bar",
			"EndMessage"
		);
		assertThat(configData.get().getLongDescription("foo"), is("bar"));
	}

	@Test
	public void defaultFcpClientCanGetConfigWithDataTypes()
	throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> configData = fcpClient.getConfig().withDataTypes().execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"GetConfig",
			"Identifier=" + identifier,
			"WithDataTypes=true",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"dataType.foo=number",
			"EndMessage"
		);
		assertThat(configData.get().getDataType("foo"), is("number"));
	}

	@Test
	public void defaultFcpClientCanModifyConfigData() throws InterruptedException, ExecutionException, IOException {
		Future<ConfigData> newConfigData = fcpClient.modifyConfig().set("foo.bar").to("baz").execute();
		connectNode();
		List<String> lines = fcpServer.collectUntil(is("EndMessage"));
		String identifier = extractIdentifier(lines);
		assertThat(lines, matchesFcpMessage(
			"ModifyConfig",
			"Identifier=" + identifier,
			"foo.bar=baz",
			"EndMessage"
		));
		fcpServer.writeLine(
			"ConfigData",
			"Identifier=" + identifier,
			"current.foo.bar=baz",
			"EndMessage"
		);
		assertThat(newConfigData.get().getCurrent("foo.bar"), is("baz"));
	}

	private List<String> lines;
	private String identifier;

	private void connectAndAssert(Supplier<Matcher<List<String>>> requestMatcher)
	throws InterruptedException, ExecutionException, IOException {
		connectNode();
		readMessage(requestMatcher);
	}

	private void readMessage(Supplier<Matcher<List<String>>> requestMatcher) throws IOException {
		lines = fcpServer.collectUntil(is("EndMessage"));
		identifier = extractIdentifier(lines);
		assertThat(lines, requestMatcher.get());
	}

	public class Peers {

		public class PeerCommands {

			public class ListPeer {

				@Test
				public void byIdentity() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.listPeer().byIdentity("id1").execute();
					connectAndAssert(() -> matchesListPeer("id1"));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void byHostAndPort() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.listPeer().byHostAndPort("host.free.net", 12345).execute();
					connectAndAssert(() -> matchesListPeer("host.free.net:12345"));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void byName() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.listPeer().byName("FriendNode").execute();
					connectAndAssert(() -> matchesListPeer("FriendNode"));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void unknownNodeIdentifier() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.listPeer().byIdentity("id2").execute();
					connectAndAssert(() -> matchesListPeer("id2"));
					replyWithUnknownNodeIdentifier();
					assertThat(peer.get().isPresent(), is(false));
				}

				private Matcher<List<String>> matchesListPeer(String nodeId) {
					return matchesFcpMessage(
						"ListPeer",
						"Identifier=" + identifier,
						"NodeIdentifier=" + nodeId,
						"EndMessage"
					);
				}

			}

			public class ListPeers {

				@Test
				public void withoutMetadataOrVolatile() throws IOException, ExecutionException, InterruptedException {
					Future<Collection<Peer>> peers = fcpClient.listPeers().execute();
					connectAndAssert(() -> matchesListPeers(false, false));
					replyWithPeer("id1");
					replyWithPeer("id2");
					sendEndOfPeerList();
					assertThat(peers.get(), hasSize(2));
					assertThat(peers.get().stream().map(Peer::getIdentity).collect(Collectors.toList()),
						containsInAnyOrder("id1", "id2"));
				}

				@Test
				public void withMetadata() throws IOException, ExecutionException, InterruptedException {
					Future<Collection<Peer>> peers = fcpClient.listPeers().includeMetadata().execute();
					connectAndAssert(() -> matchesListPeers(false, true));
					replyWithPeer("id1", "metadata.foo=bar1");
					replyWithPeer("id2", "metadata.foo=bar2");
					sendEndOfPeerList();
					assertThat(peers.get(), hasSize(2));
					assertThat(peers.get().stream().map(peer -> peer.getMetadata("foo")).collect(Collectors.toList()),
						containsInAnyOrder("bar1", "bar2"));
				}

				@Test
				public void withVolatile() throws IOException, ExecutionException, InterruptedException {
					Future<Collection<Peer>> peers = fcpClient.listPeers().includeVolatile().execute();
					connectAndAssert(() -> matchesListPeers(true, false));
					replyWithPeer("id1", "volatile.foo=bar1");
					replyWithPeer("id2", "volatile.foo=bar2");
					sendEndOfPeerList();
					assertThat(peers.get(), hasSize(2));
					assertThat(peers.get().stream().map(peer -> peer.getVolatile("foo")).collect(Collectors.toList()),
						containsInAnyOrder("bar1", "bar2"));
				}

				private Matcher<List<String>> matchesListPeers(boolean withVolatile, boolean withMetadata) {
					return matchesFcpMessage(
						"ListPeers",
						"WithVolatile=" + withVolatile,
						"WithMetadata=" + withMetadata,
						"EndMessage"
					);
				}

				private void sendEndOfPeerList() throws IOException {
					fcpServer.writeLine(
						"EndListPeers",
						"Identifier=" + identifier,
						"EndMessage"
					);
				}

			}

			public class AddPeer {

				@Test
				public void fromFile() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.addPeer().fromFile(new File("/tmp/ref.txt")).execute();
					connectAndAssert(() -> allOf(matchesAddPeer(), hasItem("File=/tmp/ref.txt")));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void fromUrl() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.addPeer().fromURL(new URL("http://node.ref/")).execute();
					connectAndAssert(() -> allOf(matchesAddPeer(), hasItem("URL=http://node.ref/")));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void fromNodeRef() throws InterruptedException, ExecutionException, IOException {
					NodeRef nodeRef = createNodeRef();
					Future<Optional<Peer>> peer = fcpClient.addPeer().fromNodeRef(nodeRef).execute();
					connectAndAssert(() -> allOf(matchesAddPeer(), Matchers.<String>hasItems(
						"myName=name",
						"ark.pubURI=public",
						"ark.number=1",
						"dsaGroup.g=base",
						"dsaGroup.p=prime",
						"dsaGroup.q=subprime",
						"dsaPubKey.y=dsa-public",
						"physical.udp=1.2.3.4:5678",
						"auth.negTypes=3;5",
						"sig=sig"
					)));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				private NodeRef createNodeRef() {
					NodeRef nodeRef = new NodeRef();
					nodeRef.setIdentity("id1");
					nodeRef.setName("name");
					nodeRef.setARK(new ARK("public", "1"));
					nodeRef.setDSAGroup(new DSAGroup("base", "prime", "subprime"));
					nodeRef.setNegotiationTypes(new int[] { 3, 5 });
					nodeRef.setPhysicalUDP("1.2.3.4:5678");
					nodeRef.setDSAPublicKey("dsa-public");
					nodeRef.setSignature("sig");
					return nodeRef;
				}

				private Matcher<List<String>> matchesAddPeer() {
					return matchesFcpMessage(
						"AddPeer",
						"Identifier=" + identifier,
						"EndMessage"
					);
				}

			}

			public class ModifyPeer {

				@Test
				public void defaultFcpClientCanEnablePeerByName()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().enable().byName("id1").execute();
					connectAndAssert(() -> matchesModifyPeer("id1", "IsDisabled", false));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void defaultFcpClientCanDisablePeerByName()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().disable().byName("id1").execute();
					connectAndAssert(() -> matchesModifyPeer("id1", "IsDisabled", true));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void defaultFcpClientCanEnablePeerByIdentity()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().enable().byIdentity("id1").execute();
					connectAndAssert(() -> matchesModifyPeer("id1", "IsDisabled", false));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void defaultFcpClientCanEnablePeerByHostAndPort()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer =
						fcpClient.modifyPeer().enable().byHostAndPort("1.2.3.4", 5678).execute();
					connectAndAssert(() -> matchesModifyPeer("1.2.3.4:5678", "IsDisabled", false));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void allowLocalAddressesOfPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer =
						fcpClient.modifyPeer().allowLocalAddresses().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "AllowLocalAddresses", true),
						not(contains(startsWith("IsDisabled=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void disallowLocalAddressesOfPeer()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer =
						fcpClient.modifyPeer().disallowLocalAddresses().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "AllowLocalAddresses", false),
						not(contains(startsWith("IsDisabled=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void setBurstOnlyForPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().setBurstOnly().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IsBurstOnly", true),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void clearBurstOnlyForPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().clearBurstOnly().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IsBurstOnly", false),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void defaultFcpClientCanSetListenOnlyForPeer()
				throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().setListenOnly().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IsListenOnly", true),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled="))),
						not(contains(startsWith("IsBurstOnly=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void clearListenOnlyForPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().clearListenOnly().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IsListenOnly", false),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled="))),
						not(contains(startsWith("IsBurstOnly=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void ignoreSourceForPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().ignoreSource().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IgnoreSourcePort", true),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled="))),
						not(contains(startsWith("IsBurstOnly="))),
						not(contains(startsWith("IsListenOnly=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void useSourceForPeer() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().useSource().byIdentity("id1").execute();
					connectAndAssert(() -> allOf(
						matchesModifyPeer("id1", "IgnoreSourcePort", false),
						not(contains(startsWith("AllowLocalAddresses="))),
						not(contains(startsWith("IsDisabled="))),
						not(contains(startsWith("IsBurstOnly="))),
						not(contains(startsWith("IsListenOnly=")))
					));
					replyWithPeer("id1");
					assertThat(peer.get().get().getIdentity(), is("id1"));
				}

				@Test
				public void unknownNode() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<Peer>> peer = fcpClient.modifyPeer().enable().byIdentity("id1").execute();
					connectAndAssert(() -> matchesModifyPeer("id1", "IsDisabled", false));
					replyWithUnknownNodeIdentifier();
					assertThat(peer.get().isPresent(), is(false));
				}

				private Matcher<List<String>> matchesModifyPeer(String nodeIdentifier, String setting, boolean value) {
					return matchesFcpMessage(
						"ModifyPeer",
						"Identifier=" + identifier,
						"NodeIdentifier=" + nodeIdentifier,
						setting + "=" + value,
						"EndMessage"
					);
				}

			}

			public class RemovePeer {

				@Test
				public void byName() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> peer = fcpClient.removePeer().byName("Friend1").execute();
					connectAndAssert(() -> matchesRemovePeer("Friend1"));
					replyWithPeerRemoved("Friend1");
					assertThat(peer.get(), is(true));
				}

				@Test
				public void invalidName() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> peer = fcpClient.removePeer().byName("NotFriend1").execute();
					connectAndAssert(() -> matchesRemovePeer("NotFriend1"));
					replyWithUnknownNodeIdentifier();
					assertThat(peer.get(), is(false));
				}

				@Test
				public void byIdentity() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> peer = fcpClient.removePeer().byIdentity("id1").execute();
					connectAndAssert(() -> matchesRemovePeer("id1"));
					replyWithPeerRemoved("id1");
					assertThat(peer.get(), is(true));
				}

				@Test
				public void byHostAndPort() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> peer = fcpClient.removePeer().byHostAndPort("1.2.3.4", 5678).execute();
					connectAndAssert(() -> matchesRemovePeer("1.2.3.4:5678"));
					replyWithPeerRemoved("Friend1");
					assertThat(peer.get(), is(true));
				}

				private Matcher<List<String>> matchesRemovePeer(String nodeIdentifier) {
					return matchesFcpMessage(
						"RemovePeer",
						"Identifier=" + identifier,
						"NodeIdentifier=" + nodeIdentifier,
						"EndMessage"
					);
				}

				private void replyWithPeerRemoved(String nodeIdentifier) throws IOException {
					fcpServer.writeLine(
						"PeerRemoved",
						"Identifier=" + identifier,
						"NodeIdentifier=" + nodeIdentifier,
						"EndMessage"
					);
				}

			}

			private void replyWithPeer(String peerId, String... additionalLines) throws IOException {
				fcpServer.writeLine(
					"Peer",
					"Identifier=" + identifier,
					"identity=" + peerId,
					"opennet=false",
					"ark.pubURI=SSK@3YEf.../ark",
					"ark.number=78",
					"auth.negTypes=2",
					"version=Fred,0.7,1.0,1466",
					"lastGoodVersion=Fred,0.7,1.0,1466"
				);
				fcpServer.writeLine(additionalLines);
				fcpServer.writeLine("EndMessage");
			}

		}

		public class PeerNoteCommands {

			public class ListPeerNotes {

				@Test
				public void onUnknownNodeIdentifier() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<PeerNote>> peerNote = fcpClient.listPeerNotes().byName("Friend1").execute();
					connectAndAssert(() -> matchesListPeerNotes("Friend1"));
					replyWithUnknownNodeIdentifier();
					assertThat(peerNote.get().isPresent(), is(false));
				}

				@Test
				public void byNodeName() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<PeerNote>> peerNote = fcpClient.listPeerNotes().byName("Friend1").execute();
					connectAndAssert(() -> matchesListPeerNotes("Friend1"));
					replyWithPeerNote();
					replyWithEndListPeerNotes();
					assertThat(peerNote.get().get().getNoteText(), is("RXhhbXBsZSBUZXh0Lg=="));
					assertThat(peerNote.get().get().getPeerNoteType(), is(1));
				}

				@Test
				public void byNodeIdentifier() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<PeerNote>> peerNote = fcpClient.listPeerNotes().byIdentity("id1").execute();
					connectAndAssert(() -> matchesListPeerNotes("id1"));
					replyWithPeerNote();
					replyWithEndListPeerNotes();
					assertThat(peerNote.get().get().getNoteText(), is("RXhhbXBsZSBUZXh0Lg=="));
					assertThat(peerNote.get().get().getPeerNoteType(), is(1));
				}

				@Test
				public void byHostAndPort() throws InterruptedException, ExecutionException, IOException {
					Future<Optional<PeerNote>> peerNote =
						fcpClient.listPeerNotes().byHostAndPort("1.2.3.4", 5678).execute();
					connectAndAssert(() -> matchesListPeerNotes("1.2.3.4:5678"));
					replyWithPeerNote();
					replyWithEndListPeerNotes();
					assertThat(peerNote.get().get().getNoteText(), is("RXhhbXBsZSBUZXh0Lg=="));
					assertThat(peerNote.get().get().getPeerNoteType(), is(1));
				}

				private Matcher<List<String>> matchesListPeerNotes(String nodeIdentifier) {
					return matchesFcpMessage(
						"ListPeerNotes",
						"NodeIdentifier=" + nodeIdentifier,
						"EndMessage"
					);
				}

				private void replyWithEndListPeerNotes() throws IOException {
					fcpServer.writeLine(
						"EndListPeerNotes",
						"Identifier=" + identifier,
						"EndMessage"
					);
				}

				private void replyWithPeerNote() throws IOException {
					fcpServer.writeLine(
						"PeerNote",
						"Identifier=" + identifier,
						"NodeIdentifier=Friend1",
						"NoteText=RXhhbXBsZSBUZXh0Lg==",
						"PeerNoteType=1",
						"EndMessage"
					);
				}

			}

			public class ModifyPeerNotes {

				@Test
				public void byName() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> noteUpdated =
						fcpClient.modifyPeerNote().darknetComment("foo").byName("Friend1").execute();
					connectAndAssert(() -> matchesModifyPeerNote("Friend1"));
					replyWithPeerNote();
					assertThat(noteUpdated.get(), is(true));
				}

				@Test
				public void onUnknownNodeIdentifier() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> noteUpdated =
						fcpClient.modifyPeerNote().darknetComment("foo").byName("Friend1").execute();
					connectAndAssert(() -> matchesModifyPeerNote("Friend1"));
					replyWithUnknownNodeIdentifier();
					assertThat(noteUpdated.get(), is(false));
				}

				@Test
				public void defaultFcpClientFailsToModifyPeerNoteWithoutPeerNote()
				throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> noteUpdated = fcpClient.modifyPeerNote().byName("Friend1").execute();
					assertThat(noteUpdated.get(), is(false));
				}

				@Test
				public void byIdentifier() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> noteUpdated =
						fcpClient.modifyPeerNote().darknetComment("foo").byIdentifier("id1").execute();
					connectAndAssert(() -> matchesModifyPeerNote("id1"));
					replyWithPeerNote();
					assertThat(noteUpdated.get(), is(true));
				}

				@Test
				public void byHostAndPort() throws InterruptedException, ExecutionException, IOException {
					Future<Boolean> noteUpdated =
						fcpClient.modifyPeerNote().darknetComment("foo").byHostAndPort("1.2.3.4", 5678).execute();
					connectAndAssert(() -> matchesModifyPeerNote("1.2.3.4:5678"));
					replyWithPeerNote();
					assertThat(noteUpdated.get(), is(true));
				}

				private Matcher<List<String>> matchesModifyPeerNote(String nodeIdentifier) {
					return matchesFcpMessage(
						"ModifyPeerNote",
						"Identifier=" + identifier,
						"NodeIdentifier=" + nodeIdentifier,
						"PeerNoteType=1",
						"NoteText=Zm9v",
						"EndMessage"
					);
				}

				private void replyWithPeerNote() throws IOException {
					fcpServer.writeLine(
						"PeerNote",
						"Identifier=" + identifier,
						"NodeIdentifier=Friend1",
						"NoteText=Zm9v",
						"PeerNoteType=1",
						"EndMessage"
					);
				}

			}

		}

		private void replyWithUnknownNodeIdentifier() throws IOException {
			fcpServer.writeLine(
				"UnknownNodeIdentifier",
				"Identifier=" + identifier,
				"NodeIdentifier=id2",
				"EndMessage"
			);
		}

	}

	public class PluginCommands {

		private static final String CLASS_NAME = "foo.plugin.Plugin";

		private void replyWithPluginInfo() throws IOException {
			fcpServer.writeLine(
				"PluginInfo",
				"Identifier=" + identifier,
				"PluginName=superPlugin",
				"IsTalkable=true",
				"LongVersion=1.2.3",
				"Version=42",
				"OriginUri=superPlugin",
				"Started=true",
				"EndMessage"
			);
		}

		private void verifyPluginInfo(Future<Optional<PluginInfo>> pluginInfo)
		throws InterruptedException, ExecutionException {
			assertThat(pluginInfo.get().get().getPluginName(), is("superPlugin"));
			assertThat(pluginInfo.get().get().getOriginalURI(), is("superPlugin"));
			assertThat(pluginInfo.get().get().isTalkable(), is(true));
			assertThat(pluginInfo.get().get().getVersion(), is("42"));
			assertThat(pluginInfo.get().get().getLongVersion(), is("1.2.3"));
			assertThat(pluginInfo.get().get().isStarted(), is(true));
		}

		public class LoadPlugin {

			public class OfficialPlugins {

				@Test
				public void fromFreenet() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo =
						fcpClient.loadPlugin().officialFromFreenet("superPlugin").execute();
					connectAndAssert(() -> createMatcherForOfficialSource("freenet"));
					assertThat(lines, not(contains(startsWith("Store="))));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				@Test
				public void persistentFromFreenet() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo =
						fcpClient.loadPlugin().addToConfig().officialFromFreenet("superPlugin").execute();
					connectAndAssert(() -> createMatcherForOfficialSource("freenet"));
					assertThat(lines, hasItem("Store=true"));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				@Test
				public void fromHttps() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo =
						fcpClient.loadPlugin().officialFromHttps("superPlugin").execute();
					connectAndAssert(() -> createMatcherForOfficialSource("https"));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				private Matcher<List<String>> createMatcherForOfficialSource(String officialSource) {
					return matchesFcpMessage(
						"LoadPlugin",
						"Identifier=" + identifier,
						"PluginURL=superPlugin",
						"URLType=official",
						"OfficialSource=" + officialSource,
						"EndMessage"
					);
				}

			}

			public class FromOtherSources {

				private static final String FILE_PATH = "/path/to/plugin.jar";
				private static final String URL = "http://server.com/plugin.jar";
				private static final String KEY = "KSK@plugin.jar";

				@Test
				public void fromFile() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo = fcpClient.loadPlugin().fromFile(FILE_PATH).execute();
					connectAndAssert(() -> createMatcher("file", FILE_PATH));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				@Test
				public void fromUrl() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo = fcpClient.loadPlugin().fromUrl(URL).execute();
					connectAndAssert(() -> createMatcher("url", URL));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				@Test
				public void fromFreenet() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo = fcpClient.loadPlugin().fromFreenet(KEY).execute();
					connectAndAssert(() -> createMatcher("freenet", KEY));
					replyWithPluginInfo();
					verifyPluginInfo(pluginInfo);
				}

				private Matcher<List<String>> createMatcher(String urlType, String url) {
					return matchesFcpMessage(
						"LoadPlugin",
						"Identifier=" + identifier,
						"PluginURL=" + url,
						"URLType=" + urlType,
						"EndMessage"
					);
				}

			}

			public class Failed {

				@Test
				public void failedLoad() throws ExecutionException, InterruptedException, IOException {
					Future<Optional<PluginInfo>> pluginInfo =
						fcpClient.loadPlugin().officialFromFreenet("superPlugin").execute();
					connectAndAssert(() -> matchesFcpMessage("LoadPlugin", "EndMessage"));
					replyWithProtocolError();
					assertThat(pluginInfo.get().isPresent(), is(false));
				}

			}

		}

		private void replyWithProtocolError() throws IOException {
			fcpServer.writeLine(
				"ProtocolError",
				"Identifier=" + identifier,
				"EndMessage"
			);
		}

		public class ReloadPlugin {

			@Test
			public void reloadingPluginWorks() throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo = fcpClient.reloadPlugin().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> matchReloadPluginMessage());
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			@Test
			public void reloadingPluginWithMaxWaitTimeWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo =
					fcpClient.reloadPlugin().waitFor(1234).plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchReloadPluginMessage(), hasItem("MaxWaitTime=1234")));
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			@Test
			public void reloadingPluginWithPurgeWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo =
					fcpClient.reloadPlugin().purge().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchReloadPluginMessage(), hasItem("Purge=true")));
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			@Test
			public void reloadingPluginWithStoreWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo =
					fcpClient.reloadPlugin().addToConfig().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchReloadPluginMessage(), hasItem("Store=true")));
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			private Matcher<List<String>> matchReloadPluginMessage() {
				return matchesFcpMessage(
					"ReloadPlugin",
					"Identifier=" + identifier,
					"PluginName=" + CLASS_NAME,
					"EndMessage"
				);
			}

		}

		public class RemovePlugin {

			@Test
			public void removingPluginWorks() throws InterruptedException, ExecutionException, IOException {
				Future<Boolean> pluginRemoved = fcpClient.removePlugin().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> matchPluginRemovedMessage());
				replyWithPluginRemoved();
				assertThat(pluginRemoved.get(), is(true));
			}

			@Test
			public void removingPluginWithMaxWaitTimeWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Boolean> pluginRemoved = fcpClient.removePlugin().waitFor(1234).plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchPluginRemovedMessage(), hasItem("MaxWaitTime=1234")));
				replyWithPluginRemoved();
				assertThat(pluginRemoved.get(), is(true));
			}

			@Test
			public void removingPluginWithPurgeWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Boolean> pluginRemoved = fcpClient.removePlugin().purge().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchPluginRemovedMessage(), hasItem("Purge=true")));
				replyWithPluginRemoved();
				assertThat(pluginRemoved.get(), is(true));
			}

			private void replyWithPluginRemoved() throws IOException {
				fcpServer.writeLine(
					"PluginRemoved",
					"Identifier=" + identifier,
					"PluginName=" + CLASS_NAME,
					"EndMessage"
				);
			}

			private Matcher<List<String>> matchPluginRemovedMessage() {
				return matchesFcpMessage(
					"RemovePlugin",
					"Identifier=" + identifier,
					"PluginName=" + CLASS_NAME,
					"EndMessage"
				);
			}

		}

		public class GetPluginInfo {

			@Test
			public void gettingPluginInfoWorks() throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo = fcpClient.getPluginInfo().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> matchGetPluginInfoMessage());
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			@Test
			public void gettingPluginInfoWithDetailsWorks()
			throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo =
					fcpClient.getPluginInfo().detailed().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchGetPluginInfoMessage(), hasItem("Detailed=true")));
				replyWithPluginInfo();
				verifyPluginInfo(pluginInfo);
			}

			@Test
			public void protocolErrorIsRecognizedAsFailure()
			throws InterruptedException, ExecutionException, IOException {
				Future<Optional<PluginInfo>> pluginInfo =
					fcpClient.getPluginInfo().detailed().plugin(CLASS_NAME).execute();
				connectAndAssert(() -> allOf(matchGetPluginInfoMessage(), hasItem("Detailed=true")));
				replyWithProtocolError();
				assertThat(pluginInfo.get(), is(Optional.empty()));
			}

			private Matcher<List<String>> matchGetPluginInfoMessage() {
				return matchesFcpMessage(
					"GetPluginInfo",
					"Identifier=" + identifier,
					"PluginName=" + CLASS_NAME,
					"EndMessage"
				);
			}

		}

	}

	public class UskSubscriptionCommands {

		private static final String URI = "USK@some,uri/file.txt";

		@Test
		public void subscriptionWorks() throws InterruptedException, ExecutionException, IOException {
			Future<Optional<UskSubscription>> uskSubscription = fcpClient.subscribeUsk().uri(URI).execute();
			connectAndAssert(() -> matchesFcpMessage("SubscribeUSK", "URI=" + URI, "EndMessage"));
			replyWithSubscribed();
			assertThat(uskSubscription.get().get().getUri(), is(URI));
			AtomicInteger edition = new AtomicInteger();
			CountDownLatch updated = new CountDownLatch(2);
			uskSubscription.get().get().onUpdate(e -> {
				edition.set(e);
				updated.countDown();
			});
			sendUpdateNotification(23);
			sendUpdateNotification(24);
			assertThat("updated in time", updated.await(5, TimeUnit.SECONDS), is(true));
			assertThat(edition.get(), is(24));
		}

		@Test
		public void subscriptionUpdatesMultipleTimes() throws InterruptedException, ExecutionException, IOException {
			Future<Optional<UskSubscription>> uskSubscription = fcpClient.subscribeUsk().uri(URI).execute();
			connectAndAssert(() -> matchesFcpMessage("SubscribeUSK", "URI=" + URI, "EndMessage"));
			replyWithSubscribed();
			assertThat(uskSubscription.get().get().getUri(), is(URI));
			AtomicInteger edition = new AtomicInteger();
			CountDownLatch updated = new CountDownLatch(2);
			uskSubscription.get().get().onUpdate(e -> {
				edition.set(e);
				updated.countDown();
			});
			uskSubscription.get().get().onUpdate(e -> updated.countDown());
			sendUpdateNotification(23);
			assertThat("updated in time", updated.await(5, TimeUnit.SECONDS), is(true));
			assertThat(edition.get(), is(23));
		}

		@Test
		public void subscriptionCanBeCancelled() throws InterruptedException, ExecutionException, IOException {
			Future<Optional<UskSubscription>> uskSubscription = fcpClient.subscribeUsk().uri(URI).execute();
			connectAndAssert(() -> matchesFcpMessage("SubscribeUSK", "URI=" + URI, "EndMessage"));
			replyWithSubscribed();
			assertThat(uskSubscription.get().get().getUri(), is(URI));
			AtomicBoolean updated = new AtomicBoolean();
			uskSubscription.get().get().onUpdate(e -> updated.set(true));
			uskSubscription.get().get().cancel();
			readMessage(() -> matchesFcpMessage("UnsubscribeUSK", "Identifier=" + identifier, "EndMessage"));
			sendUpdateNotification(23);
			assertThat(updated.get(), is(false));
		}

		private void replyWithSubscribed() throws IOException {
			fcpServer.writeLine(
				"SubscribedUSK",
				"Identifier=" + identifier,
				"URI=" + URI,
				"DontPoll=false",
				"EndMessage"
			);
		}

		private void sendUpdateNotification(int edition, String... additionalLines) throws IOException {
			fcpServer.writeLine(
				"SubscribedUSKUpdate",
				"Identifier=" + identifier,
				"URI=" + URI,
				"Edition=" + edition
			);
			fcpServer.writeLine(additionalLines);
			fcpServer.writeLine("EndMessage");
		}

	}

}
