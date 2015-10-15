package net.pterodactylus.fcp;

/**
 * All possible peer note types.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public enum PeerNoteType {

	PRIVATE_DARKNET_COMMENT(1);

	private int value;

	PeerNoteType(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

}
