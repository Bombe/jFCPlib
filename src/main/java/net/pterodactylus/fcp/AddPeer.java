/*
 * jFCPlib - AddPeer.java - Copyright © 2008–2016 David Roden
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

import java.net.URL;

/**
 * The “AddPeer” request adds a peer to the node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class AddPeer extends FcpMessage {

	/**
	 * Creates a new “AddPeer” request.
	 */
	private AddPeer() {
		super("AddPeer");
	}

	/**
	 * Creates a new “AddPeer” request that reads the noderef of the peer from
	 * the given file.
	 *
	 * @param file
	 *            The file to read the noderef from
	 */
	public AddPeer(String file) {
		this();
		setField("File", file);
	}

	public AddPeer(String identifier, String file) {
		this(file);
		setField("Identifier", identifier);
	}

	/**
	 * Creates a new “AddPeer” request that reads the noderef of the peer from
	 * the given URL.
	 *
	 * @param url
	 *            The URL to read the noderef from
	 */
	public AddPeer(URL url) {
		this();
		setField("URL", String.valueOf(url));
	}

	public AddPeer(String identifier, URL url) {
		this(url);
		setField("Identifier", identifier);
	}

	/**
	 * Creates a new “AddPeer” request that adds the peer given by the noderef.
	 *
	 * @param nodeRef
	 *            The noderef of the peer
	 */
	public AddPeer(NodeRef nodeRef) {
		this();
		setNodeRef(nodeRef);
	}

	public AddPeer(String identifier, NodeRef nodeRef) {
		this(nodeRef);
		setField("Identifier", identifier);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Sets the noderef of the peer to add.
	 *
	 * @param nodeRef
	 *            The noderef of the peer
	 */
	private void setNodeRef(NodeRef nodeRef) {
		setField("lastGoodVersion", String.valueOf(nodeRef.getLastGoodVersion()));
		setField("opennet", String.valueOf(nodeRef.isOpennet()));
		setField("identity", nodeRef.getIdentity());
		setField("myName", nodeRef.getMyName());
		setField("location", String.valueOf(nodeRef.getLocation()));
		setField("testnet", String.valueOf(nodeRef.isTestnet()));
		setField("version", String.valueOf(nodeRef.getVersion()));
		setField("physical.udp", nodeRef.getPhysicalUDP());
		setField("ark.pubURI", nodeRef.getARK().getPublicURI());
		setField("ark.number", String.valueOf(nodeRef.getARK().getNumber()));
		setField("dsaPubKey.y", nodeRef.getDSAPublicKey());
		setField("dsaGroup.g", nodeRef.getDSAGroup().getBase());
		setField("dsaGroup.p", nodeRef.getDSAGroup().getPrime());
		setField("dsaGroup.q", nodeRef.getDSAGroup().getSubprime());
		setField("auth.negTypes", FcpUtils.encodeMultiIntegerField(nodeRef.getNegotiationTypes()));
		setField("sig", nodeRef.getSignature());
	}

}
