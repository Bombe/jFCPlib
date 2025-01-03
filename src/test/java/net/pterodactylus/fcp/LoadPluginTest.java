package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingFlag;
import static net.pterodactylus.fcp.test.MessageTests.verifyFieldValueAfterSettingProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

public class LoadPluginTest {

	@Test
	public void loadPluginSetsIdentifier() {
		assertThat(loadPlugin.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void settingPluginUrlWillSetField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setPluginUrl, "PluginURL", "plugin-url");
	}

	@Test
	public void settingUrlTypeToOfficialSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setUrlType, "URLType", LoadPlugin.UrlType.OFFICIAL, equalToIgnoringCase("official"));
	}

	@Test
	public void settingUrlTypeToFileSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setUrlType, "URLType", LoadPlugin.UrlType.FILE, equalToIgnoringCase("file"));
	}

	@Test
	public void settingUrlTypeToFreenetSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setUrlType, "URLType", LoadPlugin.UrlType.FREENET, equalToIgnoringCase("freenet"));
	}

	@Test
	public void settingUrlTypeToUrlSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setUrlType, "URLType", LoadPlugin.UrlType.URL, equalToIgnoringCase("url"));
	}

	@Test
	public void settingStoreToFalseSetsStoreFieldToFalse() {
		verifyFieldValueAfterSettingFlag(loadPlugin, LoadPlugin::setStore, "Store", false);
	}

	@Test
	public void settingStoreToTrueSetsStoreFieldToTrue() {
		verifyFieldValueAfterSettingFlag(loadPlugin, LoadPlugin::setStore, "Store", true);
	}

	@Test
	public void settingOfficialSourceToFreenetSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setOfficialSource, "OfficialSource", LoadPlugin.OfficialSource.FREENET, equalToIgnoringCase("freenet"));
	}

	@Test
	public void settingOfficialSourceToHttpsSetsCorrectField() {
		verifyFieldValueAfterSettingProperty(loadPlugin, LoadPlugin::setOfficialSource, "OfficialSource", LoadPlugin.OfficialSource.HTTPS, equalToIgnoringCase("https"));
	}

	private final LoadPlugin loadPlugin = new LoadPlugin("identifier");

}
