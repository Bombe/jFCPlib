/*
 * jFCPlib - FcpClientListenerManager.java - Copyright © 2009 David Roden
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

import net.pterodactylus.util.event.AbstractListenerManager;

/**
 * Manages {@link FcpClientListener}s and fires events.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpClientListenerManager extends AbstractListenerManager<FcpClient, FcpClientListener> {

	/**
	 * Creates a new FCP client listener manager.
	 *
	 * @param fcpClient
	 *            The source FCP client
	 */
	public FcpClientListenerManager(FcpClient fcpClient) {
		super(fcpClient);
	}

	/**
	 * Notifies all listeners that the FCP client was disconnected.
	 *
	 * @see FcpClientListener#fcpClientDisconnected(FcpClient)
	 */
	public void fireFcpClientDisconnected() {
		for (FcpClientListener fcpClientListener : getListeners()) {
			fcpClientListener.fcpClientDisconnected(getSource());
		}
	}

}
