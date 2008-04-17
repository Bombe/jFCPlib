/*
 * jFCPlib-high-level-client - KeyGenerationResult.java -
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
 * Result of a {@link HighLevelClient#generateKey()} operation.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class KeyGenerationResult extends HighLevelResult {

	/** The insert URI. */
	private String insertURI;

	/** The request URI. */
	private String requestURI;

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	KeyGenerationResult(String identifier) {
		super(identifier);
	}

	/**
	 * Returns the insert URI.
	 * 
	 * @return The insert URI
	 */
	public String getInsertURI() {
		return insertURI;
	}

	/**
	 * Sets the insert URI.
	 * 
	 * @param insertURI
	 *            The insert URI
	 */
	void setInsertURI(String insertURI) {
		this.insertURI = insertURI;
	}

	/**
	 * Returns the request URI.
	 * 
	 * @return The request URI
	 */
	public String getRequestURI() {
		return requestURI;
	}

	/**
	 * Sets the request URI.
	 * 
	 * @param requestURI
	 *            The request URI
	 */
	void setRequestURI(String requestURI) {
		this.requestURI = requestURI;
	}

}
