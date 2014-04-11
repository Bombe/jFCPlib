/*
 * jFCPlib - OwnIdentity.java - Copyright © 2009–2014 David Roden
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

package net.pterodactylus.fcp.plugin;

/**
 * Wrapper around a web-of-trust own identity.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class OwnIdentity extends Identity {

	/** The identity’s insert URI. */
	private final String insertUri;

	/**
	 * Creates a new web-of-trust own identity.
	 *
	 * @param identifier
	 *            The identifier of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 * @param insertUri
	 *            The insert URI of the identity
	 */
	public OwnIdentity(String identifier, String nickname, String requestUri, String insertUri) {
		super(identifier, nickname, requestUri);
		this.insertUri = insertUri;
	}

	/**
	 * Returns the insert URI of this identity.
	 *
	 * @return This identity’s insert URI
	 */
	public String getInsertUri() {
		return insertUri;
	}

}
