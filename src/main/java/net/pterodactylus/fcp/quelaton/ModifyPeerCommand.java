package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.Peer;

/**
 * Command that modifies certain settings of a peer.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ModifyPeerCommand {

	ModifyPeerCommand enable();
	Executable<Optional<Peer>> byName(String name);

}
