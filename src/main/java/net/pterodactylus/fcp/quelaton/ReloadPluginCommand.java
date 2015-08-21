package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.PluginInfo;

/**
 * Reloads a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ReloadPluginCommand {

	ReloadPluginCommand waitFor(int milliseconds);
	ReloadPluginCommand purge();
	ReloadPluginCommand addToConfig();
	Executable<Optional<PluginInfo>> plugin(String pluginClassName);

}
