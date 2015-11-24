package net.pterodactylus.fcp;

/**
 * The “RemovePlugin” message is used to remove a plugin.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class RemovePlugin extends FcpMessage {

	public RemovePlugin(String identifier) {
		super("RemovePlugin");
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

}
