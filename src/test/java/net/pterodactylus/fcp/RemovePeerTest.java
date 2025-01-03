package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class RemovePeerTest {

	@Test
	public void removePeerMessageWithNodeIdentifierHasCorrectName() {
		RemovePeer removePeer = new RemovePeer("node-identifier");
		assertThat(removePeer.getName(), equalTo("RemovePeer"));
	}

	@Test
	public void removePeerMessageWithIdentifierAndNodeIdentifierHasCorrectName() {
		assertThat(removePeer.getName(), equalTo("RemovePeer"));
	}

	@Test
	public void removePeerWithNodeIdentifierSetsNodeIdentifierFieldCorrectly() {
		RemovePeer removePeer = new RemovePeer("node-identifier");
		assertThat(removePeer.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

	@Test
	public void removePeerWithNodeIdentifierDoesNotSetIdentifier() {
		RemovePeer removePeer = new RemovePeer("node-identifier");
		assertThat(removePeer.getField("Identifier"), nullValue());
	}

	@Test
	public void removePeerWithIdentifierAndNodeIdentifierSetsIdentifierFieldCorrectly() {
		assertThat(removePeer.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void removePeerWithIdentifierAndNodeIdentifierSetsNodeIdentifierFieldCorrectly() {
		assertThat(removePeer.getField("NodeIdentifier"), equalTo("node-identifier"));
	}

	private final RemovePeer removePeer = new RemovePeer("identifier", "node-identifier");

}
