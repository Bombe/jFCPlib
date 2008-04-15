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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.pterodactylus.fcp.AllData;
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
import net.pterodactylus.fcp.FinishedCompression;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.IdentifierCollision;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
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
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.StartedCompression;
import net.pterodactylus.fcp.SubscribedUSKUpdate;
import net.pterodactylus.fcp.TestDDAComplete;
import net.pterodactylus.fcp.TestDDAReply;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.UnknownNodeIdentifier;
import net.pterodactylus.fcp.UnknownPeerNoteType;

/**
 * A high-level client that allows simple yet full-featured access to a Freenet
 * node.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelClient {

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

	/** The listener for the connection. */
	private HighLevelClientFcpListener highLevelClientFcpListener = new HighLevelClientFcpListener();

	/** The callback for {@link #connect()}. */
	private HighLevelCallback<ConnectResult> connectCallback;

	/** Mapping from request identifiers to callbacks. */
	private Map<String, HighLevelCallback<KeyGenerationResult>> keyGenerationCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<KeyGenerationResult>>());

	/** Mapping from request identifier to peer list callbacks. */
	private Map<String, HighLevelCallback<PeerListResult>> peerListCallbacks = Collections.synchronizedMap(new HashMap<String, HighLevelCallback<PeerListResult>>());

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
	// ACCESSORS
	//

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
		ClientHello clientHello = new ClientHello(clientName);
		connectCallback = new HighLevelCallback<ConnectResult>();
		fcpConnection.sendMessage(clientHello);
		return connectCallback;
	}

	/**
	 * Disconnects the client from the node.
	 */
	public void disconnect() {
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
		HighLevelCallback<KeyGenerationResult> keyGenerationCallback = new HighLevelCallback<KeyGenerationResult>();
		keyGenerationCallbacks.put(identifier, keyGenerationCallback);
		fcpConnection.sendMessage(generateSSK);
		return keyGenerationCallback;
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
		HighLevelCallback<PeerListResult> peerListCallback = new HighLevelCallback<PeerListResult>();
		peerListCallbacks.put(identifier, peerListCallback);
		fcpConnection.sendMessage(listPeers);
		return peerListCallback;
	}

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
	 * FCP listener for {@link HighLevelClient}.
	 * 
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 * @version $Id$
	 */
	private class HighLevelClientFcpListener implements FcpListener {

		/**
		 * Creates a new FCP listener for {@link HighLevelClient}.
		 */
		HighLevelClientFcpListener() {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#connectionClosed(net.pterodactylus.fcp.FcpConnection)
		 */
		public void connectionClosed(FcpConnection fcpConnection) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedAllData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.AllData)
		 */
		public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedCloseConnectionDuplicateClientName(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.CloseConnectionDuplicateClientName)
		 */
		public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedConfigData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.ConfigData)
		 */
		public void receivedConfigData(FcpConnection fcpConnection, ConfigData configData) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedDataFound(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.DataFound)
		 */
		public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPeerNotes(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPeerNotes)
		 */
		public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPeers(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPeers)
		 */
		public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedEndListPersistentRequests(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.EndListPersistentRequests)
		 */
		public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedFCPPluginReply(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FCPPluginReply)
		 */
		public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedGetFailed(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.GetFailed)
		 */
		public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedIdentifierCollision(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.IdentifierCollision)
		 */
		public void receivedIdentifierCollision(FcpConnection fcpConnection, IdentifierCollision identifierCollision) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedMessage(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FcpMessage)
		 */
		public void receivedMessage(FcpConnection fcpConnection, FcpMessage fcpMessage) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedNodeData(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.NodeData)
		 */
		public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
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
			ConnectResult connectResult = new ConnectResult();

			synchronized (syncObject) {
				connectCallback.setResult(connectResult);
			}
			connectCallback = null;
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
			HighLevelCallback<PeerListResult> peerListCallback = peerListCallbacks.get(identifier);
			PeerListResult peerListResult = peerListCallback.getIntermediaryResult();
			if (peerListResult == null) {
				peerListResult = new PeerListResult();
				peerListCallback.setResult(peerListResult, false);
			}
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPeerNote(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PeerNote)
		 */
		public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPeerRemoved(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PeerRemoved)
		 */
		public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentGet(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentGet)
		 */
		public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentPut(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentPut)
		 */
		public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentPutDir(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentPutDir)
		 */
		public void receivedPersistentPutDir(FcpConnection fcpConnection, PersistentPutDir persistentPutDir) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentRequestModified(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentRequestModified)
		 */
		public void receivedPersistentRequestModified(FcpConnection fcpConnection, PersistentRequestModified persistentRequestModified) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPersistentRequestRemoved(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PersistentRequestRemoved)
		 */
		public void receivedPersistentRequestRemoved(FcpConnection fcpConnection, PersistentRequestRemoved persistentRequestRemoved) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPluginInfo(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PluginInfo)
		 */
		public void receivedPluginInfo(FcpConnection fcpConnection, PluginInfo pluginInfo) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedProtocolError(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.ProtocolError)
		 */
		public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutFailed(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutFailed)
		 */
		public void receivedPutFailed(FcpConnection fcpConnection, PutFailed putFailed) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutFetchable(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutFetchable)
		 */
		public void receivedPutFetchable(FcpConnection fcpConnection, PutFetchable putFetchable) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedPutSuccessful(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.PutSuccessful)
		 */
		public void receivedPutSuccessful(FcpConnection fcpConnection, PutSuccessful putSuccessful) {
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
			KeyGenerationResult keyGenerationResult = new KeyGenerationResult();
			keyGenerationResult.setInsertURI(sskKeypair.getInsertURI());
			keyGenerationResult.setRequestURI(sskKeypair.getRequestURI());
			keyGenerationCallback.setResult(keyGenerationResult);
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedSimpleProgress(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.SimpleProgress)
		 */
		public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedStartedCompression(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.StartedCompression)
		 */
		public void receivedStartedCompression(FcpConnection fcpConnection, StartedCompression startedCompression) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedSubscribedUSKUpdate(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.SubscribedUSKUpdate)
		 */
		public void receivedSubscribedUSKUpdate(FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedTestDDAComplete(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.TestDDAComplete)
		 */
		public void receivedTestDDAComplete(FcpConnection fcpConnection, TestDDAComplete testDDAComplete) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedTestDDAReply(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.TestDDAReply)
		 */
		public void receivedTestDDAReply(FcpConnection fcpConnection, TestDDAReply testDDAReply) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedURIGenerated(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.URIGenerated)
		 */
		public void receivedURIGenerated(FcpConnection fcpConnection, URIGenerated uriGenerated) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedUnknownNodeIdentifier(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.UnknownNodeIdentifier)
		 */
		public void receivedUnknownNodeIdentifier(FcpConnection fcpConnection, UnknownNodeIdentifier unknownNodeIdentifier) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receivedUnknownPeerNoteType(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.UnknownPeerNoteType)
		 */
		public void receivedUnknownPeerNoteType(FcpConnection fcpConnection, UnknownPeerNoteType unknownPeerNoteType) {
		}

		/**
		 * @see net.pterodactylus.fcp.FcpListener#receviedFinishedCompression(net.pterodactylus.fcp.FcpConnection,
		 *      net.pterodactylus.fcp.FinishedCompression)
		 */
		public void receviedFinishedCompression(FcpConnection fcpConnection, FinishedCompression finishedCompression) {
		}

	}

}
