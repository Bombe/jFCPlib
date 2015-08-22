package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.FcpConnection;

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
	private final ActiveSubscriptions activeSubscriptions = new ActiveSubscriptions(this::unsubscribeUsk);

	public DefaultFcpClient(ExecutorService threadPool, String hostname, int port, Supplier<String> clientName) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.hostname = hostname;
		this.port = port;
		this.clientName = clientName;
	}

	private FcpConnection connect() throws IOException {
		FcpConnection fcpConnection = this.fcpConnection.get();
		if ((fcpConnection != null) && !fcpConnection.isClosed()) {
			return fcpConnection;
		}
		fcpConnection = createConnection();
		this.fcpConnection.set(fcpConnection);
		try {
			activeSubscriptions.renew(fcpConnection::addFcpListener, this::subscribeUsk);
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
		return fcpConnection;
	}

	private FcpConnection createConnection() throws IOException {
		try {
			return new ClientHelloImpl(threadPool, hostname, port).withName(clientName.get()).execute().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public GetNodeCommand getNode() {
		return new GetNodeCommandImpl(threadPool, this::connect);
	}

	@Override
	public GetConfigCommand getConfig() {
		return new GetConfigCommandImpl(threadPool, this::connect);
	}

	@Override
	public ModifyConfigCommand modifyConfig() {
		return new ModifyConfigCommandImpl(threadPool, this::connect);
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

	@Override
	public ListPeerCommand listPeer() {
		return new ListPeerCommandImpl(threadPool, this::connect);
	}

	@Override
	public ListPeersCommand listPeers() {
		return new ListPeersCommandImpl(threadPool, this::connect);
	}

	@Override
	public AddPeerCommand addPeer() {
		return new AddPeerCommandImpl(threadPool, this::connect);
	}

	@Override
	public ModifyPeerCommand modifyPeer() {
		return new ModifyPeerCommandImpl(threadPool, this::connect);
	}

	@Override
	public RemovePeerCommand removePeer() {
		return new RemovePeerCommandImpl(threadPool, this::connect);
	}

	@Override
	public ListPeerNotesCommand listPeerNotes() {
		return new ListPeerNotesCommandImpl(threadPool, this::connect);
	}

	@Override
	public ModifyPeerNoteCommand modifyPeerNote() {
		return new ModifyPeerNoteCommandImpl(threadPool, this::connect);
	}

	@Override
	public LoadPluginCommand loadPlugin() {
		return new LoadPluginCommandImpl(threadPool, this::connect);
	}

	@Override
	public ReloadPluginCommand reloadPlugin() {
		return new ReloadPluginCommandImpl(threadPool, this::connect);
	}

	@Override
	public RemovePluginCommand removePlugin() {
		return new RemovePluginCommandImpl(threadPool, this::connect);
	}

	@Override
	public GetPluginInfoCommand getPluginInfo() {
		return new GetPluginInfoCommandImpl(threadPool, this::connect);
	}

	@Override
	public SubscribeUskCommand subscribeUsk() {
		return new SubscribeUskCommandImpl(threadPool, this::connect, activeSubscriptions);
	}

	private UnsubscribeUskCommand unsubscribeUsk() {
		return new UnsubscribeUskCommandImpl(threadPool, this::connect);
	}

}

