package net.pterodactylus.fcp;

import net.pterodactylus.fcp.test.MessageTests;
import org.junit.Test;

import static net.pterodactylus.fcp.test.MessageTests.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ReloadPluginTest {

	@Test
	public void reloadPluginMessageHasCorrectName() {
		assertThat(reloadPlugin.getName(), equalTo("ReloadPlugin"));
	}

	@Test
	public void reloadPluginSetsIdentifier() {
		assertThat(reloadPlugin.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void settingThePluginNameSetsTheFieldCorrectly() {
		verifyFieldValueAfterSettingProperty(reloadPlugin, ReloadPlugin::setPluginName, "PluginName", "test.Plugin");
	}

	@Test
	public void settingTheMaxWaitTimeSetsTheFieldCorrectly() {
		verifyFieldValueAfterSettingProperty(reloadPlugin, ReloadPlugin::setMaxWaitTime, "MaxWaitTime", 17, equalTo("17"));
	}

	@Test
	public void settingThePurgeFlagToFalseWillSetTheFieldToFalse() {
		verifyFieldValueAfterSettingFlag(reloadPlugin, ReloadPlugin::setPurge, "Purge", false);
	}

	@Test
	public void settingThePurgeFlagToTrueWillSetTheFieldToTrue() {
		verifyFieldValueAfterSettingFlag(reloadPlugin, ReloadPlugin::setPurge, "Purge", true);
	}

	@Test
	public void settingTheStoreFlagToFalseWillSetTheFieldToFalse() {
		verifyFieldValueAfterSettingFlag(reloadPlugin, ReloadPlugin::setStore, "Store", false);
	}

	@Test
	public void settingTheStoreFlagToTrueWillSetTheFieldToTrue() {
		verifyFieldValueAfterSettingFlag(reloadPlugin, ReloadPlugin::setStore, "Store", true);
	}

	private final ReloadPlugin reloadPlugin = new ReloadPlugin("identifier");

}
