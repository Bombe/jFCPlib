package net.pterodactylus.fcp.quelaton;

/**
 * Removes a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface RemovePluginCommand {

	RemovePluginCommand waitFor(int milliseconds);
	RemovePluginCommand purge();
	Executable<Boolean> plugin(String pluginClass);

}
