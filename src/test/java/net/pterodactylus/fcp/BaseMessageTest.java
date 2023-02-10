package net.pterodactylus.fcp;

import org.junit.Test;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit test for {@link BaseMessage}.
 */
public class BaseMessageTest {

	@Test
	public void baseMessageForwardsNameFromReceivedMessage() {
		assertThat(baseMessage.getName(), equalTo("TestMessage"));
	}

	@Test
	public void fieldFromReceivedMessageCanBeAccessed() {
		receivedMessage.setField("TestField", "TestValue");
		assertThat(baseMessage.getField("TestField"), equalTo("TestValue"));
	}

	@Test
	public void allFieldsFromReceivedMessageCanBeAccessed() {
		receivedMessage.setField("TestField1", "TestValue1");
		receivedMessage.setField("TestField2", "TestValue2");
		assertThat(baseMessage.getFields().entrySet().stream()
						.map(e -> format("%s: %s", e.getKey(), e.getValue())).collect(toList()),
				containsInAnyOrder("TestField1: TestValue1", "TestField2: TestValue2")
		);
	}

	private final FcpMessage receivedMessage = new FcpMessage("TestMessage");
	private final BaseMessage baseMessage = new BaseMessage(receivedMessage);

}
