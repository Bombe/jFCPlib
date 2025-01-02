package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingFlag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class GetRequestStatusTest {

	@Test
	public void getRequestStatusSetsIdentifierField() {
		assertThat(getRequestStatus.getField("Identifier"), equalTo("test-identifier"));
	}

	@Test
	public void newGetRequestStatusDoesNotIncludeGlobalField() {
		assertThat(getRequestStatus.getField("Global"), nullValue());
	}

	@Test
	public void getRequestStatusWithGlobalSetToTrueResultsInGlobalFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getRequestStatus, GetRequestStatus::setGlobal, m -> m.getField("Global"), true);
	}

	@Test
	public void getRequestStatusWithGlobalSetToFalseResultsInGlobalFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getRequestStatus, GetRequestStatus::setGlobal, m -> m.getField("Global"), false);
	}

	@Test
	public void newGetRequestStatusDoesNotIncludeOnlyDataField() {
		assertThat(getRequestStatus.getField("OnlyData"), nullValue());
	}

	@Test
	public void getRequestStatusWithOnlyDataSetToTrueResultsInOnlyDataFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getRequestStatus, GetRequestStatus::setOnlyData, m -> m.getField("OnlyData"), true);
	}

	@Test
	public void getRequestStatusWithOnlyDataSetToFalseResultsInOnlyDataFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getRequestStatus, GetRequestStatus::setOnlyData, m -> m.getField("OnlyData"), false);
	}

	private final GetRequestStatus getRequestStatus = new GetRequestStatus("test-identifier");

}
