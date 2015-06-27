package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpKeyPair;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.SSKKeypair;

/**
 * Default {@link FcpClient} implementation.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class DefaultFcpClient implements FcpClient {

	private final ExecutorService threadPool;
	private final String hostname;
	private final int port;
	private final AtomicReference<FcpConnection> fcpConnection = new AtomicReference<>();
	private final Supplier<String> clientName;
	private final Supplier<String> expectedVersion;

	public DefaultFcpClient(ExecutorService threadPool, String hostname, int port, Supplier<String> clientName,
			Supplier<String> expectedVersion) {
		this.threadPool = threadPool;
		this.hostname = hostname;
		this.port = port;
		this.clientName = clientName;
		this.expectedVersion = expectedVersion;
	}

	private void connect() throws IOException {
		if (fcpConnection.get() != null) {
			return;
		}
		fcpConnection.compareAndSet(null, createConnection());
	}

	private FcpConnection createConnection() throws IOException {
		FcpConnection connection = new FcpConnection(hostname, port);
		connection.connect();
		AtomicReference<NodeHello> receivedNodeHello = new AtomicReference<>();
		AtomicBoolean receivedClosed = new AtomicBoolean();
		FcpReplySequence nodeHelloSequence = new FcpReplySequence(threadPool, connection);
		nodeHelloSequence
				.handle(NodeHello.class)
				.with((nodeHello) -> receivedNodeHello.set(nodeHello));
		nodeHelloSequence
				.handle(CloseConnectionDuplicateClientName.class)
				.with((closeConnection) -> receivedClosed.set(true));
		nodeHelloSequence.waitFor(() -> receivedNodeHello.get() != null || receivedClosed.get());
		ClientHello clientHello = new ClientHello(clientName.get(), expectedVersion.get());
		try {
			nodeHelloSequence.send(clientHello).get();
		} catch (InterruptedException | ExecutionException e) {
			connection.close();
			throw new IOException(String.format("Could not connect to %s:%d.", hostname, port), e);
		}
		return connection;
	}

	@Override
	public GenerateKeypairCommand generateKeypair() {
		return new GenerateKeypairCommandImpl();
	}

	private class GenerateKeypairCommandImpl implements GenerateKeypairCommand {

		@Override
		public Future<FcpKeyPair> execute() {
			return threadPool.submit(() -> {
				connect();
				GenerateSSK generateSSK = new GenerateSSK();
				AtomicReference<FcpKeyPair> keyPair = new AtomicReference<>();
				FcpReplySequence replySequence = new FcpReplySequence(threadPool, fcpConnection.get());
				replySequence.handle(SSKKeypair.class)
						.with((message) -> keyPair.set(
								new FcpKeyPair(message.getRequestURI(), message.getInsertURI())));
				replySequence.waitFor(() -> keyPair.get() != null);
				replySequence.send(generateSSK).get();
				return keyPair.get();
			});
		}

	}

}
