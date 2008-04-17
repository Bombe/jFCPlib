/*
 * jFCPlib-high-level-client - PeerResult.java -
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

import net.pterodactylus.fcp.Peer;

/**
 * The peer result is the result of several operations:
 * {@link HighLevelClient#addPeer(String)},
 * {@link HighLevelClient#addPeer(java.net.URL)}, or
 * {@link HighLevelClient#addPeer(net.pterodactylus.fcp.NodeRef)}.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class PeerResult extends HighLevelResult {

	/** The peer. */
	private Peer peer;

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	PeerResult(String identifier) {
		super(identifier);
	}

	/**
	 * Returns the peer.
	 * 
	 * @return The peer
	 */
	public Peer getPeer() {
		return peer;
	}

	/**
	 * Sets the peer.
	 * 
	 * @param peer
	 *            The peer
	 */
	void setPeer(Peer peer) {
		this.peer = peer;
	}

}
