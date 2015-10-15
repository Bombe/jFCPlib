package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.net.URL;
import java.util.Optional;

import net.pterodactylus.fcp.NodeRef;
import net.pterodactylus.fcp.Peer;

/**
 * Command that adds a peer to the node.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface AddPeerCommand {

	Executable<Optional<Peer>> fromFile(File file);
	Executable<Optional<Peer>> fromURL(URL url);
	Executable<Optional<Peer>> fromNodeRef(NodeRef nodeRef);

}
