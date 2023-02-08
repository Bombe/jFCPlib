package net.pterodactylus.fcp.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import net.pterodactylus.fcp.FcpMessage;

import static java.util.Arrays.asList;

public class MessageUtils {

	public static List<String> encodeMessage(FcpMessage fcpMessage) {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			fcpMessage.write(outputStream);
			return asList(outputStream.toString().split("\r?\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
