/*
 * jFCPlib - SubscribedUSK.java - Copyright © 2008–2016 David Roden
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
 * A “SubscribedUSK” message is sent when a {@link SubscribeUSK} was succesfully processed.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class SubscribedUSK extends BaseMessage implements Identifiable {

	public SubscribedUSK(FcpMessage receivedMessage) {
		super(receivedMessage);
	}

	@Override
	public String getIdentifier() {
		return getField("Identifier");
	}

	public String getURI() {
		return getField("URI");
	}

	public boolean isDontPoll() {
		return Boolean.parseBoolean(getField("DontPoll"));
	}

}
