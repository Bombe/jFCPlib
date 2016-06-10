package net.pterodactylus.fcp;

/**
 * Non-validating wrapper around a Freenet key.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Key {

	private final String key;

	public Key(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
