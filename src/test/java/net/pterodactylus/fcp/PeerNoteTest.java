package net.pterodactylus.fcp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link PeerNote}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PeerNoteTest {

	@Test
	public void peerNoteIsDecodedCorrectly() {
		FcpMessage receivedMessage = new FcpMessage("PeerNote");
		receivedMessage.setField("NoteText", "VWJlck5vZGUgKHVudGlsIEkgaGF2ZSByZWFsIHBlZXJzKQ==");
		PeerNote peerNote = new PeerNote(receivedMessage);
		assertThat(peerNote.getNoteText(), is("UberNode (until I have real peers)"));
	}

}
