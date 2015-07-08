package net.pterodactylus.fcp.quelaton;

import net.pterodactylus.fcp.Key;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * The terminal operation of an FCP command, requiring a {@link Key}.
 *
 * @param <R>
 * 	The type of the command result
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface Keyed<R> {

	ListenableFuture<R> key(Key key);

}
