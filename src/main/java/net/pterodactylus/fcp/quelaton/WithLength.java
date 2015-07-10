package net.pterodactylus.fcp.quelaton;

/**
 * An intermediary interface for FCP commands that require a length parameter.
 *
 * @param <R>
 * 	The type of the next command part
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface WithLength<R> {

	R length(long length);

}
