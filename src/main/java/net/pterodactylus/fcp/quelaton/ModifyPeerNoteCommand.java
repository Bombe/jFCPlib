package net.pterodactylus.fcp.quelaton;

/**
 * Command that modifies the note of a peer.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ModifyPeerNoteCommand {

	ModifyPeerNoteCommand darknetComment(String text);

	Executable<Boolean> byName(String name);
	Executable<Boolean> byIdentifier(String identifier);
	Executable<Boolean> byHostAndPort(String host, int port);

}
