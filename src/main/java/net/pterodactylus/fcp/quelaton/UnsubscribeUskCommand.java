package net.pterodactylus.fcp.quelaton;

/**
 * Unsubscribes from a USK.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface UnsubscribeUskCommand {

	Executable<Void> identifier(String identifier);

}
