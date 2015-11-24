package net.pterodactylus.fcp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link ModifyPeerNote}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ModifyPeerNoteTest {

	private static final String IDENTIFIER = "identifier";
	private static final String NODE_IDENTIFIER = "node_identifier";
	private static final String ENCODED_NOTE_TEXT = "VWJlck5vZGUgKHVudGlsIEkgaGF2ZSByZWFsIHBlZXJzKQ==";
	private static final String DECODED_NOTE_TEXT = "UberNode (until I have real peers)";

	@Test
	public void noteTextIsEncodedCorrectly() {
	    ModifyPeerNote modifyPeerNote = new ModifyPeerNote(IDENTIFIER, NODE_IDENTIFIER);
		modifyPeerNote.setNoteText(DECODED_NOTE_TEXT);
		assertThat(modifyPeerNote.getField("NoteText"), is(ENCODED_NOTE_TEXT));
	}


}
