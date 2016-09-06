/*
 * jFCPlib - SendDownloadFeed.java - Copyright © 2009–2016 David Roden
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
 * The “SendDownloadFeed” command sends information about a download to a peer
 * node.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SendDownloadFeed extends AbstractSendFeedMessage {

	/**
	 * Creates a new “SendDownloadFeed” to a peer node.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param nodeIdentifier
	 *            The identifier of the peer node
	 * @param uri
	 *            The URI of the download to send
	 * @param description
	 *            The description of the download (may be {@code null})
	 */
	public SendDownloadFeed(String identifier, String nodeIdentifier, String uri, String description) {
		super("SendDownloadFeed", identifier, nodeIdentifier);
		setField("URI", uri);
		setField("Description", description);
	}

}
