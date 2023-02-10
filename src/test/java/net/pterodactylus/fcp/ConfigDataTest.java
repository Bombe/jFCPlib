package net.pterodactylus.fcp;

import java.util.function.Function;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit test for {@link ConfigData}.
 */
public class ConfigDataTest {

	@Test
	public void configDataCanExposeCurrentOptionValue() {
		testExposedMethodReturningString("current", configData::getCurrent);
	}

	@Test
	public void configDataCanExposeShortDescription() {
		testExposedMethodReturningString("shortDescription", configData::getShortDescription);
	}

	@Test
	public void configDataCanExposeLongDescription() {
		testExposedMethodReturningString("longDescription", configData::getLongDescription);
	}

	@Test
	public void configDataCanExposeDataType() {
		testExposedMethodReturningString("dataType", configData::getDataType);
	}

	@Test
	public void configDataCanExposeDefault() {
		testExposedMethodReturningString("default", configData::getDefault);
	}

	@Test
	public void configDataCanExposeSortOrder() {
		receivedMessage.setField("sortOrder.testOption", "123");
		assertThat(configData.getSortOrder("testOption"), equalTo(123));
	}

	@Test
	public void sortOrderIsReturnedAsMinus1IfGivenANonNumericValue() {
		receivedMessage.setField("sortOrder.testOption", "not-a-number");
		assertThat(configData.getSortOrder("testOption"), equalTo(-1));
	}

	@Test
	public void expertFlagIsReturnedCorrectly() {
		receivedMessage.setField("expertFlag.testOption", "true");
		assertThat(configData.getExpertFlag("testOption"), equalTo(true));
	}

	@Test
	public void expertFlagIsFalseIfGivenANonBooleanValue() {
		receivedMessage.setField("expertFlag.testOption", "not-a-boolean");
		assertThat(configData.getExpertFlag("testOption"), equalTo(false));
	}

	@Test
	public void forceWriteFlagIsReturnedCorrectly() {
		receivedMessage.setField("forceWriteFlag.testOption", "true");
		assertThat(configData.getForceWriteFlag("testOption"), equalTo(true));
	}

	@Test
	public void forceWriteFlagIsFalseIfGivenANonBooleanValue() {
		receivedMessage.setField("forceWriteFlag.testOption", "not-a-boolean");
		assertThat(configData.getForceWriteFlag("testOption"), equalTo(false));
	}

	private void testExposedMethodReturningString(String prefixInMap, Function<String, String> getter) {
		String option = "testOption" + Math.random();
		String value = "testValue" + Math.random();
		receivedMessage.setField(prefixInMap  + "." + option, value);
		assertThat(getter.apply(option), equalTo(value));
	}

	private final FcpMessage receivedMessage = new FcpMessage("");
	private final ConfigData configData = new ConfigData(receivedMessage);

}
