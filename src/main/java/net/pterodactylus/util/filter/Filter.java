/*
 * jFCPlib - Filter.java - Copyright © 2009 David Roden
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
package net.pterodactylus.util.filter;

/**
 * Interface for a filter that determines whether a certain action can be
 * performed on an object based on its properties.
 *
 * @param <T>
 *            The type of the filtered object
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public interface Filter<T> {

	/**
	 * Runs the given object through this filter and return whether the object
	 * matches this filter or not.
	 *
	 * @param object
	 *            The object to analyse
	 * @return <code>true</code> if the object matched this filter,
	 *         <code>false</code> otherwise
	 */
	public boolean filterObject(T object);

}
