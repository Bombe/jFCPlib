package net.pterodactylus.fcp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link FreenetBase64}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetBase64Test {

	private final FreenetBase64 freenetBase64 = new FreenetBase64();

	@Test
	public void canEncodeString() {
		assertThat(freenetBase64.encode("".getBytes()), is(""));
		assertThat(freenetBase64.encode("f".getBytes()), is("Zg=="));
		assertThat(freenetBase64.encode("fo".getBytes()), is("Zm8="));
		assertThat(freenetBase64.encode("foo".getBytes()), is("Zm9v"));
		assertThat(freenetBase64.encode("foob".getBytes()), is("Zm9vYg=="));
		assertThat(freenetBase64.encode("fooba".getBytes()), is("Zm9vYmE="));
		assertThat(freenetBase64.encode("foobar".getBytes()), is("Zm9vYmFy"));
	}

	@Test
	public void canDecodeStrings() {
		assertThat(freenetBase64.decode(""), is("".getBytes()));
		assertThat(freenetBase64.decode("Zg=="), is("f".getBytes()));
		assertThat(freenetBase64.decode("Zm8="), is("fo".getBytes()));
		assertThat(freenetBase64.decode("Zm9v"), is("foo".getBytes()));
		assertThat(freenetBase64.decode("Zm9vYg=="), is("foob".getBytes()));
		assertThat(freenetBase64.decode("Zm9vYmE="), is("fooba".getBytes()));
		assertThat(freenetBase64.decode("Zm9vYmFy"), is("foobar".getBytes()));
	}

}
