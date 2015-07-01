package net.pterodactylus.fcp.quelaton;

import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.Future;

import net.pterodactylus.fcp.Priority;

/**
 * Command that retrieves data from Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientGetCommand {

	ClientGetCommand identifier(String identifier);
	ClientGetCommand ignoreDataStore();
	ClientGetCommand dataStoreOnly();
	ClientGetCommand maxSize(long maxSize);
	ClientGetCommand priority(Priority priority);
	ClientGetCommand realTime();
	ClientGetCommand global();

	Future<Optional<Data>> uri(String uri);

	interface Data {

		String getMimeType();
		long size();
		InputStream getInputStream();

	}

}
