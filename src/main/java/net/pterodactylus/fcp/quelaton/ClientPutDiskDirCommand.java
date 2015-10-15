package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.util.Optional;

import net.pterodactylus.fcp.Key;

/**
 * FCP command that inserts a directory from the filesystem local to the node into Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientPutDiskDirCommand {

	WithUri fromDirectory(File directory);

	interface WithUri {

		Executable<Optional<Key>> uri(String uri);

	}

}
