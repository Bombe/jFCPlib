package net.pterodactylus.fcp;

import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.junit.Test;

import static net.pterodactylus.fcp.Persistence.reboot;
import static net.pterodactylus.fcp.Priority.bulkSplitfile;
import static net.pterodactylus.fcp.Verbosity.COMPRESSION;
import static net.pterodactylus.fcp.Verbosity.PROGRESS;
import static net.pterodactylus.fcp.test.Matchers.isMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

/**
 * Unit test for {@link ClientGet}.
 */
public class ClientGetTest {

	@Test
	public void canCreateClientGet() {
		ClientGet clientGet = new ClientGet("uri", "identifier");
		assertThat(clientGet, isMessage("ClientGet",
				"URI=uri",
				"Identifier=identifier",
				"ReturnType=direct"
		));
	}

	@Test
	public void canCreateClientGetWithReturnType() {
		ClientGet clientGet = new ClientGet("uri", "identifier", ReturnType.disk);
		assertThat(clientGet, isMessage("ClientGet",
				"URI=uri",
				"Identifier=identifier",
				"ReturnType=disk"
		));
	}

	@Test
	public void ignoreDataStoreFlagIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setIgnoreDataStore(true), "IgnoreDS=true");
	}

	@Test
	public void setDataStoreOnlyFlagIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setDataStoreOnly(true), "DSonly=true");
	}

	@Test
	public void verbosityIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setVerbosity(COMPRESSION.add(PROGRESS)), "Verbosity=513");
	}

	@Test
	public void maxSizeIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setMaxSize(1234567), "MaxSize=1234567");
	}

	@Test
	public void maxTempSizeIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setMaxTempSize(2345678), "MaxTempSize=2345678");
	}

	@Test
	public void maxRetriesIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setMaxRetries(12), "MaxRetries=12");
	}

	@Test
	public void priorityIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setPriority(bulkSplitfile), "PriorityClass=4");
	}

	@Test
	public void realTimeFlagIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setRealTimeFlag(true), "RealTimeFlag=true");
	}

	@Test
	public void persistenceIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setPersistence(reboot), "Persistence=reboot");
	}

	@Test
	public void clientTokenIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setClientToken("clientToken"), "ClientToken=clientToken");
	}

	@Test
	public void globalIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setGlobal(true), "Global=true");
	}

	@Test
	public void binaryBlobIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setBinaryBlob(true), "BinaryBlob=true");
	}

	@Test
	public void filterDataIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setFilterData(true), "FilterData=true");
	}

	@Test
	public void allowedMimeTypesAreIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setAllowedMimeTypes("text/plain", "text/html"), "AllowedMIMETypes=text/plain;text/html");
	}

	@Test
	public void filenameIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setFilename("file.dat"), "Filename=file.dat");
	}

	@Test
	public void tempFilenameIsIncluded() {
		testThatAFieldIsIncluded(cg -> cg.setTempFilename("temp.dat"), "TempFilename=temp.dat");
	}

	private void testThatAFieldIsIncluded(Consumer<ClientGet> setter, String expectedResult) {
		ClientGet clientGet = new ClientGet("uri", "identifier");
		setter.accept(clientGet);
		assertThat(clientGet, isMessage("ClientGet", allOf(
				containsBoilerPlate(),
				hasItem(expectedResult)
		)));
	}

	private static Matcher<? super Iterable<? super String>> containsBoilerPlate() {
		return allOf(hasItem("URI=uri"), hasItem("Identifier=identifier"), hasItem("ReturnType=direct"));
	}

}
