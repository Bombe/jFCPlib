package net.pterodactylus.fcp.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.pterodactylus.fcp.FcpMessage;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class Matchers {

	public static Matcher<FcpMessage> isMessage(String name, String... lines) {
		return isMessage(name, containsInAnyOrder(lines));
	}

	public static Matcher<FcpMessage> isMessage(String name, Matcher<? super Iterable<? super String>> linesMatcher) {
		return new TypeSafeDiagnosingMatcher<FcpMessage>() {
			@Override
			protected boolean matchesSafely(FcpMessage fcpMessage, Description mismatchDescription) {
				Iterator<String> messageLines = MessageUtils.encodeMessage(fcpMessage).iterator();
				String messageName = messageLines.next();
				if (!messageName.equals(name)) {
					mismatchDescription.appendText("name is ").appendValue(messageName);
					return false;
				}
				List<String> parameterLines = new ArrayList<>();
				while (true) {
					String actualLine = messageLines.next();
					if (actualLine.contains("=")) {
						parameterLines.add(actualLine);
					} else {
						if (!linesMatcher.matches(parameterLines)) {
							linesMatcher.describeMismatch(parameterLines, mismatchDescription);
							return false;
						}
						return true;
					}
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is message named ").appendValue(name)
						.appendText(" with parameters ").appendDescriptionOf(linesMatcher);
			}
		};
	}

}
