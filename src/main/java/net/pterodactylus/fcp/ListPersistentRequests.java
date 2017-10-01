/*
 * jFCPlib - ListPersistentRequests.java - Copyright © 2008–2016 David Roden
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
 * Command to tell the node to list all persistent requests from the current
 * queue, which is either the global queue or the client-local queue, depending
 * on your {@link WatchGlobal} status.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ListPersistentRequests extends FcpMessage {

	/**
	 * Creates a new “ListPersistentRequests” command that lists all persistent
	 * requests in the current queue.
	 */
	public ListPersistentRequests() {
		super("ListPersistentRequests");
	}

}
