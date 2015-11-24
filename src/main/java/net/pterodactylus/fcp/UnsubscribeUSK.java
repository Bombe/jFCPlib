package net.pterodactylus.fcp;

/**
 * The “UnsubscribeUSK” message is used to cancel a {@link SubscribeUSK USK subscription}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class UnsubscribeUSK extends FcpMessage {

	public UnsubscribeUSK(String identifier) {
		super("UnsubscribeUSK");
		setField("Identifier", identifier);
	}

}
