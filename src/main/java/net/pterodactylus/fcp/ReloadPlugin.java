package net.pterodactylus.fcp;

/**
 * The “ReloadPlugin” message is used to reload a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ReloadPlugin extends FcpMessage {

	public ReloadPlugin(String identifier) {
		super("ReloadPlugin");
		setField("Identifier", identifier);
	}

	public void setPluginName(String pluginName) {
		setField("PluginName", pluginName);
	}

	public void setMaxWaitTime(int maxWaitTime) {
		setField("MaxWaitTime", String.valueOf(maxWaitTime));
	}

	public void setPurge(boolean purge) {
		setField("Purge", String.valueOf(purge));
	}

	public void setStore(boolean store) {
		setField("Store", String.valueOf(store));
	}

}
