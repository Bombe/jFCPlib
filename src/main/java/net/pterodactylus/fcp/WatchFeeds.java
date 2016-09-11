package net.pterodactylus.fcp;

/**
 * Implementation of the “WatchFeeds” command.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WatchFeeds extends FcpMessage {

	public WatchFeeds(boolean enabled) {
		super("WatchFeeds");
		setField("Enabled", String.valueOf(enabled));
	}

}
