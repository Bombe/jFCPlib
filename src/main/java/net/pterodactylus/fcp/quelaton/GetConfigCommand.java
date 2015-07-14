package net.pterodactylus.fcp.quelaton;

import net.pterodactylus.fcp.ConfigData;

/**
 * Command that retrieves the node’s configuration.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface GetConfigCommand extends Executable<ConfigData> {

	GetConfigCommand withCurrent();
	GetConfigCommand withDefaults();
	GetConfigCommand withSortOrder();
	GetConfigCommand withExpertFlag();
	GetConfigCommand withForceWriteFlag();
	GetConfigCommand withShortDescription();
	GetConfigCommand withLongDescription();
	GetConfigCommand withDataTypes();

}
