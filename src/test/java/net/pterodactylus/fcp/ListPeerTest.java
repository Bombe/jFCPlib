package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class ListPeerTest {

	@Test
	public void listPeerContainsIdentifierAndNodeIdentifier() {
		ListPeer listPeer = new ListPeer("identifier", "node-identifier");
		assertThat(listPeer.getField("Identifier"), equalTo("identifier"));
		assertThat(listPeer.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

	@Test
	public void listPeerContainsNodeIdentifierButNoIdentifier() {
		ListPeer listPeer = new ListPeer("node-identifier");
		assertThat(listPeer.getField("Identifier"), nullValue());
		assertThat(listPeer.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

}
