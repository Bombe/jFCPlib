package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

import net.pterodactylus.fcp.Key;
import net.pterodactylus.fcp.RequestProgress;

/**
 * FCP command that inserts a directory from the filesystem local to the node into Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientPutDiskDirCommand {

	ClientPutDiskDirCommand onProgress(Consumer<RequestProgress> requestProgressConsumer);

	WithUri fromDirectory(File directory);

	interface WithUri {

		Executable<Optional<Key>> uri(String uri);

	}

}
