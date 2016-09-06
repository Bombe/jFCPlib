/*
 * jFCPlib - ModifyPeer.java - Copyright © 2008–2016 David Roden
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
			setAllowLocalAddresses(allowLocalAddresses);
		}
		if (disabled != null) {
			setEnabled(!disabled);
		}
		if (listenOnly != null) {
			setListenOnly(listenOnly);
		}
	}

	public void setAllowLocalAddresses(boolean allowLocalAddresses) {
		setField("AllowLocalAddresses", String.valueOf(allowLocalAddresses));
	}

	public void setEnabled(boolean enabled) {
		setField("IsDisabled", String.valueOf(!enabled));
	}

	public void setListenOnly(boolean listenOnly) {
		setField("IsListenOnly", String.valueOf(listenOnly));
	}

	public void setBurstOnly(boolean burstOnly) {
		setField("IsBurstOnly", String.valueOf(burstOnly));
	}

	public void setIgnoreSource(boolean ignoreSource) {
		setField("IgnoreSourcePort", String.valueOf(ignoreSource));
	}

}
