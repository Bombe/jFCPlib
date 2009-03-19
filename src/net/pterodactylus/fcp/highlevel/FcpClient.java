/*
 * jFCPlib - FcpClient.java -
 * Copyright © 2009 David Roden
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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import net.pterodactylus.fcp.AddPeer;
import net.pterodactylus.fcp.ClientHello;
import net.pterodactylus.fcp.CloseConnectionDuplicateClientName;
import net.pterodactylus.fcp.EndListPeers;
import net.pterodactylus.fcp.FcpAdapter;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.ListPeers;
import net.pterodactylus.fcp.ModifyPeer;
import net.pterodactylus.fcp.NodeHello;
import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;
import net.pterodactylus.fcp.ProtocolError;

/**
 * High-level FCP client that hides the details of the underlying FCP
 * implementation.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpClient {

	/** Object used for synchronization. */
	private final Object syncObject = new Object();

	/** The name of this client. */
	private final String name;

	/** The underlying FCP connection. */
	private final FcpConnection fcpConnection;

	/**
	 * Creates an FCP client with the given name.
	 *
	 * @param name
	 *            The name of the FCP client
	 * @throws UnknownHostException
	 *             if the hostname “localhost” is unknown
	 */
	public FcpClient(String name) throws UnknownHostException {
		this(name, "localhost");
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param name
	 *            The name of the FCP client
	 * @param hostname
	 *            The hostname of the Freenet node
	 * @throws UnknownHostException
	 *             if the given hostname can not be resolved
	 */
	public FcpClient(String name, String hostname) throws UnknownHostException {
		this(name, hostname, FcpConnection.DEFAULT_PORT);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param name
	 *            The name of the FCP client
	 * @param hostname
	 *            The hostname of the Freenet node
	 * @param port
	 *            The Freenet node’s FCP port
	 * @throws UnknownHostException
	 *             if the given hostname can not be resolved
	 */
	public FcpClient(String name, String hostname, int port) throws UnknownHostException {
		this(name, InetAddress.getByName(hostname), port);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param name
	 *            The name of the FCP client
	 * @param host
	 *            The host address of the Freenet node
	 */
	public FcpClient(String name, InetAddress host) {
		this(name, host, FcpConnection.DEFAULT_PORT);
	}

	/**
	 * Creates an FCP client.
	 *
	 * @param name
	 *            The name of the FCP client
	 * @param host
	 *            The host address of the Freenet node
	 * @param port
	 *            The Freenet node’s FCP port
	 */
	public FcpClient(String name, InetAddress host, int port) {
		this.name = name;
		fcpConnection = new FcpConnection(host, port);
	}

	//
	// ACTIONS
	//

	/**
	 * Connects the FCP client.
	 *
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void connect() throws IOException, FcpException {
		ExtendedFcpAdapter fcpListener = new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedNodeHello(FcpConnection fcpConnection, NodeHello nodeHello) {
				completionLatch.countDown();
			}
		};
		fcpConnection.addFcpListener(fcpListener);
		try {
			fcpConnection.connect();
			ClientHello clientHello = new ClientHello(name);
			fcpConnection.sendMessage(clientHello);
			while (true) {
				try {
					fcpListener.complete();
					break;
				} catch (InterruptedException e) {
					/* ignore, we’ll loop. */
				}
			}
		} finally {
			fcpConnection.removeFcpListener(fcpListener);
		}
		if (fcpListener.getFcpException() != null) {
			throw fcpListener.getFcpException();
		}
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

	//
	// PEER MANAGEMENT
	//

	/**
	 * Returns all peers that the node has.
	 *
	 * @return A set containing the node’s peers
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Set<Peer> getPeers() throws IOException, FcpException {
		final Set<Peer> peers = new HashSet<Peer>();
		ExtendedFcpAdapter fcpListener = new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				peers.add(peer);
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedEndListPeers(FcpConnection fcpConnection, EndListPeers endListPeers) {
				completionLatch.countDown();
			}
		};
		fcpConnection.addFcpListener(fcpListener);
		fcpConnection.sendMessage(new ListPeers("list-peers"));
		try {
			while (true) {
				try {
					fcpListener.complete();
					break;
				} catch (InterruptedException e) {
					/* ignore, we’ll loop. */
				}
			}
		} finally {
			fcpConnection.removeFcpListener(fcpListener);
		}
		if (fcpListener.getFcpException() != null) {
			throw fcpListener.getFcpException();
		}
		return peers;
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
	private void addPeer(AddPeer addPeer) throws IOException, FcpException {
		ExtendedFcpAdapter fcpListener = new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				completionLatch.countDown();
			}
		};
		fcpConnection.addFcpListener(fcpListener);
		try {
			fcpConnection.sendMessage(addPeer);
			while (true) {
				try {
					fcpListener.complete();
					break;
				} catch (InterruptedException ie1) {
					/* ignore, we’ll loop. */
				}
			}
		} finally {
			fcpConnection.removeFcpListener(fcpListener);
		}
		if (fcpListener.getFcpException() != null) {
			throw fcpListener.getFcpException();
		}
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
	public void modifyPeer(Peer peer, Boolean allowLocalAddresses, Boolean disabled, Boolean listenOnly) throws IOException, FcpException {
		ExtendedFcpAdapter fcpListener = new ExtendedFcpAdapter() {

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void receivedPeer(FcpConnection fcpConnection, Peer peer) {
				completionLatch.countDown();
			}
		};
		fcpConnection.addFcpListener(fcpListener);
		try {
			fcpConnection.sendMessage(new ModifyPeer(peer.getIdentity(), allowLocalAddresses, disabled, listenOnly));
		} finally {
			fcpConnection.removeFcpListener(fcpListener);
		}
		if (fcpListener.getFcpException() != null) {
			throw fcpListener.getFcpException();
		}
	}

	/**
	 * Implementation of an {@link FcpListener} that can store an
	 * {@link FcpException} and wait for the arrival of a certain command.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	private static class ExtendedFcpAdapter extends FcpAdapter {

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
		 * Returns the FCP exception that occured. If no FCP exception occured,
		 * <code>null</code> is returned.
		 *
		 * @return The FCP exception that occured, or <code>null</code>
		 */
		public FcpException getFcpException() {
			return fcpException;
		}

		/**
		 * Waits for the completion of the command.
		 *
		 * @throws InterruptedException
		 *             if {@link CountDownLatch#await()} is interrupted
		 */
		public void complete() throws InterruptedException {
			completionLatch.await();
		}

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
