package net.pterodactylus.fcp.quelaton;

import net.pterodactylus.fcp.ConfigData;

/**
 * Command to modify the configuration of a node.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ModifyConfigCommand extends Executable<ConfigData> {

	WithValue set(String key);

	interface WithValue {

		ModifyConfigCommand to(String value);

	}

}
