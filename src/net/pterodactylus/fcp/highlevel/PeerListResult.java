/*
 * jFCPlib-high-level-client - PeerListResult.java -
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.pterodactylus.fcp.Peer;

/**
 * The result of a {@link HighLevelClient#getPeers()} operation.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class PeerListResult extends HighLevelResult implements Iterable<Peer> {

	/** The list of peers. */
	private final List<Peer> peers = new ArrayList<Peer>();

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	PeerListResult(String identifier) {
		super(identifier);
	}

	/**
	 * Adds a peer to the list.
	 * 
	 * @param peer
	 *            The peer to add
	 */
	void addPeer(Peer peer) {
		peers.add(peer);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<Peer> iterator() {
		return peers.iterator();
	}

	/**
	 * Returns the peer at the given index.
	 * 
	 * @param index
	 *            The index of the peer
	 * @return The peer
	 * @see java.util.List#get(int)
	 */
	public Peer get(int index) {
		return peers.get(index);
	}

	/**
	 * Returns the size of the peer list.
	 * 
	 * @return The size of the peer list
	 * @see java.util.List#size()
	 */
	public int size() {
		return peers.size();
	}

}