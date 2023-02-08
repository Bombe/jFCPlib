package net.pterodactylus.fcp;

import org.junit.Test;

import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test for {@link WatchFeeds}.
 *
 * @author <a href="mailto:david.roden@bietr.de">David Roden</a>
 */
public class WatchFeedsTest {

	@Test
	public void enablingWatchFeedsSendsCorrectOutput() throws Exception {
		WatchFeeds watchFeeds = new WatchFeeds(true);
		assertThat(watchFeeds, isMessage("WatchFeeds", "Enabled=true"));
	}

	@Test
	public void disablingWatchFeedsSendsCorrectOutput() throws Exception {
		WatchFeeds watchFeeds = new WatchFeeds(false);
		assertThat(watchFeeds, isMessage("WatchFeeds", "Enabled=false"));
	}

}
