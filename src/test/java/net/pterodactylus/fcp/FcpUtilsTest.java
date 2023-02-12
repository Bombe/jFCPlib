package net.pterodactylus.fcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import static java.util.Collections.synchronizedSet;
import static net.pterodactylus.fcp.FcpUtils.close;
import static net.pterodactylus.fcp.FcpUtils.copy;
import static net.pterodactylus.fcp.FcpUtils.decodeMultiIntegerField;
import static net.pterodactylus.fcp.FcpUtils.encodeMultiIntegerField;
import static net.pterodactylus.fcp.FcpUtils.encodeMultiStringField;
import static net.pterodactylus.fcp.FcpUtils.getUniqueIdentifier;
import static net.pterodactylus.fcp.FcpUtils.safeParseInt;
import static net.pterodactylus.fcp.FcpUtils.safeParseLong;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;

/**
 * Unit test for {@link FcpUtils}.
 */
public class FcpUtilsTest {

	@Test
	public void uniqueIdentifiersAreIndeedAtLeastSomewhatUnique() {
		Set<String> identifiers = synchronizedSet(new HashSet<>(100 * 10000));
		List<Thread> threads = new ArrayList<>();
		for (int threadIndex = 0; threadIndex < 100; threadIndex++) {
			Thread thread = new Thread(() -> {
				for (int index = 0; index < 10000; index++) {
					identifiers.add(getUniqueIdentifier());
				}
			});
			threads.add(thread);
			thread.start();
		}
		threads.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
		assertThat(identifiers, hasSize(100 * 10000));
	}

	@Test
	public void decodeMultiIntegerFieldCanDecodeASingleInteger() {
		assertThat(decodeMultiIntegerField("123"), equalTo(new int[] { 123 }));
	}

	@Test
	public void decodeMultiIntegerFieldCanDecodeMultipleIntegers() {
		assertThat(decodeMultiIntegerField("123;234;345"), equalTo(new int[] { 123, 234, 345 }));
	}

	@Test
	public void decodeMultiIntegerFieldThrowsExceptionIfSingleFieldIsNotANumber() {
		assertThrows(NumberFormatException.class, () -> decodeMultiIntegerField("not-a-number"));
	}

	@Test
	public void decodeMultiIntegerFieldThrowsExceptionIfMultipleFieldsContainNotANumber() {
		assertThrows(NumberFormatException.class, () -> decodeMultiIntegerField("1;not-a-number;3"));
	}

	@Test
	public void encodeMultiIntegerFieldEncodesASingleIntegerCorrectly() {
		assertThat(encodeMultiIntegerField(new int[] { 123 }), equalTo("123"));
	}

	@Test
	public void encodeMultiIntegerFieldEncodesMultipleIntegersCorrectly() {
		assertThat(encodeMultiIntegerField(new int[] { 123, 234, 345 }), equalTo("123;234;345"));
	}

	@Test
	public void encodeMultiStringFieldEncodesASingleStringCorrectly() {
		assertThat(encodeMultiStringField(new String[] { "abc" }), equalTo("abc"));
	}

	@Test
	public void encodeMultiStringFieldEncodesMultipleStringsCorrectly() {
		assertThat(encodeMultiStringField(new String[] { "abc", "def", "ghi" }), equalTo("abc;def;ghi"));
	}

	@Test
	public void safeParseIntCanParseANumericStringCorrectly() {
		assertThat(safeParseInt("123"), equalTo(123));
	}

	@Test
	public void safeParseIntReturnsMinus1OnInvalidNumber() {
		assertThat(safeParseInt("not-a-number"), equalTo(-1));
	}

	@Test
	public void safeParseIntWithDefaultValueCanParseANumericStringCorrectly() {
		assertThat(safeParseInt("123", 234), equalTo(123));
	}

	@Test
	public void safeParseIntWithDefaultValueReturnsDefaultValueOnInvalidNumericString() {
		assertThat(safeParseInt("not-a-number", 234), equalTo(234));
	}

	@Test
	public void safeParseLongCanParseANumericStringCorrectly() {
		assertThat(safeParseLong("12345678901"), equalTo(12345678901L));
	}

	@Test
	public void safeParseLongReturnsMinus1OnInvalidNumber() {
		assertThat(safeParseLong("not-a-number"), equalTo(-1L));
	}

	@Test
	public void safeParseLongWithDefaultValueCanParseANumericStringCorrectly() {
		assertThat(safeParseLong("12345678901", 234), equalTo(12345678901L));
	}

	@Test
	public void safeParseLongWithDefaultValueReturnsDefaultValueOnInvalidNumericString() {
		assertThat(safeParseLong("not-a-number", 234), equalTo(234L));
	}

	@Test
	public void socketIsClosed() {
		AtomicBoolean closed = new AtomicBoolean(false);
		Socket socket = new Socket() {
			@Override
			public void close() {
				closed.set(true);
			}
		};
		close(socket);
		assertThat(closed.get(), equalTo(true));
	}

	@Test
	public void exceptionWhileClosingASocketIsIgnored() {
		Socket socket = new Socket() {
			@Override
			public void close() throws IOException {
				throw new IOException();
			}
		};
		close(socket);
	}

	@Test
	public void closingANullSocketDoesNotDoAnything() {
		close(null);
	}

	@Test
	public void closingACloseableClosesTheCloseable() {
		AtomicBoolean closed = new AtomicBoolean(false);
		Closeable closeable = () -> closed.set(true);
		close(closeable);
		assertThat(closed.get(), equalTo(true));
	}

	@Test
	public void exceptionWhileClosingACloseableIsIgnored() {
		Closeable closeable = () -> {
			throw new IOException();
		};
		close(closeable);
	}

	@Test
	public void closingANullCloseableDoesNothing() {
		close((Closeable) null);
	}

	@Test
	public void copyingAStreamCopiesAllOfItsContents() throws IOException {
		AtomicLong writtenBytes = new AtomicLong(0);
		try (InputStream largeInputStream = getLimitedInputStream();
			 OutputStream outputStream = getCountingOutputStream(writtenBytes)) {
			copy(largeInputStream, outputStream);
		}
		assertThat(writtenBytes.get(), equalTo(1024 * 1024 * 1024L));
	}

	@Test
	public void copyingAStreamWithALimitCopiesOnlyTheRequestedAmountOfBytes() throws IOException {
		AtomicLong writtenBytes = new AtomicLong(0);
		try (InputStream largeInputStream = getLimitedInputStream();
			 OutputStream outputStream = getCountingOutputStream(writtenBytes)) {
			copy(largeInputStream, outputStream, 1024 * 1024);
		}
		assertThat(writtenBytes.get(), equalTo(1024 * 1024L));
	}

	@Test
	public void tryingToCopyMoreBytesThanThereAreInTheInputStreamThrowsEofException() {
		assertThrows(EOFException.class, () -> copy(new ByteArrayInputStream(new byte[] { 1, 2, 3 }), new ByteArrayOutputStream(), 4));
	}

	private static InputStream getLimitedInputStream() {
		return new InputStream() {
			private long remaining = 1024 * 1024 * 1024;

			@Override
			public int read(byte[] b, int off, int len) {
				if (remaining == 0) {
					return -1;
				}
				long maxToRead = Math.min(remaining, len);
				for (int index = 0; index < maxToRead; index++) {
					b[off + index] = (byte) --remaining;
				}
				return (int) maxToRead;
			}

			@Override
			public int read() {
				remaining--;
				if (remaining == -1) {
					return -1;
				}
				return (int) (remaining & 0xff);
			}
		};
	}

	private static OutputStream getCountingOutputStream(AtomicLong writtenBytes) {
		return new OutputStream() {
			@Override
			public void write(int b) {
				writtenBytes.incrementAndGet();
			}

			@Override
			public void write(byte[] b, int off, int len) {
				writtenBytes.addAndGet(len);
			}
		};
	}

}
