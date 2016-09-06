/*
 * jFCPlib - SSKKeypair.java - Copyright © 2008–2016 David Roden
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
 * An “SSKKeypair” message that is sent as a response to a {@link GenerateSSK}
 * message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SSKKeypair extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “SSKKeypair” message that wraps the received message.
	 *
	 * @param receivedMessage
	 *            The received message
	 */
	public SSKKeypair(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The identifier of the request
	 */
	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	/**
	 * Returns the URI that must be used to insert data.
	 *
	 * @return The insert URI
	 */
	public String getInsertURI() {
		return getField("InsertURI");
	}

	/**
	 * Returns the URI that must be used to request data.
	 *
	 * @return The request URI
	 */
	public String getRequestURI() {
		return getField("RequestURI");
	}

}
