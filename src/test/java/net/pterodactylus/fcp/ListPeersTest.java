package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ListPeersTest {

	@Test
	public void listPeersWithIdentifierSetsIdentifierField() {
		ListPeers listPeers = new ListPeers("identifier");
		assertThat(listPeers.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void listPeersWithIdentifierAndFlagsSetsIdentifierAndFlagFields() {
		ListPeers listPeers = new ListPeers("identifier", false, true);
		assertThat(listPeers.getField("Identifier"), equalTo("identifier"));
		assertThat(listPeers.getField("WithMetadata"), equalTo("false"));
		assertThat(listPeers.getField("WithVolatile"), equalTo("true"));
	}

}
