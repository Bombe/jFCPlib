/*
 * jFCPlib - WebOfTrustPlugin.java -
 * Copyright © 2009 David Roden
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package net.pterodactylus.fcp.plugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.fcp.highlevel.FcpClient;
import net.pterodactylus.fcp.highlevel.FcpException;

/**
 * Simplifies handling of the web-of-trust plugin.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class WebOfTrustPlugin {

	/** The FCP client to use. */
	private final FcpClient fcpClient;

	/**
	 * Creates a new web-of-trust plugin wrapper around the given FCP client.
	 *
	 * @param fcpClient
	 *            The FCP client to use for communication with the web-of-trust
	 *            plugin
	 */
	public WebOfTrustPlugin(FcpClient fcpClient) {
		this.fcpClient = fcpClient;
	}

	/**
	 * Creates a new identity.
	 *
	 * @param nickname
	 *            The nickname of the new identity
	 * @param context
	 *            The context for the new identity
	 * @param publishTrustList
	 *            {@code true} if the new identity should publish its trust list
	 * @return The new identity
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public OwnIdentity createIdentity(String nickname, String context, boolean publishTrustList) throws IOException, FcpException {
		return createIdentity(nickname, context, publishTrustList, null, null);
	}

	/**
	 * Creates a new identity from the given request and insert URI.
	 *
	 * @param nickname
	 *            The nickname of the new identity
	 * @param context
	 *            The context for the new identity
	 * @param publishTrustList
	 *            {@code true} if the new identity should publish its trust list
	 * @param requestUri
	 *            The request URI of the identity
	 * @param insertUri
	 *            The insert URI of the identity
	 * @return The new identity
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public OwnIdentity createIdentity(String nickname, String context, boolean publishTrustList, String requestUri, String insertUri) throws IOException, FcpException {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("Message", "CreateIdentity");
		parameters.put("Nickname", nickname);
		parameters.put("Context", context);
		parameters.put("PublishTrustList", String.valueOf(publishTrustList));
		if ((requestUri != null) && (insertUri != null)) {
			parameters.put("RequestURI", requestUri);
			parameters.put("InsertURI", insertUri);
		}
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", parameters);
		if (!replies.get("Message").equals("IdentityCreated")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “IdentityCreated” message!");
		}
		String identifier = replies.get("ID");
		String newRequestUri = replies.get("RequestURI");
		String newInsertUri = replies.get("InsertURI");
		return new OwnIdentity(identifier, nickname, newRequestUri, newInsertUri);
	}

	/**
	 * Returns all own identities of the web-of-trust plugins. Almost all other
	 * commands require an {@link OwnIdentity} to return meaningful values.
	 *
	 * @return All own identities of the web-of-trust plugin
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Set<OwnIdentity> getOwnIdentites() throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetOwnIdentities"));
		if (!replies.get("Message").equals("OwnIdentities")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “OwnIdentities” message!");
		}
		Set<OwnIdentity> ownIdentities = new HashSet<OwnIdentity>();
		for (int identityIndex = 1; replies.containsKey("Identity" + identityIndex); identityIndex++) {
			String identity = replies.get("Identity" + identityIndex);
			String nickname = replies.get("Nickname" + identityIndex);
			String requestUri = replies.get("RequestURI" + identityIndex);
			String insertUri = replies.get("InsertURI" + identityIndex);
			ownIdentities.add(new OwnIdentity(identity, nickname, requestUri, insertUri));
		}
		return ownIdentities;
	}

	/**
	 * Returns the identity with the given identifier and the trust values
	 * depending on the given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity that is used to calculate trust values
	 * @param identifier
	 *            The identifier of the identity to get
	 * @return The request identity
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Identity getIdentity(OwnIdentity ownIdentity, String identifier) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetIdentity", "TreeOwner", ownIdentity.getIdentifier(), "Identity", identifier));
		if (!replies.get("Message").equals("Identity")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “Identity” message!");
		}
		String nickname = replies.get("Nickname");
		String requestUri = replies.get("RequestURI");
		Byte trust = null;
		try {
			trust = Byte.valueOf(replies.get("Trust"));
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		Integer score = null;
		try {
			score = Integer.valueOf(replies.get("Score"));
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		Integer rank = null;
		try {
			rank = Integer.valueOf(replies.get("Rank"));
		} catch (NumberFormatException nfe1) {
			/* ignore. */
		}
		return new Identity(identifier, nickname, requestUri, trust, score, rank);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a map from each pair of parameters in the given array.
	 *
	 * @param parameters
	 *            The array of parameters
	 * @return The map created from the array
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the given parameter array does not contains an even number
	 *             of elements
	 */
	private Map<String, String> createParameters(String... parameters) throws ArrayIndexOutOfBoundsException {
		Map<String, String> parameterMap = new HashMap<String, String>();
		for (int index = 0; index < parameters.length; index += 2) {
			parameterMap.put(parameters[index], parameters[index + 1]);
		}
		return parameterMap;
	}

	/**
	 * Wrapper around a web-of-trust identity.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static class Identity {

		/** The identity’s identifier. */
		private final String identifier;

		/** The identity’s nickname. */
		private final String nickname;

		/** The identity’s request URI. */
		private final String requestUri;

		/** The identity’s trust value. */
		private final Byte trust;

		/** The identity’s score value. */
		private final Integer score;

		/** The identity’s rank. */
		private final Integer rank;

		/**
		 * Creates a new identity.
		 *
		 * @param identifier
		 *            The identifies of the identity
		 * @param nickname
		 *            The nickname of the identity
		 * @param requestUri
		 *            The request URI of the identity
		 * @param trust
		 *            The trust value of the identity
		 * @param score
		 *            The score value of the identity
		 * @param rank
		 *            The rank of the identity
		 */
		public Identity(String identifier, String nickname, String requestUri, Byte trust, Integer score, Integer rank) {
			this.identifier = identifier;
			this.nickname = nickname;
			this.requestUri = requestUri;
			this.trust = trust;
			this.score = score;
			this.rank = rank;
		}

		/**
		 * Returns the identifier of this identity.
		 *
		 * @return This identity’s identifier
		 */
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * Returns the nickname of this identity.
		 *
		 * @return This identity’s nickname
		 */
		public String getNickname() {
			return nickname;
		}

		/**
		 * Returns the request URI of this identity.
		 *
		 * @return This identity’s request URI
		 */
		public String getRequestUri() {
			return requestUri;
		}

		/**
		 * Returns the trust value of this identity.
		 *
		 * @return This identity’s trust value, or {@code null} if this
		 *         identity’s trust value is not known
		 */
		public Byte getTrust() {
			return trust;
		}

		/**
		 * Returns the score value of this identity.
		 *
		 * @return This identity’s score value, or {@code null} if this
		 *         identity’s score value is not known
		 */
		public Integer getScore() {
			return score;
		}

		/**
		 * Returns the rank of this identity.
		 *
		 * @return This identity’s rank, or {@code null} if this identity’s rank
		 *         is not known
		 */
		public Integer getRank() {
			return rank;
		}

	}

	/**
	 * Wrapper around a web-of-trust own identity.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static class OwnIdentity {

		/** The identity’s identifier. */
		private final String identifier;

		/** The identity’s nickname. */
		private final String nickname;

		/** The identity’s request URI. */
		private final String requestUri;

		/** The identity’s insert URI. */
		private final String insertUri;

		/**
		 * Creates a new web-of-trust own identity.
		 *
		 * @param identifier
		 *            The identifier of the identity
		 * @param nickname
		 *            The nickname of the identity
		 * @param requestUri
		 *            The request URI of the identity
		 * @param insertUri
		 *            The insert URI of the identity
		 */
		public OwnIdentity(String identifier, String nickname, String requestUri, String insertUri) {
			this.identifier = identifier;
			this.nickname = nickname;
			this.requestUri = requestUri;
			this.insertUri = insertUri;
		}

		/**
		 * Returns the identifier of this identity.
		 *
		 * @return This identity’s identifier
		 */
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * Returns the nickname of this identity.
		 *
		 * @return This identity’s nickname
		 */
		public String getNickname() {
			return nickname;
		}

		/**
		 * Returns the request URI of this identity.
		 *
		 * @return This identity’s request URI
		 */
		public String getRequestUri() {
			return requestUri;
		}

		/**
		 * Returns the insert URI of this identity.
		 *
		 * @return This identity’s insert URI
		 */
		public String getInsertUri() {
			return insertUri;
		}

	}

}
