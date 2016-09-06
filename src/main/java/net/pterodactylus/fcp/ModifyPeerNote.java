/*
 * jFCPlib - ModifyPeerNote.java - Copyright © 2008–2016 David Roden
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

import com.google.common.base.Charsets;

/**
 * The “ModifyPeerNote” command modifies a peer note.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ModifyPeerNote extends FcpMessage {

	private static final FreenetBase64 BASE64_ENCODER = new FreenetBase64();

	public ModifyPeerNote(String identifier, String nodeIdentifier) {
		super("ModifyPeerNote");
		setField("Identifier", identifier);
		setField("NodeIdentifier", nodeIdentifier);
	}

	@Deprecated
	public ModifyPeerNote(String nodeIdentifier, String noteText, int peerNoteType) {
		super("ModifyPeerNote");
		setField("NodeIdentifier", nodeIdentifier);
		setField("NoteText", noteText);
		setField("PeerNoteType", String.valueOf(peerNoteType));
	}

	public void setNoteText(String noteText) {
		setField("NoteText", BASE64_ENCODER.encode(noteText.getBytes(Charsets.UTF_8)));
	}

	public void setPeerNoteType(PeerNoteType peerNoteType) {
		setField("PeerNoteType", peerNoteType.toString());
	}

}
