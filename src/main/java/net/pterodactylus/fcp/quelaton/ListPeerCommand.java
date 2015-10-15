package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.Peer;

/**
 * Lists a single peer by its name (darknet only), identity, or host name/IP address and port number.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ListPeerCommand {

	Executable<Optional<Peer>> byName(String name);
	Executable<Optional<Peer>> byIdentity(String identity);
	Executable<Optional<Peer>> byHostAndPort(String host, int port);

}
