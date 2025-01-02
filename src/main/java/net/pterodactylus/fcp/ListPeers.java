/*
 * jFCPlib - ListPeers.java - Copyright © 2008–2016 David Roden
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

/**
 * The “ListPeer” requests asks the node for a list of all peers it has.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ListPeers extends FcpMessage {

	/**
	 * Creates a new “ListPeers” request that only includes basic data of the
	 * peers.
	 *
	 * @param identifier
	 *            The identifier of the request
	 */
	public ListPeers(String identifier) {
		super("ListPeers");
		setField("Identifier", identifier);
	}

	/**
	 * Creates a new “ListPeers” request that includes wanted data of the
	 * peers.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param withMetadata
	 *            If <code>true</code> metadata of the peers is included in the
	 *            reply
	 * @param withVolatile
	 *            if <code>true</code> volatile data of the peers is included
	 *            in the reply
	 * @deprecated Use {@link #ListPeers(String)},
	 * {@link #setWithMetadata(boolean)},
	 * and {@link #setWithVolatile(boolean)} instead.
	 */
	@Deprecated
	public ListPeers(String identifier, boolean withMetadata, boolean withVolatile) {
		this(identifier);
		setField("WithMetadata", String.valueOf(withMetadata));
		setField("WithVolatile", String.valueOf(withVolatile));
	}

	public void setWithMetadata(boolean withMetadata) {
		setField("WithMetadata", String.valueOf(withMetadata));
	}

	public void setWithVolatile(boolean withVolatile) {
		setField("WithVolatile", String.valueOf(withVolatile));
	}

}
