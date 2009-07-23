/*
 * jFCPlib - WebOfTrustPlugin.java - Copyright © 2009 David Roden
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
	 * Returns the trust given to the identity with the given identifier by the
	 * given own identity.
	 *
	 * @param ownIdentity
	 *            The own identity that is used to calculate trust values
	 * @param identifier
	 *            The identifier of the identity whose trust to get
	 * @return The request identity trust
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public CalculatedTrust getIdentityTrust(OwnIdentity ownIdentity, String identifier) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetIdentity", "TreeOwner", ownIdentity.getIdentifier(), "Identity", identifier));
		if (!replies.get("Message").equals("Identity")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “Identity” message!");
		}
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
		return new CalculatedTrust(trust, score, rank);
	}

	/**
	 * Adds a new identity by its request URI.
	 *
	 * @param requestUri
	 *            The request URI of the identity to add
	 * @return The added identity
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Identity addIdentity(String requestUri) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "AddIdentity", "RequestURI", requestUri));
		if (!replies.get("Message").equals("IdentityAdded")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “IdentityAdded” message!");
		}
		String identifier = replies.get("ID");
		String nickname = replies.get("Nickname");
		return new Identity(identifier, nickname, requestUri);
	}

	/**
	 * Returns identities by the given score.
	 *
	 * @param ownIdentity
	 *            The own identity
	 * @param context
	 *            The context to get the identities for
	 * @param positive
	 *            {@code null} to return neutrally trusted identities, {@code
	 *            true} to return positively trusted identities, {@code false}
	 *            for negatively trusted identities
	 * @return The trusted identites
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Set<Identity> getIdentitesByScore(OwnIdentity ownIdentity, String context, Boolean positive) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetIdentitiesByScore", "TreeOwner", ownIdentity.getIdentifier(), "Context", context, "Selection", ((positive == null) ? "0" : (positive ? "+" : "-"))));
		if (!replies.get("Message").equals("Identities")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “Identities” message!");
		}
		Set<Identity> identities = new HashSet<Identity>();
		for (int identityIndex = 1; replies.containsKey("Identity" + identityIndex); identityIndex++) {
			String identifier = replies.get("Identity" + identityIndex);
			String nickname = replies.get("Nickname" + identityIndex);
			String requestUri = replies.get("RequestURI" + identityIndex);
			identities.add(new Identity(identifier, nickname, requestUri));
		}
		return identities;
	}

	/**
	 * Returns the identities that trust the given identity.
	 *
	 * @param identity
	 *            The identity to get the trusters for
	 * @param context
	 *            The context to get the trusters for
	 * @return The identities and their trust values
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Map<Identity, IdentityTrust> getTrusters(Identity identity, String context) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetTrusters", "Identity", identity.getIdentifier(), "Context", context));
		if (!replies.get("Message").equals("Identities")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “Identities” message!");
		}
		Map<Identity, IdentityTrust> identityTrusts = new HashMap<Identity, IdentityTrust>();
		for (int identityIndex = 1; replies.containsKey("Identity" + identityIndex); identityIndex++) {
			String identifier = replies.get("Identity" + identityIndex);
			String nickname = replies.get("Nickname" + identityIndex);
			String requestUri = replies.get("RequestURI" + identityIndex);
			byte trust = Byte.parseByte(replies.get("Value" + identityIndex));
			String comment = replies.get("Comment" + identityIndex);
			identityTrusts.put(new Identity(identifier, nickname, requestUri), new IdentityTrust(trust, comment));
		}
		return identityTrusts;
	}

	/**
	 * Returns the identities that given identity trusts.
	 *
	 * @param identity
	 *            The identity to get the trustees for
	 * @param context
	 *            The context to get the trustees for
	 * @return The identities and their trust values
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public Map<Identity, IdentityTrust> getTrustees(Identity identity, String context) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetTrustees", "Identity", identity.getIdentifier(), "Context", context));
		if (!replies.get("Message").equals("Identities")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “Identities” message!");
		}
		Map<Identity, IdentityTrust> identityTrusts = new HashMap<Identity, IdentityTrust>();
		for (int identityIndex = 1; replies.containsKey("Identity" + identityIndex); identityIndex++) {
			String identifier = replies.get("Identity" + identityIndex);
			String nickname = replies.get("Nickname" + identityIndex);
			String requestUri = replies.get("RequestURI" + identityIndex);
			byte trust = Byte.parseByte(replies.get("Value" + identityIndex));
			String comment = replies.get("Comment" + identityIndex);
			identityTrusts.put(new Identity(identifier, nickname, requestUri), new IdentityTrust(trust, comment));
		}
		return identityTrusts;
	}

	/**
	 * Sets the trust given to the given identify by the given own identity.
	 *
	 * @param ownIdentity
	 *            The identity that gives the trust
	 * @param identity
	 *            The identity that receives the trust
	 * @param trust
	 *            The trust value (ranging from {@code -100} to {@code 100}
	 * @param comment
	 *            The comment for setting the trust
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void setTrust(OwnIdentity ownIdentity, Identity identity, byte trust, String comment) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "SetTrust", "Truster", ownIdentity.getIdentifier(), "Trustee", identity.getIdentifier(), "Value", String.valueOf(trust), "Comment", comment));
		if (!replies.get("Message").equals("TrustSet")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “TrustSet” message!");
		}
	}

	/**
	 * Adds the given context to the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to add the context to
	 * @param context
	 *            The context to add
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void addContext(OwnIdentity ownIdentity, String context) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "AddContext", "Identity", ownIdentity.getIdentifier(), "Context", context));
		if (!replies.get("Message").equals("ContextAdded")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “ContextAdded” message!");
		}
	}

	/**
	 * Removes the given context from the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to remove the context from
	 * @param context
	 *            The context to remove
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void removeContext(OwnIdentity ownIdentity, String context) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "RemoveContext", "Identity", ownIdentity.getIdentifier(), "Context", context));
		if (!replies.get("Message").equals("ContextRemoved")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “ContextRemoved” message!");
		}
	}

	/**
	 * Sets the given property for the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to set a property for
	 * @param property
	 *            The name of the property to set
	 * @param value
	 *            The value of the property to set
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void setProperty(OwnIdentity ownIdentity, String property, String value) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "SetProperty", "Identity", ownIdentity.getIdentifier(), "Property", property, "Value", value));
		if (!replies.get("Message").equals("PropertyAdded")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “PropertyAdded” message!");
		}
	}

	/**
	 * Returns the value of the given property for the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to get a property for
	 * @param property
	 *            The name of the property to get
	 * @return The value of the property
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public String getProperty(OwnIdentity ownIdentity, String property) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "GetProperty", "Identity", ownIdentity.getIdentifier(), "Property", property));
		if (!replies.get("Message").equals("PropertyValue")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “PropertyValue” message!");
		}
		return replies.get("Property");
	}

	/**
	 * Removes the given property from the given identity.
	 *
	 * @param ownIdentity
	 *            The identity to remove a property from
	 * @param property
	 *            The name of the property to remove
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws FcpException
	 *             if an FCP error occurs
	 */
	public void removeProperty(OwnIdentity ownIdentity, String property) throws IOException, FcpException {
		Map<String, String> replies = fcpClient.sendPluginMessage("plugins.WoT.WoT", createParameters("Message", "RemoveProperty", "Identity", ownIdentity.getIdentifier(), "Property", property));
		if (!replies.get("Message").equals("PropertyRemoved")) {
			throw new FcpException("WebOfTrust Plugin did not reply with “PropertyRemoved” message!");
		}
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

		/**
		 * Creates a new identity.
		 *
		 * @param identifier
		 *            The identifies of the identity
		 * @param nickname
		 *            The nickname of the identity
		 * @param requestUri
		 *            The request URI of the identity
		 */
		public Identity(String identifier, String nickname, String requestUri) {
			this.identifier = identifier;
			this.nickname = nickname;
			this.requestUri = requestUri;
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
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if ((obj == null) || (obj.getClass() != this.getClass())) {
				return false;
			}
			Identity identity = (Identity) obj;
			return identifier.equals(identity.identifier);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return identifier.hashCode();
		}

	}

	/**
	 * Container for the trust given from one identity to another.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static class IdentityTrust {

		/** The trust given to the identity. */
		private final byte trust;

		/** The command for the trust value. */
		private final String comment;

		/**
		 * Creates a new identity trust container.
		 *
		 * @param trust
		 *            The trust given to the identity
		 * @param comment
		 *            The comment for the trust value
		 */
		public IdentityTrust(byte trust, String comment) {
			this.trust = trust;
			this.comment = comment;
		}

		/**
		 * Returns the trust value given to the identity.
		 *
		 * @return The trust value
		 */
		public byte getTrust() {
			return trust;
		}

		/**
		 * Returns the comment for the trust value.
		 *
		 * @return The comment for the trust value
		 */
		public String getComment() {
			return comment;
		}

	}

	/**
	 * Container that stores the trust that is calculated by taking all trustees
	 * and their trust lists into account.
	 *
	 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
	 */
	public static class CalculatedTrust {

		/** The calculated trust value. */
		private final Byte trust;

		/** The calculated score value. */
		private final Integer score;

		/** The calculated rank. */
		private final Integer rank;

		/**
		 * Creates a new calculated trust container.
		 *
		 * @param trust
		 *            The calculated trust value
		 * @param score
		 *            The calculated score value
		 * @param rank
		 *            The calculated rank of the
		 */
		public CalculatedTrust(Byte trust, Integer score, Integer rank) {
			this.trust = trust;
			this.score = score;
			this.rank = rank;
		}

		/**
		 * Returns the calculated trust value.
		 *
		 * @return The calculated trust value, or {@code null} if the trust
		 *         value is not known
		 */
		public Byte getTrust() {
			return trust;
		}

		/**
		 * Returns the calculated score value.
		 *
		 * @return The calculated score value, or {@code null} if the score
		 *         value is not known
		 */
		public Integer getScore() {
			return score;
		}

		/**
		 * Returns the calculated rank.
		 *
		 * @return The calculated rank, or {@code null} if the rank is not known
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
	public static class OwnIdentity extends Identity {

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
			super(identifier, nickname, requestUri);
			this.insertUri = insertUri;
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
