/*
 * jFCPlib - ModifyPeerNote.java - Copyright © 2008 David Roden
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
 * The “ModifyPeerNote” command modifies a peer note.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class ModifyPeerNote extends FcpMessage {

	public ModifyPeerNote(String identifier, String nodeIdentifier) {
		super("ModifyPeerNote");
		setField("Identifier", identifier);
		setField("NodeIdentifier", nodeIdentifier);
	}

	@Deprecated
	public ModifyPeerNote(String nodeIdentifier, String noteText, int peerNoteType) {
		super("ModifyPeer");
		setField("NodeIdentifier", nodeIdentifier);
		setField("NoteText", noteText);
		setField("PeerNoteType", String.valueOf(peerNoteType));
	}

	public void setNoteText(String noteText) {
		setField("NoteText", noteText);
	}

	public void setPeerNoteType(PeerNoteType peerNoteType) {
		setField("PeerNoteType", peerNoteType.toString());
	}

}
