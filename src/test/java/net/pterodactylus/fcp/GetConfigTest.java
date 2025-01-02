package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingFlag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class GetConfigTest {

	@Test
	public void getConfigWithoutIdentifierCreatesMessageWithCorrectName() {
		assertThat(getConfig.getName(), equalTo("GetConfig"));
	}

	@Test
	public void getConfigWithoutIdentifierCreatesMessageWithoutIdentifier() {
		assertThat(getConfig.getField("Identifier"), nullValue());
	}

	@Test
	public void getConfigWithIdentifierCreatesMessageWithCorrectName() {
		GetConfig getConfig = new GetConfig("test-identifier");
		assertThat(getConfig.getName(), equalTo("GetConfig"));
	}

	@Test
	public void getConfigWithIdentifierCreatesMessageWithIdentifier() {
		GetConfig getConfig = new GetConfig("test-identifier");
		assertThat(getConfig.getField("Identifier"), equalTo("test-identifier"));
	}

	@Test
	public void newGetConfigDoesNotHaveWithCurrentField() {
		assertThat(getConfig.getField("WithCurrent"), nullValue());
	}

	@Test
	public void settingWithCurrentToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithCurrent, "WithCurrent", true);
	}

	@Test
	public void settingWithCurrentToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithCurrent, "WithCurrent", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithDefaultsField() {
		assertThat(getConfig.getField("WithDefaults"), nullValue());
	}

	@Test
	public void settingWithDefaultsToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithDefaults, "WithDefaults", true);
	}

	@Test
	public void settingWithDefaultsToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithDefaults, "WithDefaults", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithSortOrderField() {
		assertThat(getConfig.getField("WithSortOrder"), nullValue());
	}

	@Test
	public void settingWithSortOrderToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithSortOrder, "WithSortOrder", true);
	}

	@Test
	public void settingWithSortOrderToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithSortOrder, "WithSortOrder", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithExpertFlagField() {
		assertThat(getConfig.getField("WithExpertFlag"), nullValue());
	}

	@Test
	public void settingWithExpertFlagToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithExpertFlag, "WithExpertFlag", true);
	}

	@Test
	public void settingWithExpertFlagToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithExpertFlag, "WithExpertFlag", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithForceWriteFlagField() {
		assertThat(getConfig.getField("WithForceWriteFlag"), nullValue());
	}

	@Test
	public void settingWithForceWriteFlagToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithForceWriteFlag, "WithForceWriteFlag", true);
	}

	@Test
	public void settingWithForceWriteFlagToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithForceWriteFlag, "WithForceWriteFlag", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithShortDescriptionField() {
		assertThat(getConfig.getField("WithShortDescription"), nullValue());
	}

	@Test
	public void settingWithShortDescriptionToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithShortDescription, "WithShortDescription", true);
	}

	@Test
	public void settingWithShortDescriptionToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithShortDescription, "WithShortDescription", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithLongDescriptionField() {
		assertThat(getConfig.getField("WithLongDescription"), nullValue());
	}

	@Test
	public void settingWithLongDescriptionToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithLongDescription, "WithLongDescription", true);
	}

	@Test
	public void settingWithLongDescriptionToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithLongDescription, "WithLongDescription", false);
	}

	@Test
	public void newGetConfigDoesNotHaveWithDataTypesField() {
		assertThat(getConfig.getField("WithDataTypes"), nullValue());
	}

	@Test
	public void settingWithDataTypesToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithDataTypes, "WithDataTypes", true);
	}

	@Test
	public void settingWithDataTypesToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getConfig, GetConfig::setWithDataTypes, "WithDataTypes", false);
	}

	private final GetConfig getConfig = new GetConfig();

}
