package net.pterodactylus.fcp.quelaton;

import java.util.concurrent.ExecutionException;

/**
 * USK subscription object that is returned to the client application.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public interface UskSubscription {

	String getUri();
	void onUpdate(UskUpdater uskUpdater);
	void cancel() throws ExecutionException, InterruptedException;

	interface UskUpdater {

		void uskUpdated(int edition);

	}

}
