package net.pterodactylus.fcp.quelaton;

/**
 * FCP client used to communicate with a Freenet node.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface FcpClient {

	GenerateKeypairCommand generateKeypair();
	ClientGetCommand clientGet();
	ClientPutCommand clientPut();

	ListPeersCommand listPeers();

}
