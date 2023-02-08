package net.pterodactylus.fcp;

import net.pterodactylus.fcp.test.Matchers;

import org.junit.Test;

import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Unit test for {@link ClientHello}.
 */
public class ClientHelloTest {

	@Test
	public void canCreateNewClientHello() {
		ClientHello clientHello = new ClientHello("test-client");
		assertThat(clientHello, isMessage("ClientHello", hasItem("Name=test-client")));
	}

	@Test
	public void clientHelloWithoutExpectedVersionHasDefaultExpectedVersion() {
		ClientHello clientHello = new ClientHello("test-client");
		assertThat(clientHello, isMessage("ClientHello", hasItem("ExpectedVersion=2.0")));
	}

	@Test
	public void canCreateHelloWorldWithClientNameAndExpectedVersion() {
		ClientHello clientHello = new ClientHello("test-client", "1.2.3");
		assertThat(clientHello, isMessage("ClientHello", "Name=test-client", "ExpectedVersion=1.2.3"));
	}

}
