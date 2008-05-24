/**
 * 
 */

package net.pterodactylus.fcp.highlevel;

/**
 * Exception that is thrown when an operation is tried on a not connected
 * connection.
 * 
 * @author <a href="mailto:dr@ina-germany.de">David Roden</a>
 */
public class NotConnectedException extends HighLevelException {

	/**
	 * Creates a new not-connected exception.
	 */
	public NotConnectedException() {
		super();
	}

	/**
	 * Creates a new not-connected exception with the given message.
	 * 
	 * @param message
	 *            The message of the exception
	 */
	public NotConnectedException(String message) {
		super(message);
	}

	/**
	 * Creates a new not-connected exception with the given cause.
	 * 
	 * @param cause
	 *            The cause of the exception
	 */
	public NotConnectedException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new not-connected exception with the given message and cause.
	 * 
	 * @param message
	 *            The message of the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public NotConnectedException(String message, Throwable cause) {
		super(message, cause);
	}

}
