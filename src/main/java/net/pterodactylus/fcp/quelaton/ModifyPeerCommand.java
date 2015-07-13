package net.pterodactylus.fcp.quelaton;

import java.util.Optional;

import net.pterodactylus.fcp.Peer;

/**
 * Command that modifies certain settings of a peer.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface ModifyPeerCommand {

	ModifyPeerCommand enable();
	ModifyPeerCommand disable();
	ModifyPeerCommand allowLocalAddresses();
	ModifyPeerCommand disallowLocalAddresses();
	ModifyPeerCommand setBurstOnly();
	ModifyPeerCommand clearBurstOnly();
	ModifyPeerCommand setListenOnly();
	ModifyPeerCommand clearListenOnly();
	ModifyPeerCommand ignoreSource();

	Executable<Optional<Peer>> byName(String name);
	Executable<Optional<Peer>> byIdentity(String nodeIdentity);
	Executable<Optional<Peer>> byHostAndPort(String host, int port);

}
