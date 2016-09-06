/*
 * jFCPlib - PluginRemoved.java - Copyright © 2008–2016 David Roden
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
 * The “PluginRemoved” message is a reply to the {@link RemovePlugin} request.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PluginRemoved extends BaseMessage implements Identifiable {

	/**
	 * Creates a new “PluginRemoved” message that wraps the received message.
	 *
	 * @param receivedMessage
	 * 	The received message
	 */
	public PluginRemoved(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	/**
	 * Returns the name of the plugin.
	 *
	 * @return The name of the plugin
	 */
	public String getPluginName() {
		return getField("PluginName");
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

}
