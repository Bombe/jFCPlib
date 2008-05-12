/*
 * jFCPlib-high-level-client - RequestListResult.java -
 * Copyright © 2008 David Roden
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

package net.pterodactylus.fcp.highlevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The request list results lists all currently running requests on a node.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class RequestListResult extends HighLevelResult implements Iterable<RequestResult> {

	/** The request results. */
	private List<RequestResult> requestResults = Collections.synchronizedList(new ArrayList<RequestResult>());

	/**
	 * Creates a new request list result.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	RequestListResult(String identifier) {
		super(identifier);
	}

	/**
	 * Adds a request result.
	 * 
	 * @param requestResult
	 *            The request result to add
	 */
	void addRequestResult(RequestResult requestResult) {
		requestResults.add(requestResult);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<RequestResult> iterator() {
		return requestResults.iterator();
	}

	/**
	 * Returns the request result at the given index.
	 * 
	 * @param index
	 *            The index of the request result
	 * @return The request result
	 */
	public RequestResult get(int index) {
		return requestResults.get(index);
	}

	/**
	 * Returns the number of request results in this request list.
	 * 
	 * @return The number of request results
	 */
	public int size() {
		return requestResults.size();
	}

}
