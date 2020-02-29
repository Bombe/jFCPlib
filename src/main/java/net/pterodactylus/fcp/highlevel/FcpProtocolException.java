/*
 * jFCPlib - FcpProtocolException.java - Copyright © 2020 David ‘Bombe’ Roden
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

package net.pterodactylus.fcp.highlevel;

import net.pterodactylus.fcp.ProtocolError;

/**
 * Specialized FCP exception that signals a {@link ProtocolError}.
 */
public class FcpProtocolException extends FcpException {

	private final int code;
	private final String codeDescription;
	private final String extraDescription;
	private final boolean fatal;

	public FcpProtocolException(int code, String codeDescription, String extraDescription, boolean fatal) {
		this.code = code;
		this.codeDescription = codeDescription;
		this.extraDescription = extraDescription;
		this.fatal = fatal;
	}

	public int getCode() {
		return code;
	}

	public String getCodeDescription() {
		return codeDescription;
	}

	public String getExtraDescription() {
		return extraDescription;
	}

	public boolean isFatal() {
		return fatal;
	}

	public static FcpProtocolException from(ProtocolError protocolError) {
		return new FcpProtocolException(protocolError.getCode(), protocolError.getCodeDescription(),
			protocolError.getExtraDescription(), protocolError.isFatal());
	}

}
