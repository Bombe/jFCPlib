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

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link FcpClient} implementation.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class DefaultFcpClient implements FcpClient {

	private final ListeningExecutorService threadPool;
	private final String hostname;
	private final int port;
	private final AtomicReference<FcpConnection> fcpConnection = new AtomicReference<>();
	private final Supplier<String> clientName;

	public DefaultFcpClient(ExecutorService threadPool, String hostname, int port, Supplier<String> clientName) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.hostname = hostname;
		this.port = port;
		this.clientName = clientName;
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
		FcpReplySequence<?> nodeHelloSequence = new ClientHelloReplySequence(connection);
		ClientHello clientHello = new ClientHello(clientName.get(), "2.0");
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

	@Override
	public ClientPutCommand clientPut() {
		return new ClientPutCommandImpl(threadPool, this::connect);
	}

	private class ClientHelloReplySequence extends FcpReplySequence<Void> {

		private final AtomicReference<NodeHello> receivedNodeHello;
		private final AtomicBoolean receivedClosed;

		public ClientHelloReplySequence(FcpConnection connection) {
			super(DefaultFcpClient.this.threadPool, connection);
			receivedNodeHello = new AtomicReference<>();
			receivedClosed = new AtomicBoolean();
		}

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

	}

}

