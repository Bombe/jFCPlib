/*
 * jFCPlib - PeerNoteTest.java - Copyright © 2020 David Roden
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class SubscribedUSKUpdateMessageTest {

	@Test
	public void testWithoutNewKnownGood() {
		FcpMessage receivedMessage = new FcpMessage("SubscribedUSKUpdate");
		String URIValue = "CHK@anURI";
		receivedMessage.setField("URI", URIValue);

		SubscribedUSKUpdate subscribedUSKUpdate = new SubscribedUSKUpdate(receivedMessage);

		assertThat(subscribedUSKUpdate.getURI(), is(URIValue));
		assertThat(subscribedUSKUpdate.isNewKnownGood(), is(false));
	}

	@Test
	public void testWithNewKnownGoodFalse() {
		checkWithNewKnownGood("false", false);
	}

	@Test
	public void testWithNewKnownGoodTrue() {
		checkWithNewKnownGood("true", true);
	}

	private void checkWithNewKnownGood(String text, boolean value) {
		FcpMessage receivedMessage = new FcpMessage("SubscribedUSKUpdate");
		String URIValue = "CHK@anURI";
		receivedMessage.setField("URI", URIValue);
		receivedMessage.setField("NewKnownGood", text);

		SubscribedUSKUpdate subscribedUSKUpdate = new SubscribedUSKUpdate(receivedMessage);

		assertThat(subscribedUSKUpdate.getURI(), is(URIValue));
		assertThat(subscribedUSKUpdate.isNewKnownGood(), is(value));
	}

}
