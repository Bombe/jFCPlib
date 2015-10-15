package net.pterodactylus.fcp.quelaton;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Terminal operation for all FCP commands.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@FunctionalInterface
public interface Executable<R> {

	ListenableFuture<R> execute();

}
