/*
 * fcplib - HighLevelClient.java -
 * Copyright © 2008 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package net.pterodactylus.fcp.highlevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.fcp.AddPeer;
import net.pterodactylus.fcp.AllData;
import net.pterodactylus.fcp.ClientGet;
import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.ConfigData;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.EndListPersistentRequests;
import net.pterodactylus.fcp.FCPPluginReply;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.FcpUtils;
import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.ListPersistentRequests;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerRemoved;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.PersistentPutDir;
import net.pterodactylus.fcp.PersistentRequestModified;
import net.pterodactylus.fcp.PersistentRequestRemoved;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutFetchable;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.ReturnType;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.SubscribedUSKUpdate;
import net.pterodactylus.fcp.TestDDAComplete;
import net.pterodactylus.fcp.TestDDAReply;
import net.pterodactylus.fcp.TestDDARequest;
import net.pterodactylus.fcp.TestDDAResponse;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.UnknownNodeIdentifier;
import net.pterodactylus.fcp.UnknownPeerNoteType;
import net.pterodactylus.fcp.WatchGlobal;

/**
 * A high-level client that allows simple yet full-featured access to a Freenet
 * node.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelClient {

	/** Logger. */
	private static final Logger logger = Logger.getLogger(HighLevelClient.class.getName());

	/** Object for internal synchronization. */
	private final Object syncObject = new Object();

	/** The name of the client. */
	private final String clientName;

	/** The address of the node. */
	private InetAddress address;

	/** The port number of the node. */
	private int port;

	/** The FCP connection to the node. */
	private FcpConnection fcpConnection;

	/** Listeners for high-level client events. */
	private List<HighLevelClientListener> highLevelClientListeners = Collections.synchronizedList(new ArrayList<HighLevelClientListener>());

	/** The listener for the connection. */
	private HighLevelClientFcpListener highLevelClientFcpListener = new HighLevelClientFcpListener();

	/** The listeners for progress events. */
	private List<HighLevelProgressListener> highLevelProgressListeners = Collections.synchronizedList(new ArrayList<HighLevelProgressListener>());

	/** The callback for {@link #connect()}. */
	private HighLevelCallback<ConnectResult> connectCallback;

	/** Mapping from request identifiers to callbacks. */
	private Map<String, HighLevelCallback<KeyGenerationResult>> keyGenerationCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<KeyGenerationResult>>());

	/** Mapping from request identifier to peer list callbacks. */
	private Map<String, HighLevelCallback<PeerListResult>> peerListCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<PeerListResult>>());

	/** Mapping from request identifier to peer callbacks. */
	private Map<String, HighLevelCallback<PeerResult>> peerCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<PeerResult>>());

	/** Mapping from directories to DDA callbacks. */
	private Map<String, HighLevelCallback<DirectDiskAccessResult>> directDiskAccessCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<DirectDiskAccessResult>>());

	/** Mapping from request identifiers to download callbacks. */
	private Map<String, HighLevelProgressCallback<DownloadResult>> downloadCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelProgressCallback<DownloadResult>>());

	/** The callback for {@link #getRequests()}. */
	private HighLevelCallback<RequestListResult> requestListCallback;

	/**
	 * Creates a new high-level client that connects to a node on
	 * <code>localhost</code>.
	 * 
	 * @param clientName
	 *            The name of the client
	 * @throws UnknownHostException
	 *             if the hostname of the node can not be resolved.
	 */
	public HighLevelClient(String clientName) throws UnknownHostException {
		this(clientName, "localhost");
	}

	/**
	 * Creates a new high-level client that connects to a node on the given
	 * host.
	 * 
	 * @param clientName
	 *            The name of the client
	 * @param host
	 *            The hostname of the node
	 * @throws UnknownHostException
	 *             if the hostname of the node can not be resolved.
	 */
	public HighLevelClient(String clientName, String host) throws UnknownHostException {
		this(clientName, host, FcpConnection.DEFAULT_PORT);
	}

	/**
	 * Creates a new high-level client that connects to a node on the given
	 * host.
	 * 
	 * @param clientName
	 *            The name of the client
	 * @param host
	 *            The hostname of the node
	 * @param port
	 *            The port number of the node
	 * @throws UnknownHostException
	 *             if the hostname of the node can not be resolved.
	 */
	public HighLevelClient(String clientName, String host, int port) throws UnknownHostException {
		this(clientName, InetAddress.getByName(host), port);
	}

	/**
	 * Creates a new high-level client that connects to a node at the given
	 * address.
	 * 
	 * @param clientName
	 *            The name of the client
	 * @param address
	 *            The address of the node
	 * @param port
	 *            The port number of the node
	 */
	public HighLevelClient(String clientName, InetAddress address, int port) {
		this.clientName = clientName;
		this.address = address;
		this.port = port;
	}

	//
	// EVENT MANAGEMENT
	//

	/**
	 * Adds the given high-level client listener to list of listeners.
	 * 
	 * @param highLevelClientListener
	 *            The listener to add
	 */
	public void addHighLevelClientListener(HighLevelClientListener highLevelClientListener) {
		highLevelClientListeners.add(highLevelClientListener);
	}

	/**
	 * Removes the given high-level client listener from the list of listeners.
	 * 
	 * @param highLevelClientListener
	 *            The listener to remove
	 */
	public void removeHighLevelClientListener(HighLevelClientListener highLevelClientListener) {
		highLevelClientListeners.remove(highLevelClientListener);
	}

	/**
	 * Notifies all listeners that a client has connected.
	 */
	private void fireClientConnected() {
		for (HighLevelClientListener highLevelClientListener: highLevelClientListeners) {
			highLevelClientListener.clientConnected(this);
		}
	}

	/**
	 * Notifies all listeners that a client has disconnected.
	 * 
	 * @param throwable
	 *            The exception that caused the disconnect, or <code>null</code>
	 *            if there was no exception
	 */
	private void fireClientDisconnected(Throwable throwable) {
		for (HighLevelClientListener highLevelClientListener: highLevelClientListeners) {
			highLevelClientListener.clientDisconnected(this, throwable);
		}
	}

	/**
	 * Adds a high-level progress listener.
	 * 
	 * @param highLevelProgressListener
	 *            The high-level progress listener to add
	 */
	public void addHighLevelProgressListener(HighLevelProgressListener highLevelProgressListener) {
		highLevelProgressListeners.add(highLevelProgressListener);
	}

	/**
	 * Removes a high-level progress listener.
	 * 
	 * @param highLevelProgressListener
	 *            The high-level progress listener to remove
	 */
	public void removeHighLevelProgressListener(HighLevelProgressListener highLevelProgressListener) {
		highLevelProgressListeners.remove(highLevelProgressListener);
	}

	/**
	 * Notifies all listeners that the request with the given identifier made
	 * some progress.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 * @param highLevelProgress
	 *            The progress of the request
	 */
	private void fireProgressReceived(String identifier, HighLevelProgress highLevelProgress) {
		for (HighLevelProgressListener highLevelProgressListener: highLevelProgressListeners) {
			highLevelProgressListener.progressReceived(identifier, highLevelProgress);
		}
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the FCP connection that backs this high-level client. This method
	 * should be used with care as fiddling around with the FCP connection can
	 * easily break the high-level client if you don’t know what you’re doing!
	 * 
	 * @return The FCP connection of this client
	 */
	public FcpConnection getFcpConnection() {
		return fcpConnection;
	}

	//
	// ACTIONS
	//

	/**
	 * Connects the client.
	 * 
	 * @return A callback with a connection result
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelCallback<ConnectResult> connect() throws IOException {
		fcpConnection = new FcpConnection(address, port);
		fcpConnection.addFcpListener(highLevelClientFcpListener);
		fcpConnection.connect();
		ClientHello clientHello = new ClientHello(clientName);
		connectCallback = new HighLevelCallback<ConnectResult>(new ConnectResult());
		fcpConnection.sendMessage(clientHello);
		return connectCallback;
	}

	/**
	 * Disconnects the client from the node.
	 */
	public void disconnect() {
		disconnect(null);
	}

	/**
	 * Generates a new SSK keypair.
	 * 
	 * @return A callback with the keypair
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelCallback<KeyGenerationResult> generateKey() throws IOException {
		String identifier = generateIdentifier("generateSSK");
		GenerateSSK generateSSK = new GenerateSSK(identifier);
		HighLevelCallback<KeyGenerationResult> keyGenerationCallback = new HighLevelCallback<KeyGenerationResult>(new KeyGenerationResult(identifier));
		keyGenerationCallbacks.put(identifier, keyGenerationCallback);
		fcpConnection.sendMessage(generateSSK);
		return keyGenerationCallback;
	}

	/**
	 * Sets whether to watch the global queue.
	 * 
	 * @param enabled
	 *            <code>true</code> to watch the global queue in addition to
	 *            the client-local queue, <code>false</code> to only watch the
	 *            client-local queue
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public void setWatchGlobal(boolean enabled) throws IOException {
		WatchGlobal watchGlobal = new WatchGlobal(enabled);
		fcpConnection.sendMessage(watchGlobal);
	}

	/**
	 * Gets a list of all peers from the node.
	 * 
	 * @return A callback with the peer list
	 * @throws IOException
	 *             if an I/O error occurs with the node
	 */
	public HighLevelCallback<PeerListResult> getPeers() throws IOException {
		String identifier = generateIdentifier("listPeers");
		ListPeers listPeers = new ListPeers(identifier, true, true);
		HighLevelCallback<PeerListResult> peerListCallback = new HighLevelCallback<PeerListResult>(new PeerListResult(identifier));
		peerListCallbacks.put(identifier, peerListCallback);
		fcpConnection.sendMessage(listPeers);
		return peerListCallback;
	}

	/**
	 * Adds the peer whose noderef is stored in the given file.
	 * 
	 * @param nodeRefFile
	 *            The name of the file the peer’s noderef is stored in
	 * @return A peer callback
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelCallback<PeerResult> addPeer(String nodeRefFile) throws IOException {
		String identifier = generateIdentifier("addPeer");
		AddPeer addPeer = new AddPeer(nodeRefFile);
		HighLevelCallback<PeerResult> peerCallback = new HighLevelCallback<PeerResult>(new PeerResult(identifier));
		peerCallbacks.put(identifier, peerCallback);
		fcpConnection.sendMessage(addPeer);
		return peerCallback;
	}

	/**
	 * Adds the peer whose noderef is stored in the given file.
	 * 
	 * @param nodeRefURL
	 *            The URL where the peer’s noderef is stored
	 * @return A peer callback
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelCallback<PeerResult> addPeer(URL nodeRefURL) throws IOException {
		String identifier = generateIdentifier("addPeer");
		AddPeer addPeer = new AddPeer(nodeRefURL);
		HighLevelCallback<PeerResult> peerCallback = new HighLevelCallback<PeerResult>(new PeerResult(identifier));
		peerCallbacks.put(identifier, peerCallback);
		fcpConnection.sendMessage(addPeer);
		return peerCallback;
	}

	/**
	 * Adds the peer whose noderef is stored in the given file.
	 * 
	 * @param nodeRef
	 *            The peer’s noderef
	 * @return A peer callback
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelCallback<PeerResult> addPeer(NodeRef nodeRef) throws IOException {
		String identifier = generateIdentifier("addPeer");
		AddPeer addPeer = new AddPeer(nodeRef);
		HighLevelCallback<PeerResult> peerCallback = new HighLevelCallback<PeerResult>(new PeerResult(identifier));
		peerCallbacks.put(identifier, peerCallback);
		fcpConnection.sendMessage(addPeer);
		return peerCallback;
	}

	/**
	 * Checks whether direct disk access for the given directory is possible.
	 * You have to perform this check before you can upload or download anything
	 * from or the disk directly!
	 * 
	 * @param directory
	 *            The directory to check
	 * @param wantRead
	 *            Whether you want to read the given directory
	 * @param wantWrite
	 *            Whether you want to write to the given directory
	 * @return A direct disk access callback
	 * @throws IOException
	 */
	public HighLevelCallback<DirectDiskAccessResult> checkDirectDiskAccess(String directory, boolean wantRead, boolean wantWrite) throws IOException {
		TestDDARequest testDDARequest = new TestDDARequest(directory, wantRead, wantWrite);
		HighLevelCallback<DirectDiskAccessResult> directDiskAccessCallback = new HighLevelCallback<DirectDiskAccessResult>(new DirectDiskAccessResult(directory));
		directDiskAccessCallbacks.put(directory, directDiskAccessCallback);
		fcpConnection.sendMessage(testDDARequest);
		return directDiskAccessCallback;
	}

	/**
	 * Starts a download. Files can either be download to disk or streamed from
	 * the node. When downloading to disk you have to perform a direct disk
	 * access check for the directory you want to put the downloaded file in!
	 * 
	 * @see #checkDirectDiskAccess(String, boolean, boolean)
	 * @param uri
	 *            The URI to get
	 * @param filename
	 *            The filename to save the data to, or <code>null</code> to
	 *            retrieve the data as InputStream from the
	 *            {@link DownloadResult}
	 * @param global
	 *            Whether to put the download on the global queue
	 * @return A download result
	 * @throws IOException
	 *             if an I/O error occurs communicating with the node
	 */
	public HighLevelProgressCallback<DownloadResult> download(String uri, String filename, boolean global) throws IOException {
		String identifier = generateIdentifier("download");
		ClientGet clientGet = new ClientGet(uri, identifier, (filename == null) ? ReturnType.direct : ReturnType.disk);
		clientGet.setGlobal(global);
		HighLevelProgressCallback<DownloadResult> downloadCallback = new HighLevelProgressCallback<DownloadResult>(new DownloadResult(identifier));
		downloadCallbacks.put(identifier, downloadCallback);
		fcpConnection.sendMessage(clientGet);
		return downloadCallback;
	}

	/**
	 * Requests a list of all running requests from the node.
	 * 
	 * @return The request list result
	 * @throws IOException
	 *             if an I/O errors communicating with the node
	 */
	public HighLevelCallback<RequestListResult> getRequests() throws IOException {
		String identifier = generateIdentifier("list-persistent-requests");
		ListPersistentRequests listPersistentRequests = new ListPersistentRequests();
		synchronized (syncObject) {
			if (requestListCallback != null) {
				logger.log(Level.SEVERE, "getRequests() called with request still running!");
			}
			requestListCallback = new HighLevelCallback<RequestListResult>(new RequestListResult(identifier));
		}
		fcpConnection.sendMessage(listPersistentRequests);
		return requestListCallback;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Generates an identifier for the given function.
	 * 
	 * @param function
	 *            The name of the function
	 * @return An identifier
	 */
	private String generateIdentifier(String function) {
		return "jFCPlib-" + function + "-" + System.currentTimeMillis();
	}

	/**
	 * Disconnects the client from the node, handing the given Throwable to
	 * {@link #fireClientDisconnected(Throwable)}.
	 * 
	 * @param throwable
	 *            The exception that caused the disconnect, or <code>null</code>
	 *            if there was no exception
	 */
	private void disconnect(Throwable throwable) {
		if (fcpConnection != null) {
			fcpConnection.close();
		}
		fireClientDisconnected(throwable);
	}

	/**
	 * FCP listener for {@link HighLevelClient}.
	 * 
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 * @version $Id$
	 */
	private class HighLevelClientFcpListener implements FcpListener {

		/** Mapping from directory to written file (for cleanup). */
		private final Map<DirectDiskAccessResult, String> writtenFiles = new HashMap<DirectDiskAccessResult, String>();

		/**
		 * Creates a new FCP listener for {@link HighLevelClient}.
		 */
		HighLevelClientFcpListener() {
			/* do nothing. */
		}

		//
		// PRIVATE METHODS
		//

		/**
		 * Searches all callback collections for a callback with the given
		 * identifier and cancels it.
		 * 
		 * @param identifier
		 *            The identifier to search for, or <code>null</code> to
		 *            cancel all pending requests
		 */
		@SuppressWarnings("synthetic-access")
		private void cancelIdentifier(String identifier) {
			synchronized (syncObject) {
				if (connectCallback != null) {
					connectCallback.getIntermediaryResult().setFailed(true);
					connectCallback.setDone();
					connectCallback = null;
				}
				if (requestListCallback != null) {
					requestListCallback.getIntermediaryResult().setFailed(true);
					requestListCallback.setDone();
					requestListCallback = null;
				}
			}
			if (identifier == null) {
				/* key generation callbacks */
				for (Entry<String, HighLevelCallback<KeyGenerationResult>> keyGenerationEntry: keyGenerationCallbacks.entrySet()) {
					keyGenerationEntry.getValue().getIntermediaryResult().setFailed(true);
					keyGenerationEntry.getValue().setDone();
				}
				keyGenerationCallbacks.clear();
				/* peer list callbacks. */
				for (Entry<String, HighLevelCallback<PeerListResult>> peerListEntry: peerListCallbacks.entrySet()) {
					peerListEntry.getValue().getIntermediaryResult().setFailed(true);
					peerListEntry.getValue().setDone();
				}
				peerListCallbacks.clear();
				/* peer callbacks. */
				for (Entry<String, HighLevelCallback<PeerResult>> peerEntry: peerCallbacks.entrySet()) {
					peerEntry.getValue().getIntermediaryResult().setFailed(true);
					peerEntry.getValue().setDone();
				}
				peerCallbacks.clear();
				/* direct disk access callbacks. */
				for (Entry<String, HighLevelCallback<DirectDiskAccessResult>> directDiskAccessEntry: directDiskAccessCallbacks.entrySet()) {
					directDiskAccessEntry.getValue().getIntermediaryResult().setFailed(true);
					directDiskAccessEntry.getValue().setDone();
				}
				directDiskAccessCallbacks.clear();
				/* download callbacks. */
				for (Entry<String, HighLevelProgressCallback<DownloadResult>> downloadEntry: downloadCallbacks.entrySet()) {
					downloadEntry.getValue().getIntermediaryResult().setFailed(true);
					downloadEntry.getValue().setDone();
				}
				downloadCallbacks.clear();
			} else {
				HighLevelCallback<KeyGenerationResult> keyGenerationCallback = keyGenerationCallbacks.remove(identifier);
				if (keyGenerationCallback != null) {
					keyGenerationCallback.getIntermediaryResult().setFailed(true);
					keyGenerationCallback.setDone();
					return;
				}
				HighLevelCallback<PeerListResult> peerListCallback = peerListCallbacks.remove(identifier);
				if (peerListCallback != null) {
					peerListCallback.getIntermediaryResult().setFailed(true);
					peerListCallback.setDone();
					return;
				}
				HighLevelCallback<PeerResult> peerCallback = peerCallbacks.remove(identifier);
				if (peerCallback != null) {
					peerCallback.getIntermediaryResult().setFailed(true);
					peerCallback.setDone();
					return;
				}
				HighLevelCallback<DirectDiskAccessResult> directDiskAccessCallback = directDiskAccessCallbacks.remove(identifier);
				if (directDiskAccessCallback != null) {
					directDiskAccessCallback.getIntermediaryResult().setFailed(true);
					directDiskAccessCallback.setDone();
					return;
				}
				HighLevelProgressCallback<DownloadResult> downloadCallback = downloadCallbacks.remove(identifier);
				if (downloadCallback != null) {
					downloadCallback.getIntermediaryResult().setFailed(true);
					downloadCallback.setDone();
					return;
				}
			}
		}

		/**
		 * Reads the given file and returns the first line of the file.
		 * 
		 * @param readFilename
		 *            The name of the file to read
		 * @return The content of the file
		 */
		private String readContent(String readFilename) {
			FileReader fileReader = null;
			BufferedReader bufferedFileReader = null;
			try {
				fileReader = new FileReader(readFilename);
				bufferedFileReader = new BufferedReader(fileReader);
				String content = bufferedFileReader.readLine();
				return content;
			} catch (IOException ioe1) {
				/* swallow. */
			} finally {
				FcpUtils.close(bufferedFileReader);
				FcpUtils.close(fileReader);
			}
			return null;
		}

		/**
		 * Writes the given content to the given file.
		 * 
		 * @param directDiskAccessResult
		 *            The DDA result
		 * @param writeFilename
		 *            The name of the file to write to
		 * @param writeContent
		 *            The content to write to the file
		 */
		private void writeContent(DirectDiskAccessResult directDiskAccessResult, String writeFilename, String writeContent) {
			if ((writeFilename == null) || (writeContent == null)) {
				return;
			}
			writtenFiles.put(directDiskAccessResult, writeFilename);
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(writeFilename);
				fileWriter.write(writeContent);
			} catch (IOException ioe1) {
				/* swallow. */
			} finally {
				FcpUtils.close(fileWriter);
			}
		}

		/**
		 * Cleans up any files that written for the given result.
		 * 
		 * @param directDiskAccessResult
		 *            The direct disk access result
		 */
		@SuppressWarnings("synthetic-access")
		private void cleanFiles(DirectDiskAccessResult directDiskAccessResult) {
			String writeFilename = writtenFiles.remove(directDiskAccessResult);
			if (writeFilename != null) {
				if (!new File(writeFilename).delete()) {
					logger.warning("could not delete " + writeFilename);
				}
			}
		}

		//
		// INTERFACE FcpListener
		//

		/**
		 * @see net.pterodactylus.fcp.FcpListener#connectionClosed(net.pterodactylus.fcp.FcpConnection,
		 *      Throwable)
		 */
		@SuppressWarnings("synthetic-access")
		public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			cancelIdentifier(null);
			disconnect(throwable);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedAllData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.AllData)
		 */
		public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedCloseConnectionDuplicateClientName(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.CloseConnectionDuplicateClientName)
		 */
		public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedConfigData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.ConfigData)
		 */
		public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedDataFound(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.DataFound)
		 */
		public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPeerNotes(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPeerNotes)
		 */
		public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPeers(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPeers)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String identifier = endListPeers.getIdentifier();
			HighLevelCallback<PeerListResult> peerListCallback = peerListCallbacks.remove(identifier);
			if (peerListCallback == null) {
				return;
			}
			peerListCallback.setDone();
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPersistentRequests(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPersistentRequests)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			synchronized (syncObject) {
				if (HighLevelClient.this.requestListCallback == null) {
					logger.log(Level.WARNING, "got EndListPersistentRequests without running request!");
					return;
				}
				requestListCallback.setDone();
				requestListCallback = null;
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedFCPPluginReply(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FCPPluginReply)
		 */
		public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedGetFailed(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.GetFailed)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String identifier = getFailed.getIdentifier();
			HighLevelProgressCallback<DownloadResult> downloadCallback = downloadCallbacks.remove(identifier);
			if (downloadCallback != null) {
				downloadCallback.getIntermediaryResult().setFailed(true);
				downloadCallback.setDone();
				return;
			}
			/* unknown identifier? */
			logger.warning("unknown identifier for GetFailed: " + identifier);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedIdentifierCollision(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.IdentifierCollision)
		 */
		public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedMessage(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FcpMessage)
		 */
		public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedNodeData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.NodeData)
		 */
		public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedNodeHello(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.NodeHello)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			synchronized (syncObject) {
				connectCallback.getIntermediaryResult().setFailed(false);
				connectCallback.setDone();
				connectCallback = null;
			}
			fireClientConnected();
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPeer(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.Peer)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String identifier = peer.getIdentifier();
			if (identifier == null) {
				return;
			}
			HighLevelCallback<PeerListResult> peerListCallback = peerListCallbacks.get(identifier);
			if (peerListCallback != null) {
				peerListCallback.getIntermediaryResult().addPeer(peer);
				return;
			}
			HighLevelCallback<PeerResult> peerResult = peerCallbacks.remove(identifier);
			if (peerResult != null) {
				peerResult.getIntermediaryResult().setPeer(peer);
				peerResult.setDone();
				return;
			}
			logger.warning("got Peer message with unknown identifier: " + identifier);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPeerNote(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PeerNote)
		 */
		public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPeerRemoved(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PeerRemoved)
		 */
		public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentGet(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentGet)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			synchronized (syncObject) {
				if (requestListCallback != null) {
					RequestListResult requestListResult = requestListCallback.getIntermediaryResult();
					requestListResult.addRequestResult(new GetRequestResult(persistentGet));
					return;
				}
			}
			String identifier = persistentGet.getIdentifier();
			if (downloadCallbacks.containsKey(identifier)) {
				/* TODO */
				return;
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentPut(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentPut)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			synchronized (syncObject) {
				if (requestListCallback != null) {
					RequestListResult requestListResult = requestListCallback.getIntermediaryResult();
					requestListResult.addRequestResult(new PutRequestResult(persistentPut));
					return;
				}
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentPutDir(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentPutDir)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			synchronized (syncObject) {
				if (requestListCallback != null) {
					RequestListResult requestListResult = requestListCallback.getIntermediaryResult();
					requestListResult.addRequestResult(new PutDirRequestResult(persistentPutDir));
					return;
				}
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentRequestModified(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentRequestModified)
		 */
		public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentRequestRemoved(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentRequestRemoved)
		 */
		public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPluginInfo(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PluginInfo)
		 */
		public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedProtocolError(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.ProtocolError)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String identifier = protocolError.getIdentifier();
			if (identifier == null) {
				return;
			}
			cancelIdentifier(identifier);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutFailed(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutFailed)
		 */
		public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutFetchable(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutFetchable)
		 */
		public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutSuccessful(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutSuccessful)
		 */
		public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedSSKKeypair(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.SSKKeypair)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			HighLevelCallback<KeyGenerationResult> keyGenerationCallback = keyGenerationCallbacks.remove(sskKeypair.getIdentifier());
			if (keyGenerationCallback == null) {
				return;
			}
			KeyGenerationResult keyGenerationResult = keyGenerationCallback.getIntermediaryResult();
			keyGenerationResult.setInsertURI(sskKeypair.getInsertURI());
			keyGenerationResult.setRequestURI(sskKeypair.getRequestURI());
			keyGenerationCallback.setDone();
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedSimpleProgress(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.SimpleProgress)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String identifier = simpleProgress.getIdentifier();
			HighLevelProgressCallback<DownloadResult> downloadCallback = downloadCallbacks.get(identifier);
			if (downloadCallback != null) {
				DownloadResult downloadResult = downloadCallback.getIntermediaryResult();
				downloadResult.setTotalBlocks(simpleProgress.getTotal());
				downloadResult.setRequiredBlocks(simpleProgress.getRequired());
				downloadResult.setSuccessfulBlocks(simpleProgress.getSucceeded());
				downloadResult.setFailedBlocks(simpleProgress.getFailed());
				downloadResult.setFatallyFailedBlocks(simpleProgress.getFatallyFailed());
				downloadResult.setTotalFinalized(simpleProgress.isFinalizedTotal());
				downloadCallback.progressUpdated();
				return;
			}
			HighLevelProgress highLevelProgress = new HighLevelProgress(identifier, simpleProgress.getTotal(), simpleProgress.getRequired(), simpleProgress.getSucceeded(), simpleProgress.getFailed(), simpleProgress.getFatallyFailed(), simpleProgress.isFinalizedTotal());
			fireProgressReceived(identifier, highLevelProgress);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedStartedCompression(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.StartedCompression)
		 */
		public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedSubscribedUSKUpdate(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.SubscribedUSKUpdate)
		 */
		public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedTestDDAComplete(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.TestDDAComplete)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String directory = testDDAComplete.getDirectory();
			if (directory == null) {
				return;
			}
			HighLevelCallback<DirectDiskAccessResult> directDiskAccessCallback = directDiskAccessCallbacks.remove(directory);
			DirectDiskAccessResult directDiskAccessResult = directDiskAccessCallback.getIntermediaryResult();
			cleanFiles(directDiskAccessResult);
			directDiskAccessResult.setReadAllowed(testDDAComplete.isReadDirectoryAllowed());
			directDiskAccessResult.setWriteAllowed(testDDAComplete.isWriteDirectoryAllowed());
			directDiskAccessCallback.setDone();
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedTestDDAReply(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.TestDDAReply)
		 */
		@SuppressWarnings("synthetic-access")
		public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
			if (fcpConnection != HighLevelClient.this.fcpConnection) {
				return;
			}
			String directory = testDDAReply.getDirectory();
			if (directory == null) {
				return;
			}
			DirectDiskAccessResult directDiskAccessResult = directDiskAccessCallbacks.get(directory).getIntermediaryResult();
			String readFilename = testDDAReply.getReadFilename();
			String readContent = readContent(readFilename);
			String writeFilename = testDDAReply.getWriteFilename();
			String writeContent = testDDAReply.getContentToWrite();
			writeContent(directDiskAccessResult, writeFilename, writeContent);
			TestDDAResponse testDDAResponse = new TestDDAResponse(directory, readContent);
			try {
				fcpConnection.sendMessage(testDDAResponse);
			} catch (IOException e) {
				/* swallow. I’m verry unhappy about this. */
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedURIGenerated(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.URIGenerated)
		 */
		public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedUnknownNodeIdentifier(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.UnknownNodeIdentifier)
		 */
		public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedUnknownPeerNoteType(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.UnknownPeerNoteType)
		 */
		public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
			/* TODO */
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receviedFinishedCompression(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FinishedCompression)
		 */
		public void receviedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
			/* TODO */
		}

	}

}
