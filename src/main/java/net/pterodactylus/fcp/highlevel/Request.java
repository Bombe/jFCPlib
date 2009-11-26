/*
 * jFCPlib - Request.java - Copyright © 2009 David Roden
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

import net.pterodactylus.fcp.PersistentGet;
import net.pterodactylus.fcp.PersistentPut;

/**
 * Wrapper class around request responses from the node, such as
 * {@link PersistentGet} or {@link PersistentPut}.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public abstract class Request {

	/** The identifier of the request. */
	private final String identifier;

	/** The client token of the request. */
	private final String clientToken;

	/** Whether the request is on the global queue. */
	private final boolean global;

	/** Whether the get request is complete. */
	private boolean complete;

	/** Whether the get request has failed. */
	private boolean failed;

	/** The data length. */
	private long length;

	/** The mime type. */
	private String contentType;

	/** The error code in case of failure. */
	private int errorCode;

	/** Whether the failure is fatal. */
	private boolean fatal;

	/** The total number of blocks. */
	private int totalBlocks;

	/** The required number of blocks. */
	private int requiredBlocks;

	/** The successfully processed number of blocks. */
	private int succeededBlocks;

	/** The number of failed blocks. */
	private int failedBlocks;

	/** The number of fatally failed blocks. */
	private int fatallyFailedBlocks;

	/** Whether the total number of blocks is finalized. */
	private boolean finalizedTotal;

	/**
	 * Creates a new request with the given identifier and client token.
	 *
	 * @param identifier
	 *            The identifier of the request
	 * @param clientToken
	 *            The client token of the request
	 * @param global
	 *            <code>true</code> if the request is on the global queue,
	 *            <code>false</code> otherwise
	 */
	protected Request(String identifier, String clientToken, boolean global) {
		this.identifier = identifier;
		this.clientToken = clientToken;
		this.global = global;
	}

	/**
	 * Returns the identifier of the request.
	 *
	 * @return The request’s identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the client token of the request.
	 *
	 * @return The request’s client token
	 */
	public String getClientToken() {
		return clientToken;
	}

	/**
	 * Returns whether this request is on the global queue.
	 *
	 * @return <code>true</code> if the request is on the global queue,
	 *         <code>false</code> otherwise
	 */
	public boolean isGlobal() {
		return global;
	}

	/**
	 * Returns whether this request is complete.
	 *
	 * @return <code>true</code> if this request is complete, false otherwise
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets whether this request is complete.
	 *
	 * @param complete
	 *            <code>true</code> if this request is complete, false otherwise
	 */
	void setComplete(boolean complete) {
		this.complete = complete;
	}

	/**
	 * Returns whether this request has failed. This method should only be
	 * called if {@link #isComplete()} returns <code>true</code>.
	 *
	 * @return <code>true</code> if this request failed, <code>false</code>
	 *         otherwise
	 */
	public boolean hasFailed() {
		return failed;
	}

	/**
	 * Sets whether this request has failed.
	 *
	 * @param failed
	 *            <code>true</code> if this request failed, <code>false</code>
	 *            otherwise
	 */
	void setFailed(boolean failed) {
		this.failed = failed;
	}

	/**
	 * Returns the length of the data.
	 *
	 * @return The length of the data
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Sets the length of the data.
	 *
	 * @param length
	 *            The length of the data
	 */
	void setLength(long length) {
		this.length = length;
	}

	/**
	 * Returns the content type of the data.
	 *
	 * @return The content type of the data
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type of the data.
	 *
	 * @param contentType
	 *            The content type of the data
	 */
	void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Returns the error code. This method should only be called if
	 * {@link #hasFailed()} returns <code>true</code>.
	 *
	 * @return The error code
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Sets the error code.
	 *
	 * @param errorCode
	 *            The error code
	 */
	void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * Returns whether this request has fatally failed, i.e. repitition will not
	 * cause the request to succeed.
	 *
	 * @return <code>true</code> if this request can not be made succeed by
	 *         repeating, <code>false</code> otherwise
	 */
	public boolean isFatal() {
		return fatal;
	}

	/**
	 * Sets whether this request has fatally failed.
	 *
	 * @param fatal
	 *            <code>true</code> if this request failed fatally,
	 *            <code>false</code> otherwise
	 */
	void setFatal(boolean fatal) {
		this.fatal = fatal;
	}

	/**
	 * Returns the total number of blocks of this request.
	 *
	 * @return This request’s total number of blocks
	 */
	public int getTotalBlocks() {
		return totalBlocks;
	}

	/**
	 * Sets the total number of blocks of this request.
	 *
	 * @param totalBlocks
	 *            This request’s total number of blocks
	 */
	void setTotalBlocks(int totalBlocks) {
		this.totalBlocks = totalBlocks;
	}

	/**
	 * Returns the number of required blocks. Any progress percentages should be
	 * calculated against this value as 100%. Also, as long as
	 * {@link #isFinalizedTotal()} returns {@code false} this value might
	 * change.
	 *
	 * @return The number of required blocks
	 */
	public int getRequiredBlocks() {
		return requiredBlocks;
	}

	/**
	 * Sets the number of required blocks.
	 *
	 * @param requiredBlocks
	 *            The number of required blocks
	 */
	void setRequiredBlocks(int requiredBlocks) {
		this.requiredBlocks = requiredBlocks;
	}

	/**
	 * Returns the number of succeeded blocks.
	 *
	 * @return The number of succeeded blocks
	 */
	public int getSucceededBlocks() {
		return succeededBlocks;
	}

	/**
	 * Sets the number of succeeded blocks.
	 *
	 * @param succeededBlocks
	 *            The number of succeeded blocks
	 */
	void setSucceededBlocks(int succeededBlocks) {
		this.succeededBlocks = succeededBlocks;
	}

	/**
	 * Returns the number of failed blocks. These blocks may be retried untill
	 * the maximum number of retries has been reached.
	 *
	 * @return The number of failed blocks
	 */
	public int getFailedBlocks() {
		return failedBlocks;
	}

	/**
	 * Sets the number of failed blocks.
	 *
	 * @param failedBlocks
	 *            The number of failed blocks
	 */
	void setFailedBlocks(int failedBlocks) {
		this.failedBlocks = failedBlocks;
	}

	/**
	 * Returns the number of fatally failed blocks.
	 *
	 * @return The number of fatally failed blocks
	 */
	public int getFatallyFailedBlocks() {
		return fatallyFailedBlocks;
	}

	/**
	 * Sets the number of fatally failed blocks.
	 *
	 * @param fatallyFailedBlocks
	 *            The number of fatally failed blocks
	 */
	void setFatallyFailedBlocks(int fatallyFailedBlocks) {
		this.fatallyFailedBlocks = fatallyFailedBlocks;
	}

	/**
	 * Returns whether the number of blocks has been finalized.
	 *
	 * @return {@code true} if the number of blocks is finalized, {@code false}
	 *         otherwise
	 */
	public boolean isFinalizedTotal() {
		return finalizedTotal;
	}

	/**
	 * Sets whether the number of blocks has been finalized.
	 *
	 * @param finalizedTotal
	 *            {@code true} if the number of blocks has been finalized,
	 *            {@code false} otherwise
	 */
	void setFinalizedTotal(boolean finalizedTotal) {
		this.finalizedTotal = finalizedTotal;
	}

}
