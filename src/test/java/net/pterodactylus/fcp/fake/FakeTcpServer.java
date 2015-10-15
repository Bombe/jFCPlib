package net.pterodactylus.fcp.fake;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matcher;

/**
 * TODO
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class FakeTcpServer implements AutoCloseable {

	private final ServerSocket serverSocket;
	private final ExecutorService executorService;
	private final AtomicReference<TextSocket> clientSocket = new AtomicReference<>();

	public FakeTcpServer(ExecutorService executorService) throws IOException {
		this.executorService = executorService;
		this.serverSocket = new ServerSocket(0);
	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public Future<?> connect() throws IOException {
		return executorService.submit(() -> {
			clientSocket.set(new TextSocket(serverSocket.accept()));
			return null;
		});
	}

	public List<String> collectUntil(Matcher<String> lineMatcher) throws IOException {
		return clientSocket.get().collectUntil(lineMatcher);
	}

	public void writeLine(String... lines) throws IOException {
		for (String line : lines) {
			clientSocket.get().writeLine(line);
		}
	}

	public String readLine() throws IOException {
		return clientSocket.get().readLine();
	}

	@Override
	public void close() throws IOException {
		TextSocket textSocket = clientSocket.get();
		if (textSocket != null) {
			textSocket.close();
		}
	}

}
