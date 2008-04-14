/*
 * fcplib - HighLevelCallback.java -
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
import java.util.List;

/**
 * A callback is used to notify users of the {@link HighLevelClient} that an
 * operation was completed.
 * 
 * @param <R>
 *            The type of the high-level operation result
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id$
 */
public class HighLevelCallback<R extends HighLevelResult> {

	/** Object used for synchronization. */
	public final Object syncObject = new Object();

	/** The list of callback listeners. */
	public final List<HighLevelCallbackListener<R>> highLevelCallbackListeners = Collections.synchronizedList(new ArrayList<HighLevelCallbackListener<R>>());

	/** The result of the operation. */
	public R result = null;

	/**
	 * Adds a callback listener to this callback. The callback listener will be
	 * called as soon as the result of the operation is known. If the result is
	 * already known, the listener will be called immediately. Also, as soon as
	 * a listener was notified the listener is automatically removed from this
	 * callback.
	 * 
	 * @param highLevelCallbackListener
	 *            The listener to add
	 */
	public void addHighLevelCallbackListener(HighLevelCallbackListener<R> highLevelCallbackListener) {
		highLevelCallbackListeners.add(highLevelCallbackListener);
		if (isDone()) {
			fireGotResult();
		}
	}

	/**
	 * Removes a callback listener from this callback.
	 * 
	 * @param highLevelCallbackListener
	 *            The listener to remove
	 */
	public void removeHighLevelCallbackListener(HighLevelCallbackListener<R> highLevelCallbackListener) {
		highLevelCallbackListeners.remove(highLevelCallbackListener);
	}

	/**
	 * Notifies all listeners that the result of the operation is now known.
	 * 
	 * @see HighLevelCallbackListener#gotResult(HighLevelCallback)
	 */
	private synchronized void fireGotResult() {
		for (HighLevelCallbackListener<R> highLevelCallbackListener: highLevelCallbackListeners) {
			highLevelCallbackListeners.remove(highLevelCallbackListener);
			highLevelCallbackListener.gotResult(this);
		}
	}

	/**
	 * Returns whether the result of the operation is already known. If the
	 * result is already known a call to {@link #getResult()} will not block.
	 * 
	 * @return <code>true</code> if the result is already known,
	 *         <code>false</code> otherwise
	 */
	public boolean isDone() {
		synchronized (syncObject) {
			return result != null;
		}
	}

	/**
	 * Returns the result of the operation, blocking until it is known.
	 * 
	 * @return The result of the operation
	 * @throws InterruptedException
	 *             if {@link Object#wait()} was interrupted
	 */
	public R getResult() throws InterruptedException {
		return getResult(0);
	}

	/**
	 * Returns the result of the operation, blocking until it is known or the
	 * given time (in milliseconds) has passed.
	 * 
	 * @param waitTime
	 *            The time to wait for a result
	 * @return The result of the operation, or <code>null</code> if the result
	 *         is still not known after <code>waitTime</code> milliseconds
	 *         have passed
	 * @throws InterruptedException
	 *             if {@link Object#wait()} is interrupted
	 */
	public R getResult(long waitTime) throws InterruptedException {
		synchronized (syncObject) {
			if (result == null) {
				syncObject.wait(waitTime);
			}
			return result;
		}
	}

	/**
	 * Sets the result of the operation. Calling this method will result in all
	 * listeners being notified.
	 * 
	 * @see #fireGotResult()
	 * @param result
	 *            The result of the operation
	 */
	void setResult(R result) {
		synchronized (syncObject) {
			this.result = result;
			syncObject.notifyAll();
		}
		fireGotResult();
	}

}
