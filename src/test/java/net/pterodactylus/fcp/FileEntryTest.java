package net.pterodactylus.fcp;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

public class FileEntryTest {

	@Test
	public void directFileEntryHasCorrectType() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", null, 0, null);
		assertThat(fileEntry.getFields(), hasEntry("UploadFrom", "direct"));
	}

	@Test
	public void directFileEntryHasCorrectName() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", null, 0, null);
		assertThat(fileEntry.getFields(), hasEntry("Name", "direct.entry"));
	}

	@Test
	public void directFileEntryHasUploadFromSetToDirect() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", null, 0, null);
		assertThat(fileEntry.getFields(), hasEntry("UploadFrom", "direct"));
	}

	@Test
	public void directFileEntryHasCorrectContentType() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", "application/test", 0, null);
		assertThat(fileEntry.getFields(), hasEntry("Metadata.ContentType", "application/test"));
	}

	@Test
	public void directFileHasNoContentTypeIfContentTypeIsNull() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", null, 0, null);
		assertThat(fileEntry.getFields(), not(hasKey("Metadata.ContentType")));
	}

	@Test
	public void directFileEntryHasCorrectDataLength() {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", "application/test", 12, null);
		assertThat(fileEntry.getFields(), hasEntry("DataLength", "12"));
	}

	@Test
	public void directFileEntryHasCorrectInputStream() throws IOException {
		FileEntry fileEntry = FileEntry.createDirectFileEntry("direct.entry", "application/test", 12, new ByteArrayInputStream("Hello World!".getBytes(UTF_8)));
		try (InputStream inputStream = fileEntry.getInputStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			FcpUtils.copy(inputStream, outputStream);
			assertThat(outputStream.toByteArray(), equalTo("Hello World!".getBytes(UTF_8)));
		}
	}

	@Test
	public void diskFileEntryHasCorrectType() {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", null, null);
		assertThat(fileEntry.getFields(), hasEntry("UploadFrom", "disk"));
	}

	@Test
	public void diskFileEntryHasCorrectName() {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", null, null);
		assertThat(fileEntry.getFields(), hasEntry("Name", "file.entry"));
	}

	@Test
	public void diskFileEntryHasCorrectFileName() {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", "some/file.name", null);
		assertThat(fileEntry.getFields(), hasEntry("Filename", "some/file.name"));
	}

	@Test
	public void diskFileEntryHasCorrectContentType() {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", null, "application/test");
		assertThat(fileEntry.getFields(), hasEntry("Metadata.ContentType", "application/test"));
	}

	@Test
	public void diskFileEntryWithNullContentTypeHasNoContentType() {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", null, null);
		assertThat(fileEntry.getFields(), not(hasKey("Metadata.ContentType")));
	}

	@Test
	public void diskFileEntryHasEmptyInputStream() throws IOException {
		FileEntry fileEntry = FileEntry.createDiskFileEntry("file.entry", null, null);
		try (InputStream inputStream = fileEntry.getInputStream()) {
			assertThat(inputStream.read(), equalTo(-1));
		}
	}

	@Test
	public void redirectFileEntryHasCorrectType() {
		FileEntry fileEntry = FileEntry.createRedirectFileEntry("file.entry", "target-uri");
		assertThat(fileEntry.getFields(), hasEntry("UploadFrom", "redirect"));
	}

	@Test
	public void redirectFileEntryHasCorrectName() {
		FileEntry fileEntry = FileEntry.createRedirectFileEntry("file.entry", "target-uri");
		assertThat(fileEntry.getFields(), hasEntry("Name", "file.entry"));
	}

	@Test
	public void redirectFileEntryHasCorrectTarget() {
		FileEntry fileEntry = FileEntry.createRedirectFileEntry("file.entry", "target-uri");
		assertThat(fileEntry.getFields(), hasEntry("TargetURI", "target-uri"));
	}

	@Test
	public void redirectFileEntryHasEmptyInputStream() throws IOException {
		FileEntry fileEntry = FileEntry.createRedirectFileEntry("file.entry", "target-uri");
		try (InputStream inputStream = fileEntry.getInputStream()) {
			assertThat(inputStream.read(), equalTo(-1));
		}
	}

}
