/*
 * jFCPlib - LoadPlugin.java - Copyright © 2015–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
