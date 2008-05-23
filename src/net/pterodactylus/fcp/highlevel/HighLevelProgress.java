/*
 * jFCPlib-high-level-client - HighLevelContinuosResult.java -
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

/**
 * Result for operations that send progress messages until they have completed.
 * The fields of the progress message has to be checked in given order because
 * if you receive this progress asynchronously via a
 * {@link HighLevelProgressListener} the progress will not have any state, you
 * will simply get the latest results, with other fields unset. First you should
 * check whether {@link #isFinished()} returns <code>true</code>. If it does,
 * the request is finished and {@link #isFailed()} will tell you whether the
 * request has failed or succeeded. Other fields are not set. If the request is
 * not yet finished, {@link #isFetchable()} will tell you whether the request
 * has progressed to a state that allows other clients to fetch the inserted
 * data. This is of course only valid for Put and PutDir requests. If none of
 * those methods return <code>true</code>, you can use the block count
 * methods to get detailed progress statistics. When progress you received is a
 * {@link DownloadResult} you do not need to check
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelProgress extends HighLevelResult {

	/** Whether the request is finished. */
	private boolean finished;

	/** Whether a Put request should be fetchable now. */
	private boolean fetchable;

	/** The number of total blocks. */
	private int totalBlocks;

	/** The number of required blocks. */
	private int requiredBlocks;

	/** The number of successfully transferred blocks. */
	private int successfulBlocks;

	/** The number of failed blocks. */
	private int failedBlocks;

	/** The number of fatally failed blocks. */
	private int fatallyFailedBlocks;

	/** Whether the total number is finalized. */
	private boolean totalFinalized;

	/**
	 * Package-private constructor.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 */
	public HighLevelProgress(String identifier) {
		super(identifier);
	}

	/**
	 * Creates a new high-level progress for a request that is finished.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 * @param successful
	 *            <code>true</code> if the request finished successfully,
	 *            <code>false</code> otherwise
	 */
	public HighLevelProgress(String identifier, boolean successful) {
		this(identifier);
		finished = true;
		setFailed(!successful);
	}

	/**
	 * Creates a new high-level progress with the given values.
	 * 
	 * @param identifier
	 *            The identifier of the request
	 * @param totalBlocks
	 *            The total number of blocks
	 * @param requiredBlocks
	 *            The number of required blocks
	 * @param successfulBlocks
	 *            The number of successful blocks
	 * @param failedBlocks
	 *            The number of failed blocks
	 * @param fatallyFailedBlocks
	 *            The number of fatally failed blocks
	 * @param totalFinalized
	 *            <code>true</code> if the total number of blocks is
	 *            finalized, <code>false</code> otherwise
	 */
	public HighLevelProgress(String identifier, int totalBlocks, int requiredBlocks, int successfulBlocks, int failedBlocks, int fatallyFailedBlocks, boolean totalFinalized) {
		this(identifier);
		this.totalBlocks = totalBlocks;
		this.requiredBlocks = requiredBlocks;
		this.successfulBlocks = successfulBlocks;
		this.failedBlocks = failedBlocks;
		this.fatallyFailedBlocks = fatallyFailedBlocks;
		this.totalFinalized = totalFinalized;
	}

	/**
	 * Returns whether this progress means that a request has finished. Use
	 * {@link #isFailed()} to check if the request failed.
	 * 
	 * @see #isFailed()
	 * @return <code>true</code> if the request has finished
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Sets whether the request described by this progress has finished.
	 * 
	 * @param finished
	 *            <code>true</code> if the request has finished,
	 *            <code>false</code> otherwise
	 */
	void setFinished(boolean finished) {
		this.finished = finished;
	}

	/**
	 * Returns whether the request should be fetchable now, in case it was a Put
	 * request.
	 * 
	 * @return <code>true</code> if the request should be fetchable now,
	 *         <code>false</code> otherwise
	 */
	public boolean isFetchable() {
		return fetchable;
	}

	/**
	 * Sets whether the request should be fetchable now, in case it was a Put
	 * request.
	 * 
	 * @param fetchable
	 *            <code>true</code> if the request should be fetchable now,
	 *            <code>false</code> otherwise
	 */
	void setFetchable(boolean fetchable) {
		this.fetchable = fetchable;
	}

	/**
	 * Returns the number of total blocks.
	 * 
	 * @return The number of total blocks
	 */
	public int getTotalBlocks() {
		return totalBlocks;
	}

	/**
	 * Sets the number of total blocks.
	 * 
	 * @param totalBlocks
	 *            The number of total blocks
	 */
	void setTotalBlocks(int totalBlocks) {
		this.totalBlocks = totalBlocks;
	}

	/**
	 * Returns the number of required blocks. For downloads, this number is
	 * smaller than {@link #getTotalBlocks()}.
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
	 * Returns the number of successfully transferred blocks.
	 * 
	 * @return The number of successfully transferred blocks
	 */
	public int getSuccessfulBlocks() {
		return successfulBlocks;
	}

	/**
	 * Sets the number of successfully transferred blocks.
	 * 
	 * @param successfulBlocks
	 *            The number of successfully transferred blocks
	 */
	void setSuccessfulBlocks(int successfulBlocks) {
		this.successfulBlocks = successfulBlocks;
	}

	/**
	 * Returns the number of failed blocks. Blocks that have failed can be
	 * retried.
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
	 * Returns the number of fatally failed blocks. Fatally failed blocks will
	 * never complete, even with endless retries.
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
	 *            The number fatally failed blocks
	 */
	void setFatallyFailedBlocks(int fatallyFailedBlocks) {
		this.fatallyFailedBlocks = fatallyFailedBlocks;
	}

	/**
	 * Returns whether the result of {@link #getTotalBlocks()} is final, i.e. it
	 * won’t change anymore.
	 * 
	 * @return <code>true</code> if the result of {@link #getTotalBlocks()} is
	 *         final, <code>false</code> otherwise
	 */
	public boolean isTotalFinalized() {
		return totalFinalized;
	}

	/**
	 * Sets whether the result of {@link #getTotalBlocks()} is final, i.e. it
	 * won’t change anymore.
	 * 
	 * @param totalFinalized
	 *            <code>true</code> if the result of {@link #getTotalBlocks()}
	 *            is final, <code>false</code> otherwise
	 */
	void setTotalFinalized(boolean totalFinalized) {
		this.totalFinalized = totalFinalized;
	}

}
