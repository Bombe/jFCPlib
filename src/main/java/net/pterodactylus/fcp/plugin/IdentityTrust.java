/*
 * jFCPlib - IdentityTrust.java - Copyright © 2009–2014 David Roden
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
 * Container for the trust given from one identity to another.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class IdentityTrust {

	/** The trust given to the identity. */
	private final byte trust;

	/** The command for the trust value. */
	private final String comment;

	/**
	 * Creates a new identity trust container.
	 *
	 * @param trust
	 *            The trust given to the identity
	 * @param comment
	 *            The comment for the trust value
	 */
	public IdentityTrust(byte trust, String comment) {
		this.trust = trust;
		this.comment = comment;
	}

	/**
	 * Returns the trust value given to the identity.
	 *
	 * @return The trust value
	 */
	public byte getTrust() {
		return trust;
	}

	/**
	 * Returns the comment for the trust value.
	 *
	 * @return The comment for the trust value
	 */
	public String getComment() {
		return comment;
	}

}
