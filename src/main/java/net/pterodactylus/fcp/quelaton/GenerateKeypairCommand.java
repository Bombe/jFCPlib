package net.pterodactylus.fcp.quelaton;

import net.pterodactylus.fcp.FcpKeyPair;

import java.util.concurrent.Future;

/**
 * Command to generate an SSK key pair.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface GenerateKeypairCommand extends Executable<FcpKeyPair> {

}
