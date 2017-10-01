package net.pterodactylus.fcp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import org.junit.Test;

/**
 * Unit test for {@link WatchFeeds}.
 *
 * @author <a href="mailto:david.roden@bietr.de">David Roden</a>
 */
public class WatchFeedsTest extends AbstractFcpMessageTest {

	@Test
	public void enablingWatchFeedsSendsCorrectOutput() throws Exception {
		WatchFeeds watchFeeds = new WatchFeeds(true);
		assertThat(encodeMessage(watchFeeds), contains(
			"WatchFeeds",
			"Enabled=true",
			"EndMessage"
		));
	}

	@Test
	public void disablingWatchFeedsSendsCorrectOutput() throws Exception {
		WatchFeeds watchFeeds = new WatchFeeds(false);
		assertThat(encodeMessage(watchFeeds), contains(
			"WatchFeeds",
			"Enabled=false",
			"EndMessage"
		));
	}

}
