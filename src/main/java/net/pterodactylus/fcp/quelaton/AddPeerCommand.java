package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.Peer;

/**
 * Command that adds a peer to the node.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface AddPeerCommand extends WithFile<Executable<Optional<Peer>>> {

}
