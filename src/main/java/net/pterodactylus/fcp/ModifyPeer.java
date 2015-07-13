/*
 * jFCPlib - ModifyPeer.java - Copyright © 2008 David Roden
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

package net.pterodactylus.fcp;

/**
 * The “ModifyPeer” request lets you modify certain properties of a peer.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ModifyPeer extends FcpMessage {

	public ModifyPeer(String identifier, String nodeIdentifier) {
		super("ModifyPeer");
		setField("Identifier", identifier);
		setField("NodeIdentifier", nodeIdentifier);
	}

	@Deprecated
	public ModifyPeer(String nodeIdentifier, Boolean allowLocalAddresses, Boolean disabled, Boolean listenOnly) {
		super("ModifyPeer");
		setField("NodeIdentifier", nodeIdentifier);
		if (allowLocalAddresses != null) {
			setField("AllowLocalAddresses", String.valueOf(allowLocalAddresses));
		}
		if (disabled != null) {
			setField("IsDisabled", String.valueOf(disabled));
		}
		if (listenOnly != null) {
			setField("IsListenOnly", String.valueOf(listenOnly));
		}
	}

}
