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
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelProgress extends HighLevelResult {

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
