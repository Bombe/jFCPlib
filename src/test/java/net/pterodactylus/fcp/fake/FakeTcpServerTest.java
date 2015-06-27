package net.pterodactylus.fcp.fake;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

/**
 * TODO
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class FakeTcpServerTest {

	private final ExecutorService sameThread = Executors.newSingleThreadExecutor();
	private final FakeTcpServer tcpServer;

	public FakeTcpServerTest() throws IOException {
		this.tcpServer = new FakeTcpServer(sameThread);
	}

	@Test
	public void testConnect() throws IOException, ExecutionException, InterruptedException {
		ProxySelector.setDefault(new ProxySelector() {
			@Override
			public List<Proxy> select(URI uri) {
				return asList(Proxy.NO_PROXY);
			}

			@Override
			public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			}
		});
		tcpServer.connect();
		try (TextSocket clientSocket = new TextSocket(new Socket("127.0.0.1", tcpServer.getPort()))) {
			clientSocket.writeLine("Hello");
			clientSocket.writeLine("Bye");
			List<String> receivedLines = tcpServer.collectUntil(is("Bye"));
			assertThat(receivedLines, contains("Hello", "Bye"));
			tcpServer.writeLine("Yes");
			tcpServer.writeLine("Quit");
			receivedLines = clientSocket.collectUntil(is("Quit"));
			assertThat(receivedLines, contains("Yes", "Quit"));
		}
	}

}
