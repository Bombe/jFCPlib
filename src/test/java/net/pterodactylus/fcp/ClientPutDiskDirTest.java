package net.pterodactylus.fcp;

import java.util.function.Consumer;

import org.junit.Test;

import static net.pterodactylus.fcp.Persistence.forever;
import static net.pterodactylus.fcp.Priority.update;
import static net.pterodactylus.fcp.Verbosity.COMPRESSION;
import static net.pterodactylus.fcp.Verbosity.PROGRESS;
import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Unit test for {@link ClientPutDiskDir}.
 */
public class ClientPutDiskDirTest {

	@Test
	public void canCreateClientPutDiskDir() {
		assertThat(clientPutDiskDir, isMessage("ClientPutDiskDir", "URI=uri", "Identifier=identifier", "Filename=/directory"));
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
		testThatFieldCanBeSet(cp -> cp.setPriority(update), "PriorityClass=3");
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
		testThatFieldCanBeSet(cp -> cp.setPersistence(forever), "Persistence=forever");
	}

	@Test
	public void defaultNameCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setDefaultName("default-name"), "DefaultName=default-name");
	}

	@Test
	public void allowUnreadableFilesCanBeSet() {
		testThatFieldCanBeSet(cp -> cp.setAllowUnreadableFiles(true), "AllowUnreadableFiles=true");
	}

	private void testThatFieldCanBeSet(Consumer<ClientPutDiskDir> setter, String expectedParameter) {
		setter.accept(clientPutDiskDir);
		assertThat(clientPutDiskDir, isMessage("ClientPutDiskDir", hasItem(expectedParameter)));
	}

	private final ClientPutDiskDir clientPutDiskDir = new ClientPutDiskDir("uri", "identifier", "/directory");

}
