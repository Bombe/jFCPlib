package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.PluginInfo;

/**
 * Returns information about a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface GetPluginInfoCommand {

	GetPluginInfoCommand detailed();
	Executable<Optional<PluginInfo>> plugin(String pluginName);

}
