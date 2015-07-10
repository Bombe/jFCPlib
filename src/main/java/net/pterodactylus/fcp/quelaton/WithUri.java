package net.pterodactylus.fcp.quelaton;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * The terminal operation of an FCP command, requiring a Freenet URI.
 *
 * @param <R>
 * 	The type of the command result
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface WithUri<R> {

	ListenableFuture<R> uri(String uri);

}
