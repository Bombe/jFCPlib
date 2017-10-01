/*
 * jFCPlib - PutRequest.java - Copyright © 2009–2016 David Roden
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

package net.pterodactylus.fcp.highlevel;

import net.pterodactylus.fcp.PersistentPut;

/**
 * High-level wrapper around a {@link PersistentPut}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PutRequest extends Request {

	/**
	 * Creates a new put request.
	 *
	 * @param persistentPut
	 *            The FCP message to wrap
	 */
	PutRequest(PersistentPut persistentPut) {
		super(persistentPut.getIdentifier(), persistentPut.getClientToken(), persistentPut.isGlobal());
	}

}
