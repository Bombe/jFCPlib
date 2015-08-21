/*
 * jFCPlib - CalculatedTrust.java - Copyright © 2009–2014 David Roden
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

package net.pterodactylus.fcp.plugin;

/**
 * Container that stores the trust that is calculated by taking all trustees
 * and their trust lists into account.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class CalculatedTrust {

	/** The calculated trust value. */
	private final Byte trust;

	/** The calculated score value. */
	private final Integer score;

	/** The calculated rank. */
	private final Integer rank;

	/**
	 * Creates a new calculated trust container.
	 *
	 * @param trust
	 *            The calculated trust value
	 * @param score
	 *            The calculated score value
	 * @param rank
	 *            The calculated rank of the
	 */
	public CalculatedTrust(Byte trust, Integer score, Integer rank) {
		this.trust = trust;
		this.score = score;
		this.rank = rank;
	}

	/**
	 * Returns the calculated trust value.
	 *
	 * @return The calculated trust value, or {@code null} if the trust
	 *         value is not known
	 */
	public Byte getTrust() {
		return trust;
	}

	/**
	 * Returns the calculated score value.
	 *
	 * @return The calculated score value, or {@code null} if the score
	 *         value is not known
	 */
	public Integer getScore() {
		return score;
	}

	/**
	 * Returns the calculated rank.
	 *
	 * @return The calculated rank, or {@code null} if the rank is not known
	 */
	public Integer getRank() {
		return rank;
	}

}
