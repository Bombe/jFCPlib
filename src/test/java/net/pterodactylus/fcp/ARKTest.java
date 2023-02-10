package net.pterodactylus.fcp;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

/**
 * Unit test for {@link ARK}.
 */
public class ARKTest {

	@Test
	public void twoArgumentConstructorThrowsNPEIfFirstArgumentIsNull() {
		assertThrows(NullPointerException.class, () -> new ARK(null, ""));
	}

	@Test
	public void twoArgumentConstructorThrowsNPEIfSecondArgumentIsNull() {
		assertThrows(NullPointerException.class, () -> new ARK("", null));
	}

	@Test
	public void twoArgumentConstructorThrowsIAEIfSecondArgumentIsNotNumeric() {
		assertThrows(IllegalArgumentException.class, () -> new ARK("", "not-a-number"));
	}

	@Test
	public void twoArgumentConstructorRetainsPublicUriAndNumber() {
		ARK ark = new ARK("public-uri", "123");
		assertThat(ark.getPublicURI(), equalTo("public-uri"));
		assertThat(ark.getNumber(), equalTo(123));
	}

	@Test
	public void twoArgumentConstructorUsesNullAsPrivateUri() {
		ARK ark = new ARK("public-uri", "123");
		assertThat(ark.getPrivateURI(), nullValue());
	}

	@Test
	public void threeArgumentConstructorThrowsNPEIfFirstArgumentIsNull() {
		assertThrows(NullPointerException.class, () -> new ARK(null, "", ""));
	}

	@Test
	public void threeArgumentConstructorThrowsNPEIfThirdArgumentIsNull() {
		assertThrows(NullPointerException.class, () -> new ARK("", "", null));
	}

	@Test
	public void threeArgumentConstructorThrowsIAEIfThirdArgumentIsNotNumeric() {
		assertThrows(IllegalArgumentException.class, () -> new ARK("", "", "not-a-number"));
	}

	@Test
	public void threeArgumentConstructorRetainsPublicUriPrivateUriAndNumber() {
		ARK ark = new ARK("public-uri", "private-uri", "123");
		assertThat(ark.getPublicURI(), equalTo("public-uri"));
		assertThat(ark.getPrivateURI(), equalTo("private-uri"));
		assertThat(ark.getNumber(), equalTo(123));
	}

	@Test
	public void threeArgumentConstructorRetainsNullForPrivateUri() {
		ARK ark = new ARK("public-uri", null, "123");
		assertThat(ark.getPrivateURI(), nullValue());
	}

}
