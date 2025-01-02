package net.pterodactylus.fcp.test;

import net.pterodactylus.fcp.FcpMessage;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MessageTests {

	public static <M extends FcpMessage> void verifyFieldValueAfterSettingFlag(M message, BiConsumer<? super M, Boolean> setter, Function<? super M, String> getter, boolean flag) {
		setter.accept(message, flag);
		assertThat(getter.apply(message), equalTo(String.valueOf(flag)));
	}

}
