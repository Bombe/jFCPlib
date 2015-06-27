package net.pterodactylus.fcp.fake;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;

/**
 * TODO
 *
 * @author <a href="bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
class TextSocket implements Closeable {

	private final Socket socket;
	private final InputStream socketInput;
	private final OutputStream socketOutput;
	private final BufferedReader inputReader;
	private final Writer outputWriter;

	TextSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.socketInput = socket.getInputStream();
		this.socketOutput = socket.getOutputStream();
		this.inputReader = new BufferedReader(new InputStreamReader(socketInput, "UTF-8"));
		this.outputWriter = new OutputStreamWriter(socketOutput, "UTF-8");
	}

	public String readLine() throws IOException {
		return inputReader.readLine();
	}

	public void writeLine(String line) throws IOException {
		outputWriter.write(line + "\n");
		outputWriter.flush();
	}

	public List<String> collectUntil(Matcher<String> lineMatcher) throws IOException {
		List<String> collectedLines = new ArrayList<String>();
		while (true) {
			String line = readLine();
			if (line == null) {
				throw new EOFException();
			}
			collectedLines.add(line);
			if (lineMatcher.matches(line)) {
				break;
			}
		}
		return collectedLines;
	}

	@Override
	public void close() throws IOException {
		outputWriter.close();
		inputReader.close();
		socketOutput.close();
		socketInput.close();
		socket.close();
	}

}
