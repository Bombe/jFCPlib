package net.pterodactylus.fcp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.pterodactylus.fcp.Persistence.unknown;
import static net.pterodactylus.fcp.Priority.minimum;
import static net.pterodactylus.fcp.Verbosity.COMPRESSION;
import static net.pterodactylus.fcp.Verbosity.PROGRESS;
import static net.pterodactylus.fcp.test.Matchers.isDataMessage;
import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * Unit test for {@link ClientPutComplexDir}.
 */
public class ClientPutComplexDirTest {

	@Test
	public void canCreateClientPutComplexDir() {
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", "Identifier=identifier", "URI=uri"));
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
		testThatFieldCanBeSet(cp -> cp.setPriority(minimum), "PriorityClass=6");
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
		testThatFieldCanBeSet(cp -> cp.setPersistence(unknown), "Persistence=unknown");
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
	public void defaultNameCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setDefaultName("default-name"), "DefaultName=default-name");
	}

	private void testThatFieldCanBeSet(Consumer<ClientPutComplexDir> setter, String expectedParameter) {
		setter.accept(clientPutComplexDir);
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", hasItem(expectedParameter)));
	}

	@Test
	public void directFilesAreAddedCorrectly() {
		clientPutComplexDir.addFileEntry(FileEntry.createDirectFileEntry("file.dat", "text/plain", 5, createInputStream("Test\n")));
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", allOf(
				hasItem("Files.0.Name=file.dat"),
				hasItem("Files.0.UploadFrom=direct"),
				hasItem("Files.0.DataLength=5"),
				hasItem("Files.0.Metadata.ContentType=text/plain")
		), contains("Test")));
	}

	@Test
	public void multipleDirectFilesAreAddedInCorrectOrder() {
		clientPutComplexDir.addFileEntry(FileEntry.createDirectFileEntry("file1.dat", "text/plain1", 4, createInputStream("Tes\n")));
		clientPutComplexDir.addFileEntry(FileEntry.createDirectFileEntry("file2.dat", "text/plain2", 6, createInputStream("tData\n")));
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", allOf(
				hasItem("Files.0.Name=file1.dat"),
				hasItem("Files.0.UploadFrom=direct"),
				hasItem("Files.0.DataLength=4"),
				hasItem("Files.0.Metadata.ContentType=text/plain1"),
				hasItem("Files.1.Name=file2.dat"),
				hasItem("Files.1.UploadFrom=direct"),
				hasItem("Files.1.DataLength=6"),
				hasItem("Files.1.Metadata.ContentType=text/plain2")
		), contains("Tes", "tData")));
	}

	@Test
	public void diskFileEntryIsAddedCorrectly() {
		clientPutComplexDir.addFileEntry(FileEntry.createDiskFileEntry("file1.dat", "/file/name", "text/plain1"));
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", allOf(
				hasItem("Files.0.Name=file1.dat"),
				hasItem("Files.0.UploadFrom=disk"),
				hasItem("Files.0.Filename=/file/name"),
				hasItem("Files.0.Metadata.ContentType=text/plain1")
		)));
	}

	@Test
	public void redirectFileEntryIsAddedCorrectly() {
		clientPutComplexDir.addFileEntry(FileEntry.createRedirectFileEntry("file1.dat", "target-uri"));
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", allOf(
				hasItem("Files.0.Name=file1.dat"),
				hasItem("Files.0.UploadFrom=redirect"),
				hasItem("Files.0.TargetURI=target-uri")
		)));
	}

	private static InputStream createInputStream(String text) {
		return new ByteArrayInputStream(text.getBytes(UTF_8));
	}

	@Test
	public void manuallySetPayloadIsIgnored() {
		clientPutComplexDir.setPayloadInputStream(createInputStream("Test\n"));
		assertThat(clientPutComplexDir, isDataMessage("ClientPutComplexDir", anything(), not(hasItem("Test"))));
	}

	private final ClientPutComplexDir clientPutComplexDir = new ClientPutComplexDir("identifier", "uri");

}
