/*
 * jFCPlib - GetPluginInfo.java - Copyright © 2008–2016 David Roden
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
 * The “GetPluginInfo” message requests information about a plugin from the
 * node, which will response with a {@link PluginInfo} message.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class GetPluginInfo extends FcpMessage {

	public GetPluginInfo(String identifier) {
		super("GetPluginInfo");
		setField("Identifier", identifier);
	}

	public GetPluginInfo(String pluginName, String identifier) {
		this(identifier);
		setField("PluginName", pluginName);
	}

	public void setPluginName(String pluginName) {
		setField("PluginName", pluginName);
	}

	/**
	 * Sets whether detailed information about the plugin is wanted.
	 *
	 * @param detailed
	 *            <code>true</code> to request detailed information about the
	 *            plugin, <code>false</code> otherwise
	 */
	public void setDetailed(boolean detailed) {
		setField("Detailed", String.valueOf(detailed));
	}

}
