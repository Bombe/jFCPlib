package net.pterodactylus.fcp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.junit.Test;

/**
 * Unit test for {@link FcpMessage}.
 *
 * @author <a href="mailto:david.roden@bietr.de">David Roden</a>
 */
public class FcpMessageTest extends AbstractFcpMessageTest {

	private final FcpMessage fcpMessage = new FcpMessage("TestMessage");

	@Test
	public void fcpMessageWithPayloadIsTerminatedByData() throws Exception {
		fcpMessage.setPayloadInputStream(new ByteArrayInputStream("Test".getBytes()));
		List<String> lines = encodeMessage(fcpMessage);
		assertThat(lines, contains(
			"TestMessage",
			"Data",
			"Test"
		));
	}

}
