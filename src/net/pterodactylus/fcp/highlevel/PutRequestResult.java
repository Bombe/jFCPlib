/*
 * jFCPlib-high-level-client - PutRequestResult.java -
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

import net.pterodactylus.fcp.Persistence;
import net.pterodactylus.fcp.PersistentPut;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.UploadFrom;
import net.pterodactylus.fcp.Verbosity;

/**
 * A put request result will be contained in a {@link RequestListResult}.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class PutRequestResult extends RequestResult {

	/** The wrapped PersistentPut FCP message. */
	private final PersistentPut persistentPut;

	/**
	 * Creates a new put request result.
	 * 
	 * @param persistentPut
	 *            The PersistentPut FCP message to wrap
	 */
	public PutRequestResult(PersistentPut persistentPut) {
		super(persistentPut.getIdentifier());
		this.persistentPut = persistentPut;
	}

	/**
	 * Returns the client token associated with the request.
	 * 
	 * @return The client token
	 * @see net.pterodactylus.fcp.PersistentPut#getClientToken()
	 */
	public String getClientToken() {
		return persistentPut.getClientToken();
	}

	/**
	 * Returns the length of the data to be inserted.
	 * 
	 * @return The length of the data
	 * @see net.pterodactylus.fcp.PersistentPut#getDataLength()
	 */
	public long getDataLength() {
		return persistentPut.getDataLength();
	}

	/**
	 * Returns the maximum number of retries for failed blocks.
	 * 
	 * @return The maximum number of retries
	 * @see net.pterodactylus.fcp.PersistentPut#getMaxRetries()
	 */
	public int getMaxRetries() {
		return persistentPut.getMaxRetries();
	}

	/**
	 * Returns the content type of the data.
	 * 
	 * @return The content type of the data
	 * @see net.pterodactylus.fcp.PersistentPut#getMetadataContentType()
	 */
	public String getMetadataContentType() {
		return persistentPut.getMetadataContentType();
	}

	/**
	 * Returns the persistence level of the request.
	 * 
	 * @return The persistence level
	 * @see net.pterodactylus.fcp.PersistentPut#getPersistence()
	 */
	public Persistence getPersistence() {
		return persistentPut.getPersistence();
	}

	/**
	 * Returns the priority of the request.
	 * 
	 * @return The priority
	 * @see net.pterodactylus.fcp.PersistentPut#getPriority()
	 */
	public Priority getPriority() {
		return persistentPut.getPriority();
	}

	/**
	 * Returns the target filename of the request
	 * 
	 * @return The target filename
	 * @see net.pterodactylus.fcp.PersistentPut#getTargetFilename()
	 */
	public String getTargetFilename() {
		return persistentPut.getTargetFilename();
	}

	/**
	 * Returns the upload source of the request.
	 * 
	 * @return The upload source
	 * @see net.pterodactylus.fcp.PersistentPut#getUploadFrom()
	 */
	public UploadFrom getUploadFrom() {
		return persistentPut.getUploadFrom();
	}

	/**
	 * Returns the URI of the request.
	 * 
	 * @return The URI
	 * @see net.pterodactylus.fcp.PersistentPut#getURI()
	 */
	public String getURI() {
		return persistentPut.getURI();
	}

	/**
	 * Returns the verbosity of the request.
	 * 
	 * @return The verbosity
	 * @see net.pterodactylus.fcp.PersistentPut#getVerbosity()
	 */
	public Verbosity getVerbosity() {
		return persistentPut.getVerbosity();
	}

	/**
	 * Returns whether the request is on the global queue.
	 * 
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> if it is on the client-local queue
	 * @see net.pterodactylus.fcp.PersistentPut#isGlobal()
	 */
	public boolean isGlobal() {
		return persistentPut.isGlobal();
	}

	/**
	 * Returns whether the request has already started.
	 * 
	 * @return <code>true</code> if the request has started,
	 *         <code>false</code> otherwise
	 * @see net.pterodactylus.fcp.PersistentPut#isStarted()
	 */
	public boolean isStarted() {
		return persistentPut.isStarted();
	}

}
