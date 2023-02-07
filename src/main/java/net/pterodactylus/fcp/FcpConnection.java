package net.pterodactylus.fcp;

import java.io.Closeable;
import java.io.IOException;

/**
 * An FCP connection to a Freenet node.
 */
public interface FcpConnection extends Closeable {

	/**
	 * Adds the given listener to the list of listeners.
	 *
	 * @param fcpListener The listener to add
	 */
	void addFcpListener(FcpListener fcpListener);

	/**
	 * Removes the given listener from the list of listeners.
	 *
	 * @param fcpListener The listener to remove
	 */
	void removeFcpListener(FcpListener fcpListener);

	/**
	 * Returns whether this connection has been closed, either by {@link #close()} or by the remote side.
	 *
	 * @return {@code true} if this connection is closed, {@code false} otherwise
	 */
	boolean isClosed();

	/**
	 * Connects to the node.
	 *
	 * @throws IOException           if an I/O error occurs
	 * @throws IllegalStateException if there is already a connection to the node
	 */
	void connect() throws IOException, IllegalStateException;

	/**
	 * Disconnects from the node. If there is no connection to the node, this
	 * method does nothing.
	 *
	 * @deprecated Use {@link #close()} instead
	 */
	@Deprecated
	void disconnect();

	/**
	 * Closes the connection. If there is no connection to the node, this
	 * method does nothing.
	 */
	@Override
	void close();

	/**
	 * Sends the given FCP message.
	 *
	 * @param fcpMessage The FCP message to send
	 * @throws IOException if an I/O error occurs
	 */
	void sendMessage(FcpMessage fcpMessage) throws IOException;

}
