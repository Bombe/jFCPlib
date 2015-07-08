package net.pterodactylus.fcp.quelaton;

import java.io.InputStream;
import java.util.Optional;

import net.pterodactylus.fcp.Priority;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Command that retrieves data from Freenet.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface ClientGetCommand {

	ClientGetCommand ignoreDataStore();
	ClientGetCommand dataStoreOnly();
	ClientGetCommand maxSize(long maxSize);
	ClientGetCommand priority(Priority priority);
	ClientGetCommand realTime();
	ClientGetCommand global();

	ListenableFuture<Optional<Data>> uri(String uri);

	interface Data {

		String getMimeType();
		long size();
		InputStream getInputStream();

	}

}
