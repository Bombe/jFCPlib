package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.NodeHello;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Internal <code>ClientHello</code> implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ClientHelloImpl {

	private final ListeningExecutorService threadPool;
	private final String hostname;
	private final int port;
	private final AtomicReference<String> clientName = new AtomicReference<>();

	public ClientHelloImpl(ExecutorService threadPool, String hostname, int port) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.hostname = hostname;
		this.port = port;
	}

	public Executable<FcpConnection> withName(String name) {
		clientName.set(name);
		return this::execute;
	}

	private ListenableFuture<FcpConnection> execute() {
		return threadPool.submit(this::establishConnection);
	}

	private FcpConnection establishConnection() throws IOException {
		FcpConnection connection = new FcpConnection(hostname, port);
		connection.connect();
		ClientHello clientHello = new ClientHello(clientName.get(), "2.0");
		try (ClientHelloDialog clientHelloDialog = new ClientHelloDialog(connection)) {
			if (clientHelloDialog.send(clientHello).get()) {
				return connection;
			}
		} catch (InterruptedException | ExecutionException e) {
			connection.close();
			throw new IOException(String.format("Could not connect to %s:%d.", hostname, port), e);
		}
		connection.close();
		throw new IOException(String.format("Could not connect to %s:%d.", hostname, port));
	}

	private class ClientHelloDialog extends FcpDialog<Boolean> {

		public ClientHelloDialog(FcpConnection connection) {
			super(ClientHelloImpl.this.threadPool, connection, false);
		}

		@Override
		protected void consumeNodeHello(NodeHello nodeHello) {
			setResult(true);
		}

	}

}
