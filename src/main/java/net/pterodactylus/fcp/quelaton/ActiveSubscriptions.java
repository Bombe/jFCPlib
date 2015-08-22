package net.pterodactylus.fcp.quelaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.pterodactylus.fcp.FcpAdapter;
import net.pterodactylus.fcp.FcpConnection;
import net.pterodactylus.fcp.FcpListener;
import net.pterodactylus.fcp.Priority;
import net.pterodactylus.fcp.SubscribeUSK;
import net.pterodactylus.fcp.SubscribedUSKUpdate;

/**
 * Maintains a record of active subscriptions.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class ActiveSubscriptions {

	private final Map<String, RemoteUskSubscription> subscriptions = Collections.synchronizedMap(new HashMap<>());

	public void renew(Consumer<FcpListener> fcpEventSender, Supplier<SubscribeUskCommand> subscribeUskCommandSupplier)
	throws ExecutionException, InterruptedException {
		fcpEventSender.accept(createFcpListener());
		for (UskSubscription uskSubscription : subscriptions.values()) {
			subscribeUskCommandSupplier.get().uri(uskSubscription.getUri()).execute().get();
		}
	}

	private FcpListener createFcpListener() {
		return new FcpAdapter() {
			@Override
			public void receivedSubscribedUSKUpdate(
				FcpConnection fcpConnection, SubscribedUSKUpdate subscribedUSKUpdate) {
				String identifier = subscribedUSKUpdate.getIdentifier();
				RemoteUskSubscription uskSubscription = subscriptions.get(identifier);
				if (uskSubscription == null) {
					/* TODO - log warning? */
					return;
				}
				uskSubscription.foundUpdate(subscribedUSKUpdate.getEdition());
			}
		};
	}

	public UskSubscription createUskSubscription(SubscribeUSK subscribeUSK) {
		RemoteUskSubscription remoteUskSubscription =
			new RemoteUskSubscription(subscribeUSK.getIdentifier(), subscribeUSK.getUri(), subscribeUSK.isActive(),
				subscribeUSK.isSparse(), subscribeUSK.getPriority(), subscribeUSK.getActivePriority(),
				subscribeUSK.isRealTime(), subscribeUSK.isIgnoreDateHints());
		subscriptions.put(subscribeUSK.getIdentifier(), remoteUskSubscription);
		return remoteUskSubscription;
	}

	private static class RemoteUskSubscription implements UskSubscription {

		private final String identifier;
		private final String uri;
		private final boolean active;
		private final boolean sparse;
		private final Priority priority;
		private final Priority activePriority;
		private final boolean realTime;
		private final boolean ignoreDateHints;
		private final List<UskUpdater> uskUpdaters = Collections.synchronizedList(new ArrayList<>());

		private RemoteUskSubscription(
			String identifier, String uri, boolean active, boolean sparse, Priority priority, Priority activePriority,
			boolean realTime, boolean ignoreDateHints) {
			this.identifier = identifier;
			this.uri = uri;
			this.active = active;
			this.sparse = sparse;
			this.priority = priority;
			this.activePriority = activePriority;
			this.realTime = realTime;
			this.ignoreDateHints = ignoreDateHints;
		}

		@Override
		public String getUri() {
			return uri;
		}

		@Override
		public void onUpdate(UskUpdater uskUpdater) {
			uskUpdaters.add(uskUpdater);
		}

		private void foundUpdate(int edition) {
			for (UskUpdater uskUpdater : uskUpdaters) {
				uskUpdater.uskUpdated(edition);
			}
		}

	}

}
