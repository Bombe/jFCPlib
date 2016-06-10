/*
 * jFCPlib - SubscribeUSK.java - Copyright © 2008 David Roden
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

package net.pterodactylus.fcp;

/**
 * With a “SubscribeUSK” a client requests to be notified if the edition number
 * of a USK changes.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SubscribeUSK extends FcpMessage implements Identifiable {

	public SubscribeUSK(String identifier) {
		super("SubscribeUSK");
		setField("Identifier", identifier);
	}

	public SubscribeUSK(String uri, String identifier) {
		this(identifier);
		setField("URI", uri);
	}

	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	public String getUri() {
		return getField("URI");
	}

	public void setUri(String uri) {
		setField("URI", uri);
	}

	public boolean isActive() {
		return !Boolean.parseBoolean(getField("DontPoll"));
	}

	/**
	 * Sets whether updates for the USK are actively searched.
	 *
	 * @param active
	 *            <code>true</code> to actively search for newer editions,
	 *            <code>false</code> to only watch for newer editions that are
	 *            found from other requests
	 */
	public void setActive(boolean active) {
		setField("DontPoll", String.valueOf(!active));
	}

	public boolean isSparse() {
		return Boolean.valueOf(getField("SparsePoll"));
	}

	public void setSparse(boolean sparse) {
		setField("SparsePoll", String.valueOf(sparse));
	}

	public Priority getPriority() {
		String priorityClass = getField("PriorityClass");
		if (priorityClass != null) {
			return Priority.valueOf(priorityClass);
		}
		return Priority.bulkSplitfile;
	}

	public void setPriority(Priority priority) {
		setField("PriorityClass", priority.toString());
	}

	public Priority getActivePriority() {
		String priorityClass = getField("PriorityClassProgress");
		if (priorityClass != null) {
			return Priority.valueOf(priorityClass);
		}
		return Priority.update;
	}

	public void setActivePriority(Priority activePriority) {
		setField("PriorityClassProgress", activePriority.toString());
	}

	public boolean isRealTime() {
		return Boolean.valueOf(getField("RealTimeFlag"));
	}

	public void setRealTime(boolean realTime) {
		setField("RealTimeFlag", String.valueOf(realTime));
	}

	public boolean isIgnoreDateHints() {
		return Boolean.valueOf(getField("IgnoreUSKDatehints"));
	}

	public void setIgnoreDateHints(boolean ignoreDateHints) {
		setField("IgnoreUSKDatehints", String.valueOf(ignoreDateHints));
	}

}
