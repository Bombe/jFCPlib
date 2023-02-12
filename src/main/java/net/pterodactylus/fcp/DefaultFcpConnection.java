/*
 * jFCPlib - FcpConnection.java - Copyright © 2008–2023 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.fcp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.fcp.io.TempInputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default {@link FcpConnection} implementation.
 */
public class DefaultFcpConnection implements FcpConnection {

	/** Logger. */
	private static final Logger logger = Logger.getLogger(DefaultFcpConnection.class.getName());

	/** The default port for FCP v2. */
	public static final int DEFAULT_PORT = 9481;

	/** Listener management. */
	private final FcpListenerManager fcpListenerManager = new FcpListenerManager(this);

	/** The address of the node. */
	private final InetAddress address;

	/** The port number of the node’s FCP port. */
	private final int port;

	/** The remote socket. */
	private Socket remoteSocket;

	/** The input stream from the node. */
	private InputStream remoteInputStream;

	/** The output stream to the node. */
	private OutputStream remoteOutputStream;

	/** The connection handler. */
	private FcpConnectionHandler connectionHandler;

	/** Incoming message statistics. */
	private static final Map<String, Integer> incomingMessageStatistics = Collections.synchronizedMap(new HashMap<>());

	/**
	 * Creates a new FCP connection to the freenet node running on localhost,
	 * using the default port.
	 *
	 * @throws UnknownHostException
	 *             if the hostname can not be resolved
	 */
	public DefaultFcpConnection() throws UnknownHostException {
		this(InetAddress.getLocalHost());
	}

	/**
	 * Creates a new FCP connection to the Freenet node running on the given
	 * host, listening on the default port.
	 *
	 * @param host
	 *            The hostname of the Freenet node
	 * @throws UnknownHostException
	 *             if <code>host</code> can not be resolved
	 */
	public DefaultFcpConnection(String host) throws UnknownHostException {
		this(host, DEFAULT_PORT);
	}

	/**
	 * Creates a new FCP connection to the Freenet node running on the given
	 * host, listening on the given port.
	 *
	 * @param host
	 *            The hostname of the Freenet node
	 * @param port
	 *            The port number of the node’s FCP port
	 * @throws UnknownHostException
	 *             if <code>host</code> can not be resolved
	 */
	public DefaultFcpConnection(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	/**
	 * Creates a new FCP connection to the Freenet node running at the given
	 * address, listening on the default port.
	 *
	 * @param address
	 *            The address of the Freenet node
	 */
	public DefaultFcpConnection(InetAddress address) {
		this(address, DEFAULT_PORT);
	}

	/**
	 * Creates a new FCP connection to the Freenet node running at the given
	 * address, listening on the given port.
	 *
	 * @param address
	 *            The address of the Freenet node
	 * @param port
	 *            The port number of the node’s FCP port
	 */
	public DefaultFcpConnection(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	//
	// LISTENER MANAGEMENT
	//

	@Override
	public void addFcpListener(FcpListener fcpListener) {
		fcpListenerManager.addListener(fcpListener);
	}

	@Override
	public void removeFcpListener(FcpListener fcpListener) {
		fcpListenerManager.removeListener(fcpListener);
	}

	@Override
	public synchronized boolean isClosed() {
		return connectionHandler == null;
	}

	//
	// ACTIONS
	//

	@Override
	public synchronized void connect() throws IOException, IllegalStateException {
		if (connectionHandler != null) {
			throw new IllegalStateException("already connected, disconnect first");
		}
		logger.info("connecting to " + address + ":" + port + "…");
		remoteSocket = new Socket(address, port);
		remoteInputStream = remoteSocket.getInputStream();
		remoteOutputStream = remoteSocket.getOutputStream();
		new Thread(connectionHandler = new FcpConnectionHandler(remoteInputStream)).start();
	}

	@Override
	@Deprecated
	public synchronized void disconnect() {
		close();
	}

	@Override
	public void close() {
		handleDisconnect(null);
	}

	@Override
	public synchronized void sendMessage(FcpMessage fcpMessage) throws IOException {
		logger.fine("sending message: " + fcpMessage.getName());
		fcpMessage.write(remoteOutputStream);
	}

	//
	// PACKAGE-PRIVATE METHODS
	//

	/**
	 * Handles the given message, notifying listeners. This message should only
	 * be called by {@link FcpConnectionHandler}.
	 *
	 * @param fcpMessage
	 *            The received message
	 */
	private void handleMessage(FcpMessage fcpMessage) throws IOException{
		logger.fine("received message: " + fcpMessage.getName());
		String messageName = fcpMessage.getName();
		countMessage(messageName);
		if ("SimpleProgress".equals(messageName)) {
			fcpListenerManager.fireReceivedSimpleProgress(new SimpleProgress(fcpMessage));
		} else if ("ProtocolError".equals(messageName)) {
			fcpListenerManager.fireReceivedProtocolError(new ProtocolError(fcpMessage));
		} else if ("PersistentGet".equals(messageName)) {
			fcpListenerManager.fireReceivedPersistentGet(new PersistentGet(fcpMessage));
		} else if ("PersistentPut".equals(messageName)) {
			fcpListenerManager.fireReceivedPersistentPut(new PersistentPut(fcpMessage));
		} else if ("PersistentPutDir".equals(messageName)) {
			fcpListenerManager.fireReceivedPersistentPutDir(new PersistentPutDir(fcpMessage));
		} else if ("URIGenerated".equals(messageName)) {
			fcpListenerManager.fireReceivedURIGenerated(new URIGenerated(fcpMessage));
		} else if ("EndListPersistentRequests".equals(messageName)) {
			fcpListenerManager.fireReceivedEndListPersistentRequests(new EndListPersistentRequests(fcpMessage));
		} else if ("Peer".equals(messageName)) {
			fcpListenerManager.fireReceivedPeer(new Peer(fcpMessage));
		} else if ("PeerNote".equals(messageName)) {
			fcpListenerManager.fireReceivedPeerNote(new PeerNote(fcpMessage));
		} else if ("StartedCompression".equals(messageName)) {
			fcpListenerManager.fireReceivedStartedCompression(new StartedCompression(fcpMessage));
		} else if ("FinishedCompression".equals(messageName)) {
			fcpListenerManager.fireReceivedFinishedCompression(new FinishedCompression(fcpMessage));
		} else if ("GetFailed".equals(messageName)) {
			fcpListenerManager.fireReceivedGetFailed(new GetFailed(fcpMessage));
		} else if ("PutFetchable".equals(messageName)) {
			fcpListenerManager.fireReceivedPutFetchable(new PutFetchable(fcpMessage));
		} else if ("PutSuccessful".equals(messageName)) {
			fcpListenerManager.fireReceivedPutSuccessful(new PutSuccessful(fcpMessage));
		} else if ("PutFailed".equals(messageName)) {
			fcpListenerManager.fireReceivedPutFailed(new PutFailed(fcpMessage));
		} else if ("DataFound".equals(messageName)) {
			fcpListenerManager.fireReceivedDataFound(new DataFound(fcpMessage));
		} else if ("SubscribedUSKUpdate".equals(messageName)) {
			fcpListenerManager.fireReceivedSubscribedUSKUpdate(new SubscribedUSKUpdate(fcpMessage));
		} else if ("SubscribedUSK".equals(messageName)) {
			fcpListenerManager.fireReceivedSubscribedUSK(new SubscribedUSK(fcpMessage));
		} else if ("IdentifierCollision".equals(messageName)) {
			fcpListenerManager.fireReceivedIdentifierCollision(new IdentifierCollision(fcpMessage));
		} else if ("AllData".equals(messageName)) {
			InputStream payloadInputStream = getInputStream(FcpUtils.safeParseLong(fcpMessage.getField("DataLength")));
			fcpListenerManager.fireReceivedAllData(new AllData(fcpMessage, payloadInputStream));
		} else if ("EndListPeerNotes".equals(messageName)) {
			fcpListenerManager.fireReceivedEndListPeerNotes(new EndListPeerNotes(fcpMessage));
		} else if ("EndListPeers".equals(messageName)) {
			fcpListenerManager.fireReceivedEndListPeers(new EndListPeers(fcpMessage));
		} else if ("SSKKeypair".equals(messageName)) {
			fcpListenerManager.fireReceivedSSKKeypair(new SSKKeypair(fcpMessage));
		} else if ("PeerRemoved".equals(messageName)) {
			fcpListenerManager.fireReceivedPeerRemoved(new PeerRemoved(fcpMessage));
		} else if ("PersistentRequestModified".equals(messageName)) {
			fcpListenerManager.fireReceivedPersistentRequestModified(new PersistentRequestModified(fcpMessage));
		} else if ("PersistentRequestRemoved".equals(messageName)) {
			fcpListenerManager.fireReceivedPersistentRequestRemoved(new PersistentRequestRemoved(fcpMessage));
		} else if ("UnknownPeerNoteType".equals(messageName)) {
			fcpListenerManager.fireReceivedUnknownPeerNoteType(new UnknownPeerNoteType(fcpMessage));
		} else if ("UnknownNodeIdentifier".equals(messageName)) {
			fcpListenerManager.fireReceivedUnknownNodeIdentifier(new UnknownNodeIdentifier(fcpMessage));
		} else if ("FCPPluginReply".equals(messageName)) {
			InputStream payloadInputStream = getInputStream(FcpUtils.safeParseLong(fcpMessage.getField("DataLength"), 0));
			fcpListenerManager.fireReceivedFCPPluginReply(new FCPPluginReply(fcpMessage, payloadInputStream));
		} else if ("PluginInfo".equals(messageName)) {
			fcpListenerManager.fireReceivedPluginInfo(new PluginInfo(fcpMessage));
		} else if ("PluginRemoved".equals(messageName)) {
			fcpListenerManager.fireReceivedPluginRemoved(new PluginRemoved(fcpMessage));
		} else if ("NodeData".equals(messageName)) {
			fcpListenerManager.fireReceivedNodeData(new NodeData(fcpMessage));
		} else if ("TestDDAReply".equals(messageName)) {
			fcpListenerManager.fireReceivedTestDDAReply(new TestDDAReply(fcpMessage));
		} else if ("TestDDAComplete".equals(messageName)) {
			fcpListenerManager.fireReceivedTestDDAComplete(new TestDDAComplete(fcpMessage));
		} else if ("ConfigData".equals(messageName)) {
			fcpListenerManager.fireReceivedConfigData(new ConfigData(fcpMessage));
		} else if ("NodeHello".equals(messageName)) {
			fcpListenerManager.fireReceivedNodeHello(new NodeHello(fcpMessage));
		} else if ("CloseConnectionDuplicateClientName".equals(messageName)) {
			fcpListenerManager.fireReceivedCloseConnectionDuplicateClientName(new CloseConnectionDuplicateClientName(fcpMessage));
		} else if ("SentFeed".equals(messageName)) {
			fcpListenerManager.fireSentFeed(new SentFeed(fcpMessage));
		} else if ("ReceivedBookmarkFeed".equals(messageName)) {
			fcpListenerManager.fireReceivedBookmarkFeed(new ReceivedBookmarkFeed(fcpMessage));
		} else {
			fcpListenerManager.fireMessageReceived(fcpMessage);
		}
	}

	/**
	 * Handles a disconnect from the node.
	 *
	 * @param throwable
	 *            The exception that caused the disconnect, or
	 *            <code>null</code> if there was no exception
	 */
	 private synchronized void handleDisconnect(Throwable throwable) {
		FcpUtils.close(remoteInputStream);
		FcpUtils.close(remoteOutputStream);
		FcpUtils.close(remoteSocket);
		if (connectionHandler != null) {
			connectionHandler.stop();
			connectionHandler = null;
			fcpListenerManager.fireConnectionClosed(throwable);
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Incremets the counter in {@link #incomingMessageStatistics} by
	 * <cod>1</code> for the given message name.
	 *
	 * @param name
	 *            The name of the message to count
	 */
	private void countMessage(String name) {
		int oldValue = 0;
		if (incomingMessageStatistics.containsKey(name)) {
			oldValue = incomingMessageStatistics.get(name);
		}
		incomingMessageStatistics.put(name, oldValue + 1);
		logger.finest("count for " + name + ": " + (oldValue + 1));
	}

	private synchronized InputStream getInputStream(long dataLength) throws IOException {
		return new TempInputStream(remoteInputStream, dataLength);
	}

	/**
	 * Handles an FCP connection to a node.
	 */
	class FcpConnectionHandler implements Runnable {

		/** The logger. */
		private final Logger logger = Logger.getLogger(FcpConnectionHandler.class.getName());

		/** The input stream from the node. */
		private final InputStream remoteInputStream;

		/** Whether to stop the connection handler. */
		private boolean shouldStop;

		/** Whether the next read line feed should be ignored. */
		private boolean ignoreNextLinefeed;

		/**
		 * Creates a new connection handler that operates on the given input stream.
		 *
		 * @param remoteInputStream
		 *            The input stream from the node
		 */
		public FcpConnectionHandler(InputStream remoteInputStream) {
			this.remoteInputStream = remoteInputStream;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			FcpMessage fcpMessage = null;
			Throwable throwable = null;
			while (true) {
				synchronized (this) {
					if (shouldStop) {
						break;
					}
				}
				try {
					String line = readLine();
					logger.log(Level.FINEST, String.format("read line: %1$s", line));
					if (line == null) {
						throwable = new EOFException();
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					line = line.trim();
					if (fcpMessage == null) {
						fcpMessage = new FcpMessage(line);
						continue;
					}
					if ("EndMessage".equalsIgnoreCase(line) || "Data".equalsIgnoreCase(line)) {
						handleMessage(fcpMessage);
						fcpMessage = null;
					}
					int equalSign = line.indexOf('=');
					if (equalSign == -1) {
						/* something's fishy! */
						continue;
					}
					String field = line.substring(0, equalSign);
					String value = line.substring(equalSign + 1);
					assert fcpMessage != null: "fcp message is null";
					fcpMessage.setField(field, value);
				} catch (IOException ioe1) {
					throwable = ioe1;
					break;
				}
			}
			handleDisconnect(throwable);
		}

		/**
		 * Stops the connection handler.
		 */
		public void stop() {
			synchronized (this) {
				shouldStop = true;
			}
		}

		//
		// PRIVATE METHODS
		//

		/**
		 * Reads bytes from {@link #remoteInputStream} until ‘\r’ or ‘\n’ are
		 * encountered and decodes the read bytes using UTF-8.
		 *
		 * @return The decoded line
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		private String readLine() throws IOException {
			byte[] readBytes = new byte[512];
			int readIndex = 0;
			while (true) {
				int nextByte = remoteInputStream.read();
				if (nextByte == -1) {
					if (readIndex == 0) {
						return null;
					}
					break;
				}
				if (nextByte == 10) {
					if (!ignoreNextLinefeed) {
						break;
					}
				}
				ignoreNextLinefeed = false;
				if (nextByte == 13) {
					ignoreNextLinefeed = true;
					break;
				}
				if (readIndex == readBytes.length) {
					/* recopy & enlarge array */
					byte[] newReadBytes = new byte[readBytes.length * 2];
					System.arraycopy(readBytes, 0, newReadBytes, 0, readBytes.length);
					readBytes = newReadBytes;
				}
				readBytes[readIndex++] = (byte) nextByte;
			}
			return new String(readBytes, UTF_8);
		}

	}

}
