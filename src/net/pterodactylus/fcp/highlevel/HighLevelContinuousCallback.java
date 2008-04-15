/*
 * jFCPlib-high-level-client - HighLevelContinousCallback.java -
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
import java.util.List;

/**
 * Callback for an operation that sends progress messages before completion.
 * 
 * @param <R>
 *            The type of the continuous result
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelContinuousCallback<R extends HighLevelContinuousResult> extends HighLevelCallback<R> {

	/** Callback listeners. */
	private final List<HighLevelContinuousCallbackListener<R>> highLevelContinuousCallbackListeners = new ArrayList<HighLevelContinuousCallbackListener<R>>();

	/** List of progress results. */
	private final List<R> progressQueue = new ArrayList<R>();

	/**
	 * Creates a new continuous callback with the given result.
	 * 
	 * @see HighLevelCallback#HighLevelCallback(HighLevelResult)
	 * @param progress
	 *            The result of the operation
	 */
	HighLevelContinuousCallback(R progress) {
		super(progress);
	}

	//
	// EVENT MANAGEMENT
	//

	/**
	 * Adds a callback listener to this callback. If this callback already has
	 * some progress results, the listener is notified immediately.
	 * 
	 * @param highLevelContinuousCallbackListener
	 *            The callback listener to add
	 */
	public void addHighLevelContinuousCallbackListener(HighLevelContinuousCallbackListener<R> highLevelContinuousCallbackListener) {
		highLevelContinuousCallbackListeners.add(highLevelContinuousCallbackListener);
		synchronized (progressQueue) {
			if (!progressQueue.isEmpty()) {
				fireGotProgress();
			}
		}
	}

	/**
	 * Removes a callback listener from this callback.
	 * 
	 * @param highLevelContinuousCallbackListener
	 *            The callback listener to remove
	 */
	public void removeHighLevelContinuousCallbackListener(HighLevelContinuousCallbackListener<R> highLevelContinuousCallbackListener) {
		highLevelContinuousCallbackListeners.remove(highLevelContinuousCallbackListener);
	}

	/**
	 * Notifies all listeners that progress results have been received.
	 */
	private void fireGotProgress() {
		for (HighLevelContinuousCallbackListener<R> highLevelContinuousCallbackListener: highLevelContinuousCallbackListeners) {
			highLevelContinuousCallbackListener.gotProgress(this);
		}
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the next progress result and removes it from the queue. If no
	 * progress result is yet available, this method will block.
	 * 
	 * @return The next progress result
	 * @throws InterruptedException
	 *             if {@link Object#wait()} is interrupted
	 */
	public R getProgress() throws InterruptedException {
		return getProgress(0);
	}

	/**
	 * Returns the next progress result and removes it from the queue. If no
	 * progress result is yet available, this method will block until either a
	 * progress result is available or the given time (in milliseconds) has
	 * passed.
	 * 
	 * @param waitTime
	 *            The maximum time to wait for a progress result
	 * @return The next progress result
	 * @throws InterruptedException
	 *             if {@link Object#wait()} is interrupted
	 */
	public R getProgress(long waitTime) throws InterruptedException {
		synchronized (progressQueue) {
			if (progressQueue.isEmpty()) {
				progressQueue.wait(waitTime);
			}
			return progressQueue.remove(0);
		}
	}

	/**
	 * Returns the latest progress result and clears the queue.
	 * 
	 * @return The latest progress result, or <code>null</code> if the queue
	 *         is empty
	 */
	public R getLatestProgress() {
		synchronized (progressQueue) {
			if (progressQueue.isEmpty()) {
				return null;
			}
			R latestProgress = progressQueue.get(progressQueue.size() - 1);
			progressQueue.clear();
			return latestProgress;
		}
	}

	/**
	 * Adds a progress result and notifies all listeners.
	 * 
	 * @param progress
	 *            The progress result to add
	 */
	void addProgress(R progress) {
		synchronized (progressQueue) {
			progressQueue.add(progress);
			progressQueue.notify();
		}
		fireGotProgress();
	}

}
