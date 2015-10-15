package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

/**
 * Subscribes to a USK.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface SubscribeUskCommand {

	Executable<Optional<UskSubscription>> uri(String uri);

}
