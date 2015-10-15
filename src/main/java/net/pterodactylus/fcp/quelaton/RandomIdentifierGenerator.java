package net.pterodactylus.fcp.quelaton;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Generates random identifiers.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RandomIdentifierGenerator {

	private final Random random = new Random();

	public String generate() {
		StringBuilder stringBuilder = new StringBuilder(32);
		IntStream.range(0, 32).forEach((i) -> stringBuilder.append(generateRandomLetter()));
		return stringBuilder.toString();
	}

	private char generateRandomLetter() {
		return (char) (65 + (random.nextInt(26)) + (random.nextBoolean() ? 32 : 0));
	}

}
