package net.pterodactylus.fcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Base test for all tests that verify a message’s appearance.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AbstractFcpMessageTest {

	protected List<String> encodeMessage(FcpMessage fcpMessage) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		fcpMessage.write(outputStream);
		return Arrays.asList(outputStream.toString().split("\r?\n"));
	}

}
