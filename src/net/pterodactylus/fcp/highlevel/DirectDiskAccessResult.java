/*
 * jFCPlib-high-level-client - DDAResult.java -
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
 * The result of a direct disk access check.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class DirectDiskAccessResult extends HighLevelResult {

	/** Whether reading is allowed. */
	private boolean readAllowed;

	/** Whether writing is allowed. */
	private boolean writeAllowed;

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	DirectDiskAccessResult(String identifier) {
		super(identifier);
	}

	/**
	 * Returns whether reading the directory is allowed.
	 * 
	 * @return <code>true</code> if the client is allowed to read from the
	 *         directory, <code>false</code> otherwise
	 */
	public boolean isReadAllowed() {
		return readAllowed;
	}

	/**
	 * Sets whether reading the directory is allowed.
	 * 
	 * @param readAllowed
	 *            <code>true</code> if the client is allowed to read from the
	 *            directory, <code>false</code> otherwise
	 */
	void setReadAllowed(boolean readAllowed) {
		this.readAllowed = readAllowed;
	}

	/**
	 * Returns whether writing to the directory is allowed.
	 * 
	 * @return <code>true</code> if the client is allowed to write to the
	 *         directory, <code>false</code> otherwise
	 */
	public boolean isWriteAllowed() {
		return writeAllowed;
	}

	/**
	 * Sets whether writing to the directory is allowed.
	 * 
	 * @param writeAllowed
	 *            <code>true</code> if the client is allowed to write to the
	 *            directory, <code>false</code> otherwise
	 */
	void setWriteAllowed(boolean writeAllowed) {
		this.writeAllowed = writeAllowed;
	}

}
