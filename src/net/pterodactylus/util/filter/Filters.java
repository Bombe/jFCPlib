/*
 * jFCPlib - Filters.java -
 * Copyright © 2009 David Roden
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Defines various methods to filter {@link Collection}s.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class Filters {

	/**
	 * Returns a list that contains only the elements from the given list that
	 * match the given filter.
	 *
	 * @param <E>
	 *            The type of the list elements
	 * @param list
	 *            The list to filter
	 * @param listFilter
	 *            The list filter
	 * @return The filtered list
	 */
	public static <E> List<E> filteredList(List<E> list, Filter<E> listFilter) {
		List<E> filteredList = new ArrayList<E>();
		for (E element : list) {
			if (listFilter.filterObject(element)) {
				filteredList.add(element);
			}
		}
		return filteredList;
	}

	/**
	 * Returns a set that contains only the elements from the given set that
	 * match the given filter.
	 *
	 * @param <E>
	 *            The type of the set elements
	 * @param set
	 *            The set to filter
	 * @param setFilter
	 *            The set filter
	 * @return The filtered set
	 */
	public static <E> Set<E> filteredSet(Set<E> set, Filter<E> setFilter) {
		Set<E> filteredSet = new HashSet<E>();
		for (E element : set) {
			if (setFilter.filterObject(element)) {
				filteredSet.add(element);
			}
		}
		return filteredSet;
	}

	/**
	 * Returns a map that contains only the elements from the given map that
	 * match the given filter.
	 *
	 * @param <K>
	 *            The type of the map keys
	 * @param <V>
	 *            The type of the map values
	 * @param map
	 *            The map to filter
	 * @param mapFilter
	 *            The map filter
	 * @return The filtered map
	 */
	public static <K, V> Map<K, V> filteredMap(Map<K, V> map, Filter<Entry<K, V>> mapFilter) {
		Map<K, V> filteredMap = new HashMap<K, V>();
		for (Entry<K, V> element : map.entrySet()) {
			if (mapFilter.filterObject(element)) {
				filteredMap.put(element.getKey(), element.getValue());
			}
		}
		return filteredMap;
	}

	/**
	 * Returns a collection that contains only the elements from the given
	 * collection that match the given filter.
	 *
	 * @param <K>
	 *            The type of the collection values
	 * @param collection
	 *            The collection to filter
	 * @param collectionFilter
	 *            The collection filter
	 * @return The filtered collection
	 */
	public static <K> Collection<K> filteredCollection(Collection<K> collection, Filter<K> collectionFilter) {
		return filteredList(new ArrayList<K>(collection), collectionFilter);
	}

	/**
	 * Returns an iterator that contains only the elements from the given
	 * iterator that match the given filter.
	 *
	 * @param <E>
	 *            The type of the iterator elements
	 * @param iterator
	 *            The iterator to filter
	 * @param iteratorFilter
	 *            The iterator filter
	 * @return The filtered iterator
	 */
	public static <E> Iterator<E> filteredIterator(final Iterator<E> iterator, final Filter<E> iteratorFilter) {
		return new Iterator<E>() {

			private boolean gotNextElement = false;

			private E nextElement;

			private void getNextElement() {
				if (gotNextElement) {
					return;
				}
				while (iterator.hasNext()) {
					nextElement = iterator.next();
					if (iteratorFilter.filterObject(nextElement)) {
						gotNextElement = true;
						break;
					}
				}
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see java.util.Iterator#hasNext()
			 */
			public boolean hasNext() {
				getNextElement();
				return gotNextElement;
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see java.util.Iterator#next()
			 */
			public E next() {
				getNextElement();
				if (!gotNextElement) {
					throw new NoSuchElementException("no more elements in iteration");
				}
				gotNextElement = false;
				return nextElement;
			}

			/**
			 * {@inheritDoc}
			 *
			 * @see java.util.Iterator#remove()
			 */
			public void remove() {
				throw new UnsupportedOperationException("remove() not supported on this iteration");
			}

		};
	}

}
