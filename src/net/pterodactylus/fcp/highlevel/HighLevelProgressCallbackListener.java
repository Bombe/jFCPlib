/*
 * jFCPlib-high-level-client - HighLevelContinuousCallbackListener.java -
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
 * Interface for objects that want to be notified as soon as a lengthy operation
 * made some progress.
 * 
 * @param
 * <P>
 * The type of the high-level progress
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 * @version $Id: HighLevelContinuousCallbackListener.java 29 2008-04-15
 *          23:15:49Z bombe $
 */
public interface HighLevelProgressCallbackListener<P extends HighLevelProgress> extends HighLevelCallbackListener<P> {

	/**
	 * Notifies a listener that a progress message has been received.
	 * 
	 * @param highLevelContinuousCallback
	 *            The callback that made the progress
	 */
	public void gotProgress(HighLevelProgressCallback<P> highLevelContinuousCallback);

}
