package net.pterodactylus.fcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Freenet-specific Base64 implementation.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class FreenetBase64 {

	private static final char[] FREENET_BASE64_ALPHABET =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~-".toCharArray();
	private static final int[] REVERSE_FREENET_BASE64_ALPHABET = reverse(FREENET_BASE64_ALPHABET);

	private static int[] reverse(char[] alphabet) {
		String alphabetString = new String(alphabet);
		int[] reversedAlphabet = new int[128];
		for (int i = 0; i < 128; i++) {
			if (alphabetString.indexOf(i) > -1) {
				reversedAlphabet[i] = alphabetString.indexOf(i);
			} else {
				reversedAlphabet[i] = -1;
			}
		}
		return reversedAlphabet;
	}

	public String encode(byte[] data) {
		StringBuilder result = new StringBuilder();
		int currentValue = 0;
		int index = 0;
		while (index < data.length) {
			currentValue = (currentValue << 8) | data[index];
			if (index % 3 == 2) {
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 18) & 0x3f]);
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 12) & 0x3f]);
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 6) & 0x3f]);
				result.append(FREENET_BASE64_ALPHABET[currentValue & 0x3f]);
			}
			index++;
		}
		if (index % 3 == 1) {
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 2) & 0x3f]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue << 4) & 0x3f]);
			result.append("==");
		} else if (index % 3 == 2) {
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 10) & 0x3f]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 4) & 0x3f]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue << 2) & 0x3f]);
			result.append("=");
		}
		return result.toString();
	}

	public byte[] decode(String data) {
		ByteArrayOutputStream dataOutput = new ByteArrayOutputStream(data.length() * 3 / 4);
		try {
			int currentValue = 0;
			int index = 0;
			for (char c : data.toCharArray()) {
				if (c == '=') {
					break;
				}
				currentValue = (currentValue << 6) | REVERSE_FREENET_BASE64_ALPHABET[c];
				if (index % 4 == 3) {
					dataOutput.write((currentValue >> 16) & 0xff);
					dataOutput.write((currentValue >> 8) & 0xff);
					dataOutput.write(currentValue & 0xff);
				}
				index++;
			}
			if (index % 4 == 2) {
				dataOutput.write((currentValue >> 4) & 0xff);
			} else if (index % 4 == 3) {
				dataOutput.write((currentValue >> 10) & 0xff);
				dataOutput.write((currentValue >> 2) & 0xff);
			}
			return dataOutput.toByteArray();
		} finally {
			try {
				dataOutput.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
