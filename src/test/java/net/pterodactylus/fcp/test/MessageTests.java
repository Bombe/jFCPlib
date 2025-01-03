package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.FcpMessage;
import org.hamcrest.Matcher;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessageTests {

	public static <M extends FcpMessage> void verifyPropertyAfterSettingFlag(M message, BiConsumer<? super M, Boolean> setter, Function<? super M, String> getter, boolean flag) {
		setter.accept(message, flag);
		assertThat(getter.apply(message), equalTo(String.valueOf(flag)));
	}

	public static <M extends FcpMessage> void verifyFieldValueAfterSettingFlag(M message, BiConsumer<? super M, Boolean> setter, String fieldName, boolean flag) {
		verifyPropertyAfterSettingFlag(message, setter, m -> m.getField(fieldName), false);
	}

	public static <M extends FcpMessage> void verifyFieldValueAfterSettingProperty(M message, BiConsumer<M, String> setter, String fieldName, String value) {
		verifyFieldValueAfterSettingProperty(message, setter, fieldName, value, equalTo(value));
	}

	public static <M extends FcpMessage, V> void verifyFieldValueAfterSettingProperty(M message, BiConsumer<M, ? super V> setter, String fieldName, V value, Matcher<? super String> matcher) {
		setter.accept(message, value);
		assertThat(message.getField(fieldName), matcher);
	}

}
