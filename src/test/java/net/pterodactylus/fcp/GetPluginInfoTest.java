package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingFlag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class GetPluginInfoTest {

	@Test
	public void getPluginInfoWithoutPluginNameHasCorrectName() {
		GetPluginInfo getPluginInfo = new GetPluginInfo("test-identifier");
		assertThat(getPluginInfo.getName(), equalTo("GetPluginInfo"));
	}

	@Test
	public void getPluginInfoWithPluginNameHasCorrectName() {
		GetPluginInfo getPluginInfo = new GetPluginInfo("test.Plugin", "test-identifier");
		assertThat(getPluginInfo.getName(), equalTo("GetPluginInfo"));
	}

	@Test
	public void newGetPluginInfoHasNoDetailedField() {
		GetPluginInfo getPluginInfo = new GetPluginInfo("test.Plugin", "test-identifier");
		assertThat(getPluginInfo.getField("Detailed"), nullValue());
	}

	@Test
	public void settingPluginNameResultsInFieldBeingSet() {
		getPluginInfo.setPluginName("test.Plugin2");
		assertThat(getPluginInfo.getField("PluginName"), equalTo("test.Plugin2"));
	}

	@Test
	public void settingDetailedToTrueResultsInFieldBeingSetToTrue() {
		verifyFieldValueAfterSettingFlag(getPluginInfo, GetPluginInfo::setDetailed, "Detailed", true);
	}

	@Test
	public void settingDetailedToFalseResultsInFieldBeingSetToFalse() {
		verifyFieldValueAfterSettingFlag(getPluginInfo, GetPluginInfo::setDetailed, "Detailed", false);
	}

	private final GetPluginInfo getPluginInfo = new GetPluginInfo("test.Plugin", "test-identifier");

}
