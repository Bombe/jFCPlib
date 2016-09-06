/*
 * jFCPlib - SendTextFeed.java - Copyright © 2009–2016 David Roden
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
 * The “SendTextFeed” command sends an arbitrary text to a peer node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SendTextFeed extends AbstractSendFeedMessage {

	/**
	 * Creates a new “SendTextFeed” command.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param nodeIdentifier
	 *            The identifier of the peer node
	 * @param text
	 *            The text to send
	 */
	public SendTextFeed(String identifier, String nodeIdentifier, String text) {
		super("SendTextFeed", identifier, nodeIdentifier);
		setField("Text", text);
	}

}
