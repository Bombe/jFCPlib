package net.pterodactylus.fcp;

/**
 * The “LoadPlugin” message is used to load a plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LoadPlugin extends FcpMessage {

	public enum UrlType {

		OFFICIAL,
		FILE,
		FREENET,
		URL

	}

	public enum OfficialSource {

		FREENET,
		HTTPS

	}

	public LoadPlugin(String identifier) {
		super("LoadPlugin");
		setField("Identifier", identifier);
	}

	public void setPluginUrl(String pluginUrl) {
		setField("PluginURL", pluginUrl);
	}

	public void setUrlType(UrlType urlType) {
		setField("URLType", urlType.toString().toLowerCase());
	}

	public void setStore(boolean store) {
		setField("Store", String.valueOf(store));
	}

	public void setOfficialSource(OfficialSource officialSource) {
		setField("OfficialSource", officialSource.toString().toLowerCase());
	}

}
