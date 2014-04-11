/*
 * jFCPlib - Identity.java - Copyright © 2009–2014 David Roden
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
 * Wrapper around a web-of-trust identity.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Identity {

	/** The identity’s identifier. */
	private final String identifier;

	/** The identity’s nickname. */
	private final String nickname;

	/** The identity’s request URI. */
	private final String requestUri;

	/**
	 * Creates a new identity.
	 *
	 * @param identifier
	 *            The identifies of the identity
	 * @param nickname
	 *            The nickname of the identity
	 * @param requestUri
	 *            The request URI of the identity
	 */
	public Identity(String identifier, String nickname, String requestUri) {
		this.identifier = identifier;
		this.nickname = nickname;
		this.requestUri = requestUri;
	}

	/**
	 * Returns the identifier of this identity.
	 *
	 * @return This identity’s identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the nickname of this identity.
	 *
	 * @return This identity’s nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * Returns the request URI of this identity.
	 *
	 * @return This identity’s request URI
	 */
	public String getRequestUri() {
		return requestUri;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		Identity identity = (Identity) obj;
		return identifier.equals(identity.identifier);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

}
