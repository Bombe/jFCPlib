package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ListPeerNotesTest {

	@Test
	public void listPeerNotesWithNodeIdentifierSetsNodeIdentifierField() {
		ListPeerNotes listPeerNotes = new ListPeerNotes("node-identifier");
		assertThat(listPeerNotes.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

	@Test
	public void listPeerNotesWithIdentifierAndNodeIdentifierSetsIdentifierAndNodeIdentifierField() {
		ListPeerNotes listPeerNotes = new ListPeerNotes("identifier", "node-identifier");
		assertThat(listPeerNotes.getField("Identifier"), equalTo("identifier"));
		assertThat(listPeerNotes.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

}
