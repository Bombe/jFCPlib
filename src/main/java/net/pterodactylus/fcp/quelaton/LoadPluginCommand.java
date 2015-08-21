package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.PluginInfo;

/**
 * Loads a plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface LoadPluginCommand {

	LoadPluginCommand addToConfig();
	Executable<Optional<PluginInfo>> officialFromFreenet(String pluginIdentifier);
	Executable<Optional<PluginInfo>> officialFromHttps(String pluginIdentifier);
	Executable<Optional<PluginInfo>> fromFile(String filename);
	Executable<Optional<PluginInfo>> fromUrl(String url);

}
