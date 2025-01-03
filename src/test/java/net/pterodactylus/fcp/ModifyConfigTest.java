package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThrows;

public class ModifyConfigTest {

	@Test
	public void modifyConfigWithoutIdentifierHasCorrectName() {
		ModifyConfig modifyConfig = new ModifyConfig();
		assertThat(modifyConfig.getName(), equalTo("ModifyConfig"));
	}

	@Test
	public void modifyConfigWithIdentifierHasCorrectName() {
		assertThat(modifyConfig.getName(), equalTo("ModifyConfig"));
	}

	@Test
	public void modifyConfigWithIdentifierSetsIdentifierField() {
		assertThat(modifyConfig.getField("Identifier"), equalTo("identifier"));
	}

	@Test
	public void optionCanBeSet() {
		modifyConfig.setOption("node.option", "value");
		assertThat(modifyConfig.getField("node.option"), equalTo("value"));
	}

	@Test
	public void invalidOptionNameResultsInException() {
		assertThrows(IllegalArgumentException.class, () -> modifyConfig.setOption("option", "value"));
	}

	private final ModifyConfig modifyConfig = new ModifyConfig("identifier");

}
