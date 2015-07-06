package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.NodeHello;

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

	private FcpConnection connect() throws IOException {
		FcpConnection fcpConnection = this.fcpConnection.get();
		if (fcpConnection != null) {
			return fcpConnection;
		}
		fcpConnection = createConnection();
		this.fcpConnection.compareAndSet(null, fcpConnection);
		return fcpConnection;
	}

	private FcpConnection createConnection() throws IOException {
		FcpConnection connection = new FcpConnection(hostname, port);
		connection.connect();
		FcpReplySequence<?> nodeHelloSequence = new FcpReplySequence<Void>(threadPool, connection) {
			private final AtomicReference<NodeHello> receivedNodeHello = new AtomicReference<>();
			private final AtomicBoolean receivedClosed = new AtomicBoolean();
			@Override
			protected boolean isFinished() {
				return receivedNodeHello.get() != null || receivedClosed.get();
			}

			@Override
			protected void consumeNodeHello(NodeHello nodeHello) {
				receivedNodeHello.set(nodeHello);
			}

			@Override
			protected void consumeCloseConnectionDuplicateClientName(
				CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
				receivedClosed.set(true);
			}
		};
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
		return new GenerateKeypairCommandImpl(threadPool, this::connect);
	}

	@Override
	public ClientGetCommand clientGet() {
		return new ClientGetCommandImpl(threadPool, this::connect);
	}

}

