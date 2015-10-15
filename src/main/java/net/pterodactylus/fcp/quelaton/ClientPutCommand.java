package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

import net.pterodactylus.fcp.Key;
import net.pterodactylus.fcp.RequestProgress;

/**
 * FCP command that inserts data into Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientPutCommand {

	ClientPutCommand onProgress(Consumer<RequestProgress> requestProgressConsumer);
	ClientPutCommand onKeyGenerated(Consumer<String> keyGenerated);
	ClientPutCommand named(String targetFilename);
	WithUri redirectTo(String uri);
	WithUri from(File file);
	WithLength from(InputStream inputStream);

	interface WithLength {

		WithUri length(long length);

	}

	interface WithUri {

		Executable<Optional<Key>> uri(String uri);

	}

}
