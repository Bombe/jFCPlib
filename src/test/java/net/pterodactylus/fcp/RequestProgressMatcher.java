package net.pterodactylus.fcp;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Hamcrest matcher for {@link RequestProgress}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RequestProgressMatcher {

	public static Matcher<RequestProgress> isRequestProgress(int total, int required, int failed, int fatallyFailed,
		int succeeded, long lastProgress, boolean finalizedTotal, int minSuccessFetchBlocks) {
		return new TypeSafeDiagnosingMatcher<RequestProgress>() {
			@Override
			protected boolean matchesSafely(RequestProgress requestProgress, Description mismatchDescription) {
				if (requestProgress.getTotal() != total) {
					mismatchDescription.appendText("total is ").appendValue(requestProgress.getTotal());
					return false;
				}
				if (requestProgress.getRequired() != required) {
					mismatchDescription.appendText("required is ").appendValue(requestProgress.getRequired());
					return false;
				}
				if (requestProgress.getFailed() != failed) {
					mismatchDescription.appendText("failed is ").appendValue(requestProgress.getFailed());
					return false;
				}
				if (requestProgress.getFatallyFailed() != fatallyFailed) {
					mismatchDescription.appendText("fatally failed is ")
						.appendValue(requestProgress.getFatallyFailed());
					return false;
				}
				if (requestProgress.getSucceeded() != succeeded) {
					mismatchDescription.appendText("succeeded is ").appendValue(requestProgress.getSucceeded());
					return false;
				}
				if (requestProgress.getLastProgress() != lastProgress) {
					mismatchDescription.appendText("last progress is ").appendValue(requestProgress.getLastProgress());
					return false;
				}
				if (requestProgress.isFinalizedTotal() != finalizedTotal) {
					mismatchDescription.appendText("finalized total is ")
						.appendValue(requestProgress.isFinalizedTotal());
					return false;
				}
				if (requestProgress.getMinSuccessFetchBlocks() != minSuccessFetchBlocks) {
					mismatchDescription.appendText("min success fetch blocks is ")
						.appendValue(requestProgress.getMinSuccessFetchBlocks());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("total ").appendValue(total);
				description.appendText(", required ").appendValue(required);
				description.appendText(", failed ").appendValue(failed);
				description.appendText(", fatally failed ").appendValue(fatallyFailed);
				description.appendText(", succeeded ").appendValue(succeeded);
				description.appendText(", last progress ").appendValue(lastProgress);
				description.appendText(", finalized total ").appendValue(finalizedTotal);
				description.appendText(", min success fetch blocks ").appendValue(minSuccessFetchBlocks);
			}
		};
	}

}
