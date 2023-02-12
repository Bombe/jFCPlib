package net.pterodactylus.fcp.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import net.pterodactylus.fcp.io.TempInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;


public class TempInputStreamTest {

	private byte[] prepareArrayOfNBytes(int n) {
		byte[] data = new byte[n];
		for (int i = 0;  i < n;  ++i) {
			data[i] = (byte)i;
		}
		return data;
	}

	private void checkTempInputStreamStoresPartOfAnotherStream(int length, int maxMemoryLength) throws IOException {
		byte[] originalData = prepareArrayOfNBytes(length + 1);
		InputStream anotherStream = new ByteArrayInputStream(originalData);
		TempInputStream cut = new TempInputStream(anotherStream, length, maxMemoryLength);

		// check length bytes are read from anotherStream and are accessible from cut
		byte[] buffer = new byte[length];
		int n = cut.read(buffer);
		assertThat(n, is(length));
		assertArrayEquals(Arrays.copyOf(originalData, length), buffer);
		assertThat(cut.read(), is(-1)); // check end of cut stream

		// check the rest of data in anotherStream is still there
		n = anotherStream.read(buffer);
		assertThat(n, is(1));
		assertThat(buffer[0], is(originalData[originalData.length - 1]));
		assertThat(anotherStream.read(), is(-1)); // check end of another stream
	}

	@Test
	public void tempInputStreamShouldCorrectlyStorePartOfAnotherStreamInMemory() throws IOException  {
		checkTempInputStreamStoresPartOfAnotherStream(1, 1);
	}

	@Test
	public void tempInputStreamShouldCorrectlyStorePartOfAnotherStreamInFile() throws IOException  {
		checkTempInputStreamStoresPartOfAnotherStream(2, 1);
	}

}
