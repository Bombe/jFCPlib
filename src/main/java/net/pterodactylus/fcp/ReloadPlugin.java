/*
 * jFCPlib - ReloadPlugin.java - Copyright © 2015–2016 David Roden
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
