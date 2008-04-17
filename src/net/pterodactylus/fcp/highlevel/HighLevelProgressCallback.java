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
 * @param
 * <P>
 * The type of the high-level progress
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelProgressCallback<P extends HighLevelProgress> extends HighLevelCallback<P> {

	/** Callback listeners. */
	private final List<HighLevelProgressCallbackListener<P>> highLevelContinuousCallbackListeners = new ArrayList<HighLevelProgressCallbackListener<P>>();

	/**
	 * Creates a new continuous callback with the given result.
	 * 
	 * @see HighLevelCallback#HighLevelCallback(HighLevelResult)
	 * @param progress
	 *            The result of the operation
	 */
	HighLevelProgressCallback(P progress) {
		super(progress);
	}

	//
	// EVENT MANAGEMENT
	//

	/**
	 * Adds a callback listener to this callback.
	 * 
	 * @param highLevelContinuousCallbackListener
	 *            The callback listener to add
	 */
	public void addHighLevelContinuousCallbackListener(HighLevelProgressCallbackListener<P> highLevelContinuousCallbackListener) {
		highLevelContinuousCallbackListeners.add(highLevelContinuousCallbackListener);
		fireGotProgress();
	}

	/**
	 * Removes a callback listener from this callback.
	 * 
	 * @param highLevelContinuousCallbackListener
	 *            The callback listener to remove
	 */
	public void removeHighLevelContinuousCallbackListener(HighLevelProgressCallbackListener<P> highLevelContinuousCallbackListener) {
		highLevelContinuousCallbackListeners.remove(highLevelContinuousCallbackListener);
	}

	/**
	 * Notifies all listeners that progress results have been received.
	 */
	private void fireGotProgress() {
		for (HighLevelProgressCallbackListener<P> highLevelContinuousCallbackListener: highLevelContinuousCallbackListeners) {
			highLevelContinuousCallbackListener.gotProgress(this);
		}
	}

	//
	// ACCESSORS
	//

	/**
	 * Waits for the next progress on this callback. Completion of the result
	 * also counts as progress.
	 * 
	 * @throws InterruptedException
	 *             if {@link Object#wait()} is interrupted
	 */
	public void waitForProgress() throws InterruptedException {
		synchronized (syncObject) {
			syncObject.wait();
		}
	}

	/**
	 * Waits for the given amount of time (in milliseconds) for the next
	 * progress on this callback. Completion of the result also counts as
	 * progress.
	 * 
	 * @param waitTime
	 *            The maximum time to wait for progress
	 * @throws InterruptedException
	 *             if {@link Object#wait()} is interrupted
	 */
	public void waitForProgress(long waitTime) throws InterruptedException {
		synchronized (syncObject) {
			syncObject.wait(waitTime);
		}
	}

	/**
	 * Notifies all listeners that the progress was updated.
	 */
	void progressUpdated() {
		synchronized (syncObject) {
			syncObject.notify();
		}
		fireGotProgress();
	}

}
