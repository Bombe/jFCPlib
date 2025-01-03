package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ModifyPersistentRequestTest {

	@Test
	public void modifyPersistentRequestWithIdentifierAndGlobalHasCorrectName() {
		assertThat(modifyPersistentRequest.getName(), equalTo("ModifyPersistentRequest"));
	}

	@Test
	public void modifyPersistentRequestSetsIdentifierField() {
		assertThat(modifyPersistentRequest.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void modifyPersistentRequestSetsGlobalField() {
		assertThat(modifyPersistentRequest.getField("Global"), equalTo("true"));
	}

	@Test
	public void settingClientTokenResultsInFieldBeingSet() {
		verifyFieldValueAfterSettingProperty(modifyPersistentRequest, ModifyPersistentRequest::setClientToken, "ClientToken", "client-token");
	}

	@Test
	public void settingPriorityClassResultsInFieldBeingSet() {
		verifyFieldValueAfterSettingProperty(modifyPersistentRequest, ModifyPersistentRequest::setPriority, "PriorityClass", Priority.update, equalTo("3"));
	}

	private final ModifyPersistentRequest modifyPersistentRequest = new ModifyPersistentRequest("identifier", true);

}
