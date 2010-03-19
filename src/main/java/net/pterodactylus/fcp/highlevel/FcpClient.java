/*
 * jFCPlib - FcpClient.java - Copyright © 2009 David Roden
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
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import net.pterodactylus.fcp.AddPeer;
import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.DataFound;
import net.pterodactylus.fcp.EndListPeerNotes;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.EndListPersistentRequests;
import net.pterodactylus.fcp.FCPPluginMessage;
import net.pterodactylus.fcp.FCPPluginReply;
import net.pterodactylus.fcp.FcpAdapter;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.GenerateSSK;
import net.pterodactylus.fcp.GetFailed;
import net.pterodactylus.fcp.GetNode;
import net.pterodactylus.fcp.ListPeerNotes;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.ListPersistentRequests;
import net.pterodactylus.fcp.ModifyPeer;
import net.pterodactylus.fcp.ModifyPeerNote;
import net.pterodactylus.fcp.NodeData;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.PeerNote;
import net.pterodactylus.fcp.PeerRemoved;
import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.RemovePeer;
import net.pterodactylus.fcp.SSKKeypair;
import net.pterodactylus.fcp.SimpleProgress;
import net.pterodactylus.fcp.WatchGlobal;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.thread.ObjectWrapper;

/**
 * High-level FCP client that hides the details of the underlying FCP
 * implementation.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpClient {

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** Listener management. */
	private final FcpClientListenerManager fcpClientListenerManager = new FcpClientListenerManager(this);

	/** The underlying FCP connection. */
	private final FcpConnection fcpConnection;

	/** The {@link NodeHello} data sent by the node on connection. */
	private volatile NodeHello nodeHello;

	/** Whether the client is currently connected. */
	private volatile boolean connected;

	/** The listener for “connection closed” events. */
	private FcpListener connectionClosedListener;

	/**
	 * Creates an FCP client with the given name.
	 *
	 * @throws UnknownHostException
	 *             if the hostname “localhost” is unknown
	 */
	public FcpClient() throws UnknownHostException {
		this("localhost");
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param hostname
	 *            The hostname of the Freenet node
	 * @throws UnknownHostException
	 *             if the given hostname can not be resolved
	 */
	public FcpClient(String hostname) throws UnknownHostException {
		this(hostname, FcpConnection.DEFAULT_PORT);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param hostname
	 *            The hostname of the Freenet node
	 * @param port
	 *            The Freenet node’s FCP port
	 * @throws UnknownHostException
	 *             if the given hostname can not be resolved
	 */
	public FcpClient(String hostname, int port) throws UnknownHostException {
		this(InetAddress.getByName(hostname), port);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param host
	 *            The host address of the Freenet node
	 */
	public FcpClient(InetAddress host) {
		this(host, FcpConnection.DEFAULT_PORT);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param host
	 *            The host address of the Freenet node
	 * @param port
	 *            The Freenet node’s FCP port
	 */
	public FcpClient(InetAddress host, int port) {
		this(new FcpConnection(host, port), false);
	}

	/**
	 * Creates a new high-level FCP client that will use the given connection.
	 * This constructor will assume that the FCP connection is already
	 * connected.
	 *
	 * @param fcpConnection
	 *            The FCP connection to use
	 */
	public FcpClient(FcpConnection fcpConnection) {
		this(fcpConnection, true);
	}

	/**
	 * Creates a new high-level FCP client that will use the given connection.
	 *
	 * @param fcpConnection
	 *            The FCP connection to use
	 * @param connected
	 *            The initial status of the FCP connection
	 */
	public FcpClient(FcpConnection fcpConnection, boolean connected) {
		this.fcpConnection = fcpConnection;
		this.connected = connected;
		connectionClosedListener = new FcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
				FcpClient.this.connected = false;
				fcpClientListenerManager.fireFcpClientDisconnected();
			}
		};
		fcpConnection.addFcpListener(connectionClosedListener);
	}

	//
	// LISTENER MANAGEMENT
	//

	/**
	 * Adds an FCP listener to the underlying connection.
	 *
	 * @param fcpListener
	 *            The FCP listener to add
	 */
	public void addFcpListener(FcpListener fcpListener) {
		fcpConnection.addFcpListener(fcpListener);
	}

	/**
	 * Removes an FCP listener from the underlying connection.
	 *
	 * @param fcpListener
	 *            The FCP listener to remove
	 */
	public void removeFcpListener(FcpListener fcpListener) {
		fcpConnection.removeFcpListener(fcpListener);
	}

	/**
	 * Adds an FCP client listener to the list of registered listeners.
	 *
	 * @param fcpClientListener
	 *            The FCP client listener to add
	 */
	public void addFcpClientListener(FcpClientListener fcpClientListener) {
		fcpClientListenerManager.addListener(fcpClientListener);
	}

	/**
	 * Removes an FCP client listener from the list of registered listeners.
	 *
	 * @param fcpClientListener
	 *            The FCP client listener to remove
	 */
	public void removeFcpClientListener(FcpClientListener fcpClientListener) {
		fcpClientListenerManager.removeListener(fcpClientListener);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the {@link NodeHello} object that the node returned when
	 * connecting.
	 *
	 * @return The {@code NodeHello} data container
	 */
	public NodeHello getNodeHello() {
		return nodeHello;
	}

	/**
	 * Returns the underlying FCP connection.
	 *
	 * @return The underlying FCP connection
	 */
	public FcpConnection getConnection() {
		return fcpConnection;
	}

	//
	// ACTIONS
	//

	/**
	 * Connects the FCP client.
	 *
	 * @param name
	 *            The name of the client
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void connect(final String name) throws IOException, FcpException {
		checkConnected(false);
		connected = true;
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.connect();
				ClientHello clientHello = new ClientHello(name);
				fcpConnection.sendMessage(clientHello);
				WatchGlobal watchGlobal = new WatchGlobal(true);
				fcpConnection.sendMessage(watchGlobal);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
				FcpClient.this.nodeHello = nodeHello;
				completionLatch.countDown();
			}
		}.execute();
	}

	/**
	 * Disconnects the FCP client.
	 */
	public void disconnect() {
		synchronized (syncObject) {
			fcpConnection.close();
			syncObject.notifyAll();
		}
	}

	/**
	 * Returns whether this client is currently connected.
	 *
	 * @return {@code true} if the client is currently connected, {@code false}
	 *         otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Detaches this client from its underlying FCP connection.
	 */
	public void detach() {
		fcpConnection.removeFcpListener(connectionClosedListener);
	}

	//
	// PEER MANAGEMENT
	//

	/**
	 * Returns all peers that the node has.
	 *
	 * @param withMetadata
	 *            <code>true</code> to include peer metadata
	 * @param withVolatile
	 *            <code>true</code> to include volatile peer data
	 * @return A set containing the node’s peers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Peer> getPeers(final boolean withMetadata, final boolean withVolatile) throws IOException, FcpException {
		final Set<Peer> peers = Collections.synchronizedSet(new HashSet<Peer>());
		new ExtendedFcpAdapter() {

			/** The ID of the “ListPeers” request. */
			@SuppressWarnings("synthetic-access")
			private String identifier = createIdentifier("list-peers");

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new ListPeers(identifier, withMetadata, withVolatile));
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				if (peer.getIdentifier().equals(identifier)) {
					peers.add(peer);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
				if (endListPeers.getIdentifier().equals(identifier)) {
					completionLatch.countDown();
				}
			}
		}.execute();
		return peers;
	}

	/**
	 * Returns all darknet peers.
	 *
	 * @param withMetadata
	 *            <code>true</code> to include peer metadata
	 * @param withVolatile
	 *            <code>true</code> to include volatile peer data
	 * @return A set containing the node’s darknet peers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Peer> getDarknetPeers(boolean withMetadata, boolean withVolatile) throws IOException, FcpException {
		Collection<Peer> allPeers = getPeers(withMetadata, withVolatile);
		Collection<Peer> darknetPeers = new HashSet<Peer>();
		for (Peer peer : allPeers) {
			if (!peer.isOpennet() && !peer.isSeed()) {
				darknetPeers.add(peer);
			}
		}
		return darknetPeers;
	}

	/**
	 * Returns all opennet peers.
	 *
	 * @param withMetadata
	 *            <code>true</code> to include peer metadata
	 * @param withVolatile
	 *            <code>true</code> to include volatile peer data
	 * @return A set containing the node’s opennet peers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Peer> getOpennetPeers(boolean withMetadata, boolean withVolatile) throws IOException, FcpException {
		Collection<Peer> allPeers = getPeers(withMetadata, withVolatile);
		Collection<Peer> opennetPeers = new HashSet<Peer>();
		for (Peer peer : allPeers) {
			if (peer.isOpennet() && !peer.isSeed()) {
				opennetPeers.add(peer);
			}
		}
		return opennetPeers;
	}

	/**
	 * Returns all seed peers.
	 *
	 * @param withMetadata
	 *            <code>true</code> to include peer metadata
	 * @param withVolatile
	 *            <code>true</code> to include volatile peer data
	 * @return A set containing the node’s seed peers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Peer> getSeedPeers(boolean withMetadata, boolean withVolatile) throws IOException, FcpException {
		Collection<Peer> allPeers = getPeers(withMetadata, withVolatile);
		Collection<Peer> seedPeers = new HashSet<Peer>();
		for (Peer peer : allPeers) {
			if (peer.isSeed()) {
				seedPeers.add(peer);
			}
		}
		return seedPeers;
	}

	/**
	 * Adds the given peer to the node.
	 *
	 * @param peer
	 *            The peer to add
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void addPeer(Peer peer) throws IOException, FcpException {
		addPeer(peer.getNodeRef());
	}

	/**
	 * Adds the peer defined by the noderef to the node.
	 *
	 * @param nodeRef
	 *            The noderef that defines the new peer
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void addPeer(NodeRef nodeRef) throws IOException, FcpException {
		addPeer(new AddPeer(nodeRef));
	}

	/**
	 * Adds a peer, reading the noderef from the given URL.
	 *
	 * @param url
	 *            The URL to read the noderef from
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void addPeer(URL url) throws IOException, FcpException {
		addPeer(new AddPeer(url));
	}

	/**
	 * Adds a peer, reading the noderef of the peer from the given file.
	 * <strong>Note:</strong> the file to read the noderef from has to reside on
	 * the same machine as the node!
	 *
	 * @param file
	 *            The name of the file containing the peer’s noderef
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void addPeer(String file) throws IOException, FcpException {
		addPeer(new AddPeer(file));
	}

	/**
	 * Sends the given {@link AddPeer} message to the node. This method should
	 * not be called directly. Use one of {@link #addPeer(Peer)},
	 * {@link #addPeer(NodeRef)}, {@link #addPeer(URL)}, or
	 * {@link #addPeer(String)} instead.
	 *
	 * @param addPeer
	 *            The “AddPeer” message
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	private void addPeer(final AddPeer addPeer) throws IOException, FcpException {
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(addPeer);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				completionLatch.countDown();
			}
		}.execute();
	}

	/**
	 * Modifies the given peer.
	 *
	 * @param peer
	 *            The peer to modify
	 * @param allowLocalAddresses
	 *            <code>true</code> to allow local address, <code>false</code>
	 *            to not allow local address, <code>null</code> to not change
	 *            the setting
	 * @param disabled
	 *            <code>true</code> to disable the peer, <code>false</code> to
	 *            enable the peer, <code>null</code> to not change the setting
	 * @param listenOnly
	 *            <code>true</code> to enable “listen only” for the peer,
	 *            <code>false</code> to disable it, <code>null</code> to not
	 *            change it
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void modifyPeer(final Peer peer, final Boolean allowLocalAddresses, final Boolean disabled, final Boolean listenOnly) throws IOException, FcpException {
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new ModifyPeer(peer.getIdentity(), allowLocalAddresses, disabled, listenOnly));
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				completionLatch.countDown();
			}
		}.execute();
	}

	/**
	 * Removes the given peer.
	 *
	 * @param peer
	 *            The peer to remove
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void removePeer(final Peer peer) throws IOException, FcpException {
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new RemovePeer(peer.getIdentity()));
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeerRemoved(FcpConnection fcpConnection, PeerRemoved peerRemoved) {
				completionLatch.countDown();
			}
		}.execute();
	}

	//
	// PEER NOTES MANAGEMENT
	//

	/**
	 * Returns the peer note of the given peer.
	 *
	 * @param peer
	 *            The peer to get the note for
	 * @return The peer’s note
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public PeerNote getPeerNote(final Peer peer) throws IOException, FcpException {
		final ObjectWrapper<PeerNote> objectWrapper = new ObjectWrapper<PeerNote>();
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new ListPeerNotes(peer.getIdentity()));
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeerNote(FcpConnection fcpConnection, PeerNote peerNote) {
				if (peerNote.getNodeIdentifier().equals(peer.getIdentity())) {
					objectWrapper.set(peerNote);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedEndListPeerNotes(FcpConnection fcpConnection, EndListPeerNotes endListPeerNotes) {
				completionLatch.countDown();
			}
		}.execute();
		return objectWrapper.get();
	}

	/**
	 * Replaces the peer note for the given peer.
	 *
	 * @param peer
	 *            The peer
	 * @param noteText
	 *            The new base64-encoded note text
	 * @param noteType
	 *            The type of the note (currently only <code>1</code> is
	 *            allowed)
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void modifyPeerNote(final Peer peer, final String noteText, final int noteType) throws IOException, FcpException {
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new ModifyPeerNote(peer.getIdentity(), noteText, noteType));
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer receivedPeer) {
				if (receivedPeer.getIdentity().equals(peer.getIdentity())) {
					completionLatch.countDown();
				}
			}
		}.execute();
	}

	//
	// KEY GENERATION
	//

	/**
	 * Generates a new SSK key pair.
	 *
	 * @return The generated key pair
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public SSKKeypair generateKeyPair() throws IOException, FcpException {
		final ObjectWrapper<SSKKeypair> sskKeypairWrapper = new ObjectWrapper<SSKKeypair>();
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new GenerateSSK());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedSSKKeypair(FcpConnection fcpConnection, SSKKeypair sskKeypair) {
				sskKeypairWrapper.set(sskKeypair);
				completionLatch.countDown();
			}
		}.execute();
		return sskKeypairWrapper.get();
	}

	//
	// REQUEST MANAGEMENT
	//

	/**
	 * Returns all currently visible persistent get requests.
	 *
	 * @param global
	 *            <code>true</code> to return get requests from the global
	 *            queue, <code>false</code> to only show requests from the
	 *            client-local queue
	 * @return All get requests
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Request> getGetRequests(final boolean global) throws IOException, FcpException {
		return Filters.filteredCollection(getRequests(global), new Filter<Request>() {

			/**
			 * {@inheritDoc}
			 */
			public boolean filterObject(Request request) {
				return request instanceof GetRequest;
			}
		});
	}

	/**
	 * Returns all currently visible persistent put requests.
	 *
	 * @param global
	 *            <code>true</code> to return put requests from the global
	 *            queue, <code>false</code> to only show requests from the
	 *            client-local queue
	 * @return All put requests
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Request> getPutRequests(final boolean global) throws IOException, FcpException {
		return Filters.filteredCollection(getRequests(global), new Filter<Request>() {

			/**
			 * {@inheritDoc}
			 */
			public boolean filterObject(Request request) {
				return request instanceof PutRequest;
			}
		});
	}

	/**
	 * Returns all currently visible persistent requests.
	 *
	 * @param global
	 *            <code>true</code> to return requests from the global queue,
	 *            <code>false</code> to only show requests from the client-local
	 *            queue
	 * @return All requests
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Collection<Request> getRequests(final boolean global) throws IOException, FcpException {
		final Map<String, Request> requests = Collections.synchronizedMap(new HashMap<String, Request>());
		new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				fcpConnection.sendMessage(new ListPersistentRequests());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPersistentGet(FcpConnection fcpConnection, PersistentGet persistentGet) {
				if (!persistentGet.isGlobal() || global) {
					GetRequest getRequest = new GetRequest(persistentGet);
					requests.put(persistentGet.getIdentifier(), getRequest);
				}
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see net.pterodactylus.fcp.FcpAdapter#receivedDataFound(net.pterodactylus.fcp.FcpConnection,
			 *      net.pterodactylus.fcp.DataFound)
			 */
			@Override
			public void receivedDataFound(FcpConnection fcpConnection, DataFound dataFound) {
				Request getRequest = requests.get(dataFound.getIdentifier());
				if (getRequest == null) {
					return;
				}
				getRequest.setComplete(true);
				getRequest.setLength(dataFound.getDataLength());
				getRequest.setContentType(dataFound.getMetadataContentType());
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see net.pterodactylus.fcp.FcpAdapter#receivedGetFailed(net.pterodactylus.fcp.FcpConnection,
			 *      net.pterodactylus.fcp.GetFailed)
			 */
			@Override
			public void receivedGetFailed(FcpConnection fcpConnection, GetFailed getFailed) {
				Request getRequest = requests.get(getFailed.getIdentifier());
				if (getRequest == null) {
					return;
				}
				getRequest.setComplete(true);
				getRequest.setFailed(true);
				getRequest.setFatal(getFailed.isFatal());
				getRequest.setErrorCode(getFailed.getCode());
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see net.pterodactylus.fcp.FcpAdapter#receivedPersistentPut(net.pterodactylus.fcp.FcpConnection,
			 *      net.pterodactylus.fcp.PersistentPut)
			 */
			@Override
			public void receivedPersistentPut(FcpConnection fcpConnection, PersistentPut persistentPut) {
				if (!persistentPut.isGlobal() || global) {
					PutRequest putRequest = new PutRequest(persistentPut);
					requests.put(persistentPut.getIdentifier(), putRequest);
				}
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see net.pterodactylus.fcp.FcpAdapter#receivedSimpleProgress(net.pterodactylus.fcp.FcpConnection,
			 *      net.pterodactylus.fcp.SimpleProgress)
			 */
			@Override
			public void receivedSimpleProgress(FcpConnection fcpConnection, SimpleProgress simpleProgress) {
				Request request = requests.get(simpleProgress.getIdentifier());
				if (request == null) {
					return;
				}
				request.setTotalBlocks(simpleProgress.getTotal());
				request.setRequiredBlocks(simpleProgress.getRequired());
				request.setFailedBlocks(simpleProgress.getFailed());
				request.setFatallyFailedBlocks(simpleProgress.getFatallyFailed());
				request.setSucceededBlocks(simpleProgress.getSucceeded());
				request.setFinalizedTotal(simpleProgress.isFinalizedTotal());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedEndListPersistentRequests(FcpConnection fcpConnection, EndListPersistentRequests endListPersistentRequests) {
				completionLatch.countDown();
			}
		}.execute();
		return requests.values();
	}

	/**
	 * Sends a message to a plugin and waits for the response.
	 *
	 * @param pluginClass
	 *            The name of the plugin class
	 * @param parameters
	 *            The parameters for the plugin
	 * @return The responses from the plugin
	 * @throws FcpException
	 *             if an FCP error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public Map<String, String> sendPluginMessage(String pluginClass, Map<String, String> parameters) throws IOException, FcpException {
		return sendPluginMessage(pluginClass, parameters, 0, null);
	}

	/**
	 * Sends a message to a plugin and waits for the response.
	 *
	 * @param pluginClass
	 *            The name of the plugin class
	 * @param parameters
	 *            The parameters for the plugin
	 * @param dataLength
	 *            The length of the optional data stream, or {@code 0} if there
	 *            is no optional data stream
	 * @param dataInputStream
	 *            The input stream for the payload, or {@code null} if there is
	 *            no payload
	 * @return The responses from the plugin
	 * @throws FcpException
	 *             if an FCP error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public Map<String, String> sendPluginMessage(final String pluginClass, final Map<String, String> parameters, final long dataLength, final InputStream dataInputStream) throws IOException, FcpException {
		final Map<String, String> pluginReplies = Collections.synchronizedMap(new HashMap<String, String>());
		new ExtendedFcpAdapter() {

			@SuppressWarnings("synthetic-access")
			private final String identifier = createIdentifier("FCPPluginMessage");

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				FCPPluginMessage fcpPluginMessage = new FCPPluginMessage(pluginClass);
				for (Entry<String, String> parameter : parameters.entrySet()) {
					fcpPluginMessage.setParameter(parameter.getKey(), parameter.getValue());
				}
				fcpPluginMessage.setIdentifier(identifier);
				if ((dataLength > 0) && (dataInputStream != null)) {
					fcpPluginMessage.setDataLength(dataLength);
					fcpPluginMessage.setPayloadInputStream(dataInputStream);
				}
				fcpConnection.sendMessage(fcpPluginMessage);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedFCPPluginReply(FcpConnection fcpConnection, FCPPluginReply fcpPluginReply) {
				if (!fcpPluginReply.getIdentifier().equals(identifier)) {
					return;
				}
				pluginReplies.putAll(fcpPluginReply.getReplies());
				completionLatch.countDown();
			}

		}.execute();
		return pluginReplies;
	}

	//
	// NODE INFORMATION
	//

	/**
	 * Returns information about the node.
	 *
	 * @param giveOpennetRef
	 *            Whether to return the OpenNet reference
	 * @param withPrivate
	 *            Whether to return private node data
	 * @param withVolatile
	 *            Whether to return volatile node data
	 * @return Node information
	 * @throws FcpException
	 *             if an FCP error occurs
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public NodeData getNodeInformation(final Boolean giveOpennetRef, final Boolean withPrivate, final Boolean withVolatile) throws IOException, FcpException {
		final ObjectWrapper<NodeData> nodeDataWrapper = new ObjectWrapper<NodeData>();
		new ExtendedFcpAdapter() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() throws IOException {
				GetNode getNodeMessage = new GetNode(giveOpennetRef, withPrivate, withVolatile);
				fcpConnection.sendMessage(getNodeMessage);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedNodeData(FcpConnection fcpConnection, NodeData nodeData) {
				nodeDataWrapper.set(nodeData);
				completionLatch.countDown();
			}
		}.execute();
		return nodeDataWrapper.get();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a unique request identifier.
	 *
	 * @param basename
	 *            The basename of the request
	 * @return The created request identifier
	 */
	private String createIdentifier(String basename) {
		return basename + "-" + System.currentTimeMillis() + "-" + (int) (Math.random() * Integer.MAX_VALUE);
	}

	/**
	 * Checks whether the connection is in the required state.
	 *
	 * @param connected
	 *            The required connection state
	 * @throws FcpException
	 *             if the connection is not in the required state
	 */
	private void checkConnected(boolean connected) throws FcpException {
		if (this.connected != connected) {
			throw new FcpException("Client is " + (connected ? "not" : "already") + " connected.");
		}
	}

	/**
	 * Tells the client that it is now disconnected. This method is called by
	 * {@link ExtendedFcpAdapter} only.
	 */
	private void setDisconnected() {
		connected = false;
	}

	/**
	 * Implementation of an {@link FcpListener} that can store an
	 * {@link FcpException} and wait for the arrival of a certain command.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	private abstract class ExtendedFcpAdapter extends FcpAdapter {

		/** The count down latch used to wait for completion. */
		protected final CountDownLatch completionLatch = new CountDownLatch(1);

		/** The FCP exception, if any. */
		protected FcpException fcpException;

		/**
		 * Creates a new extended FCP adapter.
		 */
		public ExtendedFcpAdapter() {
			/* do nothing. */
		}

		/**
		 * Executes the FCP commands in {@link #run()}, wrapping the execution
		 * and catching exceptions.
		 *
		 * @throws IOException
		 *             if an I/O error occurs
		 * @throws FcpException
		 *             if an FCP error occurs
		 */
		@SuppressWarnings("synthetic-access")
		public void execute() throws IOException, FcpException {
			checkConnected(true);
			fcpConnection.addFcpListener(this);
			try {
				run();
				while (true) {
					try {
						completionLatch.await();
						break;
					} catch (InterruptedException ie1) {
						/* ignore, we’ll loop. */
					}
				}
			} catch (IOException ioe1) {
				setDisconnected();
				throw ioe1;
			} finally {
				fcpConnection.removeFcpListener(this);
			}
			if (fcpException != null) {
				setDisconnected();
				throw fcpException;
			}
		}

		/**
		 * The FCP commands that actually get executed.
		 *
		 * @throws IOException
		 *             if an I/O error occurs
		 */
		public abstract void run() throws IOException;

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void connectionClosed(FcpConnection fcpConnection, Throwable throwable) {
			fcpException = new FcpException("Connection closed", throwable);
			completionLatch.countDown();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void receivedCloseConnectionDuplicateClientName(FcpConnection fcpConnection, CloseConnectionDuplicateClientName closeConnectionDuplicateClientName) {
			fcpException = new FcpException("Connection closed, duplicate client name");
			completionLatch.countDown();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void receivedProtocolError(FcpConnection fcpConnection, ProtocolError protocolError) {
			fcpException = new FcpException("Protocol error (" + protocolError.getCode() + ", " + protocolError.getCodeDescription());
			completionLatch.countDown();
		}

	}

}
