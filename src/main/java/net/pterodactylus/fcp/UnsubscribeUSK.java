/*
 * jFCPlib - UnsubscribeUSK.java - Copyright © 2015–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.fcp;

/**
 * The “UnsubscribeUSK” message is used to cancel a {@link SubscribeUSK USK subscription}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class UnsubscribeUSK extends FcpMessage {

	public UnsubscribeUSK(String identifier) {
		super("UnsubscribeUSK");
		setField("Identifier", identifier);
	}

}
