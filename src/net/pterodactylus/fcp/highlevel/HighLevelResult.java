/*
 * fcplib - HighLevelResult.java -
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
 * Base class for results of {@link HighLevelClient} operations.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public abstract class HighLevelResult {

	/** The identifier of the request. */
	private final String identifier;

	/** Whether the operation failed. */
	private boolean failed;

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	HighLevelResult(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Returns the identifier of the underlying request.
	 * 
	 * @return The identifier of the request
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns whether the operation failed.
	 * 
	 * @return <code>true</code> if the operation failed, <code>false</code>
	 *         otherwise
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Sets whether the operation failed.
	 * 
	 * @param failed
	 *            <code>true</code> if the operation failed,
	 *            <code>false</code> otherwise
	 */
	void setFailed(boolean failed) {
		this.failed = failed;
	}

}
