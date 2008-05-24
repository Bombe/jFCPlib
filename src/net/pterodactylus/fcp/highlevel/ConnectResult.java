/*
 * fcplib - ConnectResult.java -
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

/**
 * A result of the {@link HighLevelClient#connect(String)} operation.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ConnectResult extends HighLevelResult {

	/** Whether the node is now connected. */
	private boolean connected;

	/**
	 * Package-private constructor.
	 */
	ConnectResult() {
		super(null);
	}

	/**
	 * Returns whether the node is now connected.
	 * 
	 * @return <code>true</code> if the node is now connected,
	 *         <code>false</code> otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Sets whether the node is now connected.
	 * 
	 * @param connected
	 *            <code>true</code> if the node is now connected,
	 *            <code>false</code> otherwise
	 */
	void setConnected(boolean connected) {
		this.connected = connected;
	}

}
