/*
 * jFCPlib - FcpConnectionTest.java -
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

package net.pterodactylus.fcp;

import java.io.IOException;
import java.io.InputStream;


/**
 * TODO
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class FcpConnectionTest extends FcpAdapter {

	public static void main(String[] commandLine) throws IllegalStateException, IOException {
		new FcpConnectionTest();
	}

	private FcpConnectionTest() throws IllegalStateException, IOException {
		FcpConnection fcpConnection = new FcpConnection("wing");
		fcpConnection.addFcpListener(this);
		fcpConnection.connect();
		ClientHello clientHello = new ClientHello("bug-test");
		fcpConnection.sendMessage(clientHello);
		ClientGet clientGet = new ClientGet("KSK@gpl.txt", "test");
		fcpConnection.sendMessage(clientGet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receivedAllData(FcpConnection fcpConnection, AllData allData) {
		System.out.println("AllData");
		InputStream payloadInputStream = allData.getPayloadInputStream();
		int r = 0;
		byte[] buffer = new byte[1024];
		try {
			while ((r = payloadInputStream.read(buffer)) != -1) {
				for (int i = 0; i < r; i++) {
					System.out.print((char) buffer[i]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
		fcpConnection.close();
	}

}
