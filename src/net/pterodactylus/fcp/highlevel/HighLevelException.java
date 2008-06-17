/**
 * 
 */

package net.pterodactylus.fcp.highlevel;

/**
 * Base exception for high-level client exceptions.
 * 
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class HighLevelException extends Exception {

	/**
	 * Creates a new high-level client exception.
	 */
	public HighLevelException() {
		super();
	}

	/**
	 * Creates a new high-level client exception with the given message.
	 * 
	 * @param message
	 *            The message of the exception
	 */
	public HighLevelException(String message) {
		super(message);
	}

	/**
	 * Creates a new high-level client exception with the given cause.
	 * 
	 * @param cause
	 *            The cause of the exception
	 */
	public HighLevelException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new high-level client exception with the given message and
	 * cause.
	 * 
	 * @param message
	 *            The message of the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public HighLevelException(String message, Throwable cause) {
		super(message, cause);
	}

}
