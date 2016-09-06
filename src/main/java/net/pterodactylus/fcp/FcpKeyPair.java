/*
 * jFCPlib - FcpKeyPair.java - Copyright © 2008–2016 David Roden
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
 * Container for an SSK keypair.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpKeyPair {

	/** The public key. */
	private final String publicKey;

	/** The private key. */
	private final String privateKey;

	/**
	 * Creates a new keypair from the given keys.
	 *
	 * @param publicKey
	 *            The public key
	 * @param privateKey
	 *            The private key
	 */
	public FcpKeyPair(String publicKey, String privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * Returns the public key of this keypair.
	 *
	 * @return The public key
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * Returns the private key of this keypair.
	 *
	 * @return The private key
	 */
	public String getPrivateKey() {
		return privateKey;
	}

}
