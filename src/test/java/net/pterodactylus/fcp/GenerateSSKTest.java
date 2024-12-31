package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class GenerateSSKTest {

	@Test
	public void generateSskMessageHasCorrectName() {
		GenerateSSK generateSSK = new GenerateSSK();
		assertThat(generateSSK.getName(), equalTo("GenerateSSK"));
	}

	@Test
	public void generateSskWithoutIdentifierGeneratesAIdentifier() {
		GenerateSSK generateSSK = new GenerateSSK();
		assertThat(generateSSK.getField("Identifier"), notNullValue());
	}

	@Test
	public void generateSskWithIdentifierSetsIdentifier() {
		GenerateSSK generateSSK = new GenerateSSK("test-identifier");
		assertThat(generateSSK.getField("Identifier"), equalTo("test-identifier"));
	}

}
