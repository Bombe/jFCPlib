package net.pterodactylus.fcp.quelaton;

import java.io.File;

/**
 * An intermediary interface for FCP commands that require a file parameter.
 *
 * @param <R>
 * 	The type of the next command part
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface WithFile<R> {

	R withFile(File file);

}
