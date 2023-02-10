package net.pterodactylus.fcp;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.common.io.ByteStreams;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit test for {@link AllData}
 */
public class AllDataTest {

	@Test
	public void allDataReturnsIdentifierFromFcpMessage() {
		receivedMessage.setField("Identifier", "identifier");
		assertThat(allData.getIdentifier(), equalTo("identifier"));
	}

	@Test
	public void allDataCanParseTheDataLengthFromTheFcpMessage() {
		receivedMessage.setField("DataLength", "1234567");
		assertThat(allData.getDataLength(), equalTo(1234567L));
	}

	@Test
	public void allDataReturnsMinus1ForUnparseableDataLength() {
		receivedMessage.setField("DataLength", "broken");
		assertThat(allData.getDataLength(), equalTo(-1L));
	}

	@Test
	public void allDataCanParseStartupTimeFromFcpMessage() {
		receivedMessage.setField("StartupTime", "1234567");
		assertThat(allData.getStartupTime(), equalTo(1234567L));
	}

	@Test
	public void allDataReturnsMinus1ForUnparseableStartupTime() {
		receivedMessage.setField("StartupTime", "broken");
		assertThat(allData.getStartupTime(), equalTo(-1L));
	}

	@Test
	public void allDataCanParseCompletionTimeFromFcpMessage() {
		receivedMessage.setField("CompletionTime", "1234567");
		assertThat(allData.getCompletionTime(), equalTo(1234567L));
	}

	@Test
	public void allDataReturnsMinus1ForUnparseableCompletionTime() {
		receivedMessage.setField("CompletionTime", "broken");
		assertThat(allData.getCompletionTime(), equalTo(-1L));
	}

	@Test
	public void allDataReturnsContentTypeFromFcpMessage() {
		receivedMessage.setField("Metadata.ContentType", "application/test");
		assertThat(allData.getContentType(), equalTo("application/test"));
	}

	@Test
	public void allDataReturnsGivenPayloadInputStream() throws IOException {
		byte[] payload = new byte[4];
		assertThat(allData.getPayloadInputStream().read(payload), equalTo(4));
		assertThat(payload, equalTo(new byte[] { 0, 1, 2, 3 }));
		assertThat(allData.getPayloadInputStream().read(), equalTo(-1));
	}

	private final FcpMessage receivedMessage = new FcpMessage("AllData");
	private final AllData allData = new AllData(receivedMessage, new ByteArrayInputStream(new byte[] { 0, 1, 2, 3 }));

}
