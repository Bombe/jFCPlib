package net.pterodactylus.fcp.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.fcp.FcpUtils;

/**
 * This input stream stores the content of another input stream either in a
 * file or in memory, depending on the length of the input stream.
 *
 * @author David ‘Bombe’ Roden &lt;bombe@freenetproject.org&gt;
 */
public class TempInputStream extends InputStream {

	/**
	 * The default maximum lenght for in-memory storage.
	 */
	public static final long MAX_LENGTH_MEMORY = 65536;

	/**
	 * The temporary file to read from.
	 */
	private final File tempFile;

	/**
	 * The input stream that reads from the file.
	 */
	private final InputStream fileInputStream;

	/**
	 * The input stream that reads from memory.
	 */
	private final InputStream memoryInputStream;

	/**
	 * Creates a new temporary input stream that stores the given input
	 * stream in a temporary file.
	 *
	 * @param originalInputStream The original input stream
	 * @throws IOException if an I/O error occurs
	 */
	public TempInputStream(InputStream originalInputStream) throws IOException {
		this(originalInputStream, -1);
	}

	/**
	 * Creates a new temporary input stream that stores the given input
	 * stream in memory if it is shorter than {@link #MAX_LENGTH_MEMORY},
	 * otherwise it is stored in a file.
	 *
	 * @param originalInputStream The original input stream
	 * @param length              The length of the input stream
	 * @throws IOException if an I/O error occurs
	 */
	public TempInputStream(InputStream originalInputStream, long length) throws IOException {
		this(originalInputStream, length, MAX_LENGTH_MEMORY);
	}

	/**
	 * Creates a new temporary input stream that stores the given input
	 * stream in memory if it is shorter than <code>maxMemoryLength</code>,
	 * otherwise it is stored in a file.
	 *
	 * @param originalInputStream The original input stream
	 * @param length              The length of the input stream
	 * @param maxMemoryLength     The maximum length to store in memory
	 * @throws IOException if an I/O error occurs
	 */
	public TempInputStream(InputStream originalInputStream, long length, long maxMemoryLength) throws IOException {
		if ((length > -1) && (length <= maxMemoryLength)) {
			ByteArrayOutputStream memoryOutputStream = new ByteArrayOutputStream((int) length);
			try {
				FcpUtils.copy(originalInputStream, memoryOutputStream, length, (int) length);
			} finally {
				memoryOutputStream.close();
			}
			tempFile = null;
			fileInputStream = null;
			memoryInputStream = new ByteArrayInputStream(memoryOutputStream.toByteArray());
		} else {
			tempFile = File.createTempFile("temp-", ".bin");
			tempFile.deleteOnExit();
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(tempFile);
				FcpUtils.copy(originalInputStream, fileOutputStream, length);
				fileInputStream = new FileInputStream(tempFile);
			} finally {
				FcpUtils.close(fileOutputStream);
			}
			memoryInputStream = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException {
		if (memoryInputStream != null) {
			return memoryInputStream.available();
		}
		return fileInputStream.available();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		if (memoryInputStream != null) {
			memoryInputStream.close();
			return;
		}
		tempFile.delete();
		fileInputStream.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void mark(int readlimit) {
		if (memoryInputStream != null) {
			memoryInputStream.mark(readlimit);
			return;
		}
		fileInputStream.mark(readlimit);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean markSupported() {
		if (memoryInputStream != null) {
			return memoryInputStream.markSupported();
		}
		return fileInputStream.markSupported();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		if (memoryInputStream != null) {
			return memoryInputStream.read();
		}
		return fileInputStream.read();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b) throws IOException {
		if (memoryInputStream != null) {
			return memoryInputStream.read(b);
		}
		return fileInputStream.read(b);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (memoryInputStream != null) {
			return memoryInputStream.read(b, off, len);
		}
		return fileInputStream.read(b, off, len);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void reset() throws IOException {
		if (memoryInputStream != null) {
			memoryInputStream.reset();
			return;
		}
		fileInputStream.reset();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip(long n) throws IOException {
		if (memoryInputStream != null) {
			return memoryInputStream.skip(n);
		}
		return fileInputStream.skip(n);
	}

}
