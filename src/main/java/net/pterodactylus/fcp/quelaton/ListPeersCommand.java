package net.pterodactylus.fcp.quelaton;

import java.util.Collection;
import java.util.concurrent.Future;

import net.pterodactylus.fcp.Peer;

/**
 * Retrieves the list of all peers from the FCP server.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ListPeersCommand extends Executable<Collection<Peer>> {

	ListPeersCommand includeMetadata();
	ListPeersCommand includeVolatile();

}
