package net.pterodactylus.fcp.quelaton;

/**
 * FCP client used to communicate with a Freenet node.
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface FcpClient {

	GetNodeCommand getNode();
	GetConfigCommand getConfig();
	ModifyConfigCommand modifyConfig();

	GenerateKeypairCommand generateKeypair();
	ClientGetCommand clientGet();
	ClientPutCommand clientPut();

	ListPeerCommand listPeer();
	ListPeersCommand listPeers();
	AddPeerCommand addPeer();
	ModifyPeerCommand modifyPeer();
	RemovePeerCommand removePeer();

	ListPeerNotesCommand listPeerNotes();
	ModifyPeerNoteCommand modifyPeerNote();

	LoadPluginCommand loadPlugin();
	ReloadPluginCommand reloadPlugin();

}
