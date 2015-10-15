package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.PeerNote;

/**
 * List the peer notes of a single peer by its name (darknet only), node identifier, or host name/IP address and port
 * number.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ListPeerNotesCommand {

	Executable<Optional<PeerNote>> byName(String name);
	Executable<Optional<PeerNote>> byIdentity(String identity);
	Executable<Optional<PeerNote>> byHostAndPort(String host, int port);

}
