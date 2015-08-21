package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.PluginInfo;

/**
 * Reloads a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ReloadPluginCommand {

	Executable<Optional<PluginInfo>> plugin(String pluginClassName);

}
