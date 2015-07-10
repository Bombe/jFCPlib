package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import net.pterodactylus.fcp.Key;

/**
 * FCP command that inserts data into Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientPutCommand {

	ClientPutCommand named(String targetFilename);
	WithUri<Executable<Optional<Key>>> redirectTo(String uri);
	WithUri<Executable<Optional<Key>>> from(File file);
	WithLength<WithUri<Executable<Optional<Key>>>> from(InputStream inputStream);

}
