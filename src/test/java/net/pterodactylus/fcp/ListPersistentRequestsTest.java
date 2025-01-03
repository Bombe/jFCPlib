package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class ListPersistentRequestsTest {

	@Test
	public void listPersistentRequestsHasCorrectName() {
		ListPersistentRequests listPersistentRequests = new ListPersistentRequests();
		assertThat(listPersistentRequests.getName(), equalTo("ListPersistentRequests"));
	}

	@Test
	public void listPersistentRequestsWithIdentifierHasCorrectName() {
		ListPersistentRequests listPersistentRequests = new ListPersistentRequests("identifier");
		assertThat(listPersistentRequests.getName(), equalTo("ListPersistentRequests"));
	}

	@Test
	public void newListPersistentRequestsHasNoIdentifier() {
		ListPersistentRequests listPersistentRequests = new ListPersistentRequests();
		assertThat(listPersistentRequests.getField("Identifier"), nullValue());
	}

	@Test
	public void newListPersistentRequestsWithIdentifierHasIdentifier() {
		ListPersistentRequests listPersistentRequests = new ListPersistentRequests("identifier");
		assertThat(listPersistentRequests.getField("Identifier"), equalTo("identifier"));
	}

}
