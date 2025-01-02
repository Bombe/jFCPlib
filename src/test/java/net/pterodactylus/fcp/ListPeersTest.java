package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingFlag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class ListPeersTest {

	@Test
	public void listPeersWithIdentifierSetsIdentifierField() {
		assertThat(listPeers.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void listPeersWithIdentifierDoesNotSetWithMetadataField() {
		assertThat(listPeers.getField("WithMetadata"), nullValue());
	}

	@Test
	public void settingWithMetadataToFalseOnListPeersResultsInWithMetadataFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(listPeers, ListPeers::setWithMetadata, "WithMetadata", false);
	}

	@Test
	public void settingWithMetadataToTrueOnListPeersResultsInWithMetadataFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(listPeers, ListPeers::setWithMetadata, "WithMetadata", true);
	}

	@Test
	public void listPeersWithIdentifierDoesNotSetWithVolatileField() {
		assertThat(listPeers.getField("WithVolatile"), nullValue());
	}

	@Test
	public void settingWithVolatileToFalseOnListPeersResultsInWithVolatileFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(listPeers, ListPeers::setWithVolatile, "WithVolatile", false);
	}

	@Test
	public void settingWithVolatileToTrueOnListPeersResultsInWithVolatileFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(listPeers, ListPeers::setWithVolatile, "WithVolatile", true);
	}

	@Test
	public void listPeersWithIdentifierAndFlagsSetsIdentifierAndFlagFields() {
		ListPeers listPeers = new ListPeers("identifier", false, true);
		assertThat(listPeers.getField("Identifier"), equalTo("identifier"));
		assertThat(listPeers.getField("WithMetadata"), equalTo("false"));
		assertThat(listPeers.getField("WithVolatile"), equalTo("true"));
	}

	private final ListPeers listPeers = new ListPeers("identifier");

}
