package net.pterodactylus.fcp.quelaton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.pterodactylus.fcp.FcpKeyPair;
import net.pterodactylus.fcp.fake.FakeTcpServer;

import org.junit.After;
import org.junit.Test;

/**
 * Unit test for {@link DefaultFcpClient}.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class DefaultFcpClientTest {

	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final FakeTcpServer fcpServer;
	private final DefaultFcpClient fcpClient;

	public DefaultFcpClientTest() throws IOException {
		fcpServer = new FakeTcpServer(threadPool);
		fcpClient = new DefaultFcpClient(threadPool, "localhost", fcpServer.getPort(), () -> "Test", () -> "2.0");
	}

	@After
	public void tearDown() throws IOException {
		fcpServer.close();
	}

	@Test
	public void defaultFcpClientCanGenerateKeypair() throws ExecutionException, InterruptedException, IOException {
		Future<FcpKeyPair> keyPairFuture = fcpClient.generateKeypair().execute();
		connectNode();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.writeLine("SSKKeypair\n"
				+ "InsertURI=freenet:SSK@AKTTKG6YwjrHzWo67laRcoPqibyiTdyYufjVg54fBlWr,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM/\n"
				+ "RequestURI=freenet:SSK@BnHXXv3Fa43w~~iz1tNUd~cj4OpUuDjVouOWZ5XlpX0,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM,AQABAAE/\n"
				+ "Identifier=My Identifier from GenerateSSK\n"
				+ "EndMessage");
		FcpKeyPair keyPair = keyPairFuture.get();
		assertThat(keyPair.getPublicKey(),
				is("freenet:SSK@BnHXXv3Fa43w~~iz1tNUd~cj4OpUuDjVouOWZ5XlpX0,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM,AQABAAE/"));
		assertThat(keyPair.getPrivateKey(), is(
				"freenet:SSK@AKTTKG6YwjrHzWo67laRcoPqibyiTdyYufjVg54fBlWr,AwUSJG5ZS-FDZTqnt6skTzhxQe08T-fbKXj8aEHZsXM/"));
	}

	private void connectNode() throws InterruptedException, ExecutionException, IOException {
		fcpServer.connect().get();
		fcpServer.collectUntil(is("EndMessage"));
		fcpServer.writeLine("NodeHello\n"
				+ "FCPVersion=2.0\n"
				+ "ConnectionIdentifier=754595fc35701d76096d8279d15c57e6\n"
				+ "Version=Fred,0.7,1.0,1231\n"
				+ "Node=Fred\n"
				+ "NodeLanguage=ENGLISH\n"
				+ "ExtRevision=23771\n"
				+ "Build=1231\n"
				+ "Testnet=false\n"
				+ "ExtBuild=26\n"
				+ "CompressionCodecs=3 - GZIP(0), BZIP2(1), LZMA(2)\n"
				+ "Revision=@custom@\n"
				+ "EndMessage");
	}

}
