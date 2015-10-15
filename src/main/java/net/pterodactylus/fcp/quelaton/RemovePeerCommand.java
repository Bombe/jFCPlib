package net.pterodactylus.fcp.quelaton;

/**
 * Command that removes a peer from the node.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface RemovePeerCommand {

	Executable<Boolean> byName(String name);
	Executable<Boolean> byIdentity(String nodeIdentity);
	Executable<Boolean> byHostAndPort(String host, int port);

}
