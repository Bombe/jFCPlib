/*
 * jFCPlib - BaseMessage.java - Copyright © 2008–2016 David Roden
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

import java.util.Map;

/**
 * A basic message abstraction that wraps a received FCP message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class BaseMessage {

	/** The received message, wrapped here. */
	private final FcpMessage receivedMessage;

	/**
	 * Creates a new base message that wraps the given message.
	 *
	 * @param receivedMessage
	 *            The FCP message that was received
	 */
	BaseMessage(FcpMessage receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	/**
	 * Returns the name of the message.
	 *
	 * @return The name of the message
	 */
	public String getName() {
		return receivedMessage.getName();
	}

	/**
	 * Returns the content of the field.
	 *
	 * @param field
	 *            The name of the field
	 * @return The content of the field, or <code>null</code> if there is no
	 *         such field
	 */
	public String getField(String field) {
		return receivedMessage.getField(field);
	}

	/**
	 * Returns all fields from the received message.
	 *
	 * @see FcpMessage#getFields()
	 * @return All fields from the message
	 */
	public Map<String, String> getFields() {
		return receivedMessage.getFields();
	}

}
