package net.pterodactylus.fcp;

import java.util.function.Consumer;

import org.junit.Test;

import static net.pterodactylus.fcp.Persistence.connection;
import static net.pterodactylus.fcp.Priority.interactive;
import static net.pterodactylus.fcp.UploadFrom.redirect;
import static net.pterodactylus.fcp.Verbosity.COMPRESSION;
import static net.pterodactylus.fcp.Verbosity.PROGRESS;
import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

/**
 * Unit test for {@link ClientPut}.
 */
public class ClientPutTest {

	@Test
	public void canCreateClientPutWithUriAndIdentifier() {
		ClientPut clientPut = new ClientPut("upload-uri", "identifier");
		assertThat(clientPut, isMessage("ClientPut", allOf(hasItem("URI=upload-uri"), hasItem("Identifier=identifier"))));
	}

	@Test
	public void clientPutWithoutUploadFromUsesDefaultUploadFrom() {
		ClientPut clientPut = new ClientPut("upload-uri", "identifier");
		assertThat(clientPut, isMessage("ClientPut", hasItem("UploadFrom=direct")));
	}

	@Test
	public void clientPutWithUploadFromUsesGivenUploadFrom() {
		ClientPut clientPut = new ClientPut("upload-uri", "identifier", redirect);
		assertThat(clientPut, isMessage("ClientPut", hasItem("UploadFrom=redirect")));
	}

	@Test
	public void contentTypeCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setMetadataContentType("test/content-type"), "Metadata.ContentType=test/content-type");
	}

	@Test
	public void verbosityCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setVerbosity(COMPRESSION.add(PROGRESS)), "Verbosity=513");
	}

	@Test
	public void maxRetriesCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setMaxRetries(12), "MaxRetries=12");
	}

	@Test
	public void priorityCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setPriority(interactive), "PriorityClass=1");
	}

	@Test
	public void getChkOnlyCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setGetCHKOnly(true), "GetCHKOnly=true");
	}

	@Test
	public void forkOnCacheableCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setForkOnCacheable(true), "ForkOnCacheable=true");
	}

	@Test
	public void extraInsertsSingleBlockCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setExtraInsertsSingleBlock(12), "ExtraInsertsSingleBlock=12");
	}

	@Test
	public void extraInsertsSplitfileHeaderBlockCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setExtraInsertsSplitfileHeaderBlock(23), "ExtraInsertsSplitfileHeaderBlock=23");
	}

	@Test
	public void globalCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setGlobal(true), "Global=true");
	}

	@Test
	public void dontCompressCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setDontCompress(true), "DontCompress=true");
	}

	@Test
	public void clientTokenCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setClientToken("client-token"), "ClientToken=client-token");
	}

	@Test
	public void persistenceCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setPersistence(connection), "Persistence=connection");
	}

	@Test
	public void targetFilenameCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setTargetFilename("target-filename"), "TargetFilename=target-filename");
	}

	@Test
	public void earlyEncodeCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setEarlyEncode(true), "EarlyEncode=true");
	}

	@Test
	public void dataLengthCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setDataLength(1234567), "DataLength=1234567");
	}

	@Test
	public void filenameCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setFilename("filename"), "Filename=filename");
	}

	@Test
	public void targetUriCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setTargetURI("target-uri"), "TargetURI=target-uri");
	}

	private void testThatFieldCanBeSet(Consumer<ClientPut> setter, String expectedField) {
		ClientPut clientPut = new ClientPut("upload-uri", "identifier");
		setter.accept(clientPut);
		assertThat(clientPut, isMessage("ClientPut", hasItem(expectedField)));
	}

}
