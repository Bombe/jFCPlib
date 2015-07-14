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
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 18) & 0b111111]);
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 12) & 0b111111]);
				result.append(FREENET_BASE64_ALPHABET[(currentValue >> 6) & 0b111111]);
				result.append(FREENET_BASE64_ALPHABET[currentValue & 0b111111]);
			}
			index++;
		}
		if (index % 3 == 1) {
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 2) & 0b111111]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue << 4) & 0b111111]);
			result.append("==");
		} else if (index % 3 == 2) {
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 10) & 0b111111]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue >> 4) & 0b111111]);
			result.append(FREENET_BASE64_ALPHABET[(currentValue << 2) & 0b111111]);
			result.append("=");
		}
		return result.toString();
	}

	public byte[] decode(String data) {
		try (ByteArrayOutputStream dataOutput = new ByteArrayOutputStream(data.length() * 3 / 4)) {
			int currentValue = 0;
			int index = 0;
			for (char c : data.toCharArray()) {
				if (c == '=') {
					break;
				}
				currentValue = (currentValue << 6) | REVERSE_FREENET_BASE64_ALPHABET[c];
				if (index % 4 == 3) {
					dataOutput.write((currentValue >> 16) & 0b11111111);
					dataOutput.write((currentValue >> 8) & 0b11111111);
					dataOutput.write(currentValue & 0b11111111);
				}
				index++;
			}
			if (index % 4 == 2) {
				dataOutput.write((currentValue >> 4) & 0b11111111);
			} else if (index % 4 == 3) {
				dataOutput.write((currentValue >> 10) & 0b11111111);
				dataOutput.write((currentValue >> 2) & 0b11111111);
			}
			return dataOutput.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
