package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ClientPut;
import net.pterodactylus.fcp.FcpMessage;
import net.pterodactylus.fcp.Key;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutFailed;
import net.pterodactylus.fcp.PutSuccessful;
import net.pterodactylus.fcp.TestDDAComplete;
import net.pterodactylus.fcp.TestDDAReply;
import net.pterodactylus.fcp.TestDDARequest;
import net.pterodactylus.fcp.TestDDAResponse;
import net.pterodactylus.fcp.URIGenerated;
import net.pterodactylus.fcp.UploadFrom;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ClientPutCommand} implemented based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class ClientPutCommandImpl implements ClientPutCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;
	private final AtomicReference<String> redirectUri = new AtomicReference<>();
	private final AtomicReference<File> file = new AtomicReference<>();
	private final AtomicReference<InputStream> payload = new AtomicReference<>();
	private final AtomicLong length = new AtomicLong();
	private final AtomicReference<String> targetFilename = new AtomicReference<>();
	private final List<Consumer<String>> keyGenerateds = new CopyOnWriteArrayList<>();

	public ClientPutCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
	}

	@Override
	public ClientPutCommand onKeyGenerated(Consumer<String> keyGenerated) {
		keyGenerateds.add(keyGenerated);
		return this;
	}

	@Override
	public ClientPutCommand named(String targetFilename) {
		this.targetFilename.set(targetFilename);
		return this;
	}

	@Override
	public WithUri redirectTo(String uri) {
		this.redirectUri.set(Objects.requireNonNull(uri, "uri must not be null"));
		return this::key;
	}

	@Override
	public WithUri from(File file) {
		this.file.set(Objects.requireNonNull(file, "file must not be null"));
		return this::key;
	}

	@Override
	public WithLength from(InputStream inputStream) {
		payload.set(Objects.requireNonNull(inputStream, "inputStream must not be null"));
		return this::length;
	}

	private WithUri length(long length) {
		this.length.set(length);
		return this::key;
	}

	private Executable<Optional<Key>> key(String uri) {
		return () -> threadPool.submit(() -> execute(uri));
	}

	private Optional<Key> execute(String uri) throws InterruptedException, ExecutionException, IOException {
		ClientPut clientPut = createClientPutCommand(uri, identifierGenerator.get());
		try (ClientPutDialog clientPutDialog = new ClientPutDialog()) {
			return clientPutDialog.send(clientPut).get();
		}
	}

	private ClientPut createClientPutCommand(String uri, String identifier) {
		ClientPut clientPut;
		if (file.get() != null) {
			clientPut = createClientPutFromDisk(uri, identifier, file.get());
		} else if (redirectUri.get() != null) {
			clientPut = createClientPutRedirect(uri, identifier, redirectUri.get());
		} else {
			clientPut = createClientPutDirect(uri, identifier, length.get(), payload.get());
		}
		if (targetFilename.get() != null) {
			clientPut.setTargetFilename(targetFilename.get());
		}
		return clientPut;
	}

	private ClientPut createClientPutFromDisk(String uri, String identifier, File file) {
		ClientPut clientPut = new ClientPut(uri, identifier, UploadFrom.disk);
		clientPut.setFilename(file.getAbsolutePath());
		return clientPut;
	}

	private ClientPut createClientPutRedirect(String uri, String identifier, String redirectUri) {
		ClientPut clientPut = new ClientPut(uri, identifier, UploadFrom.redirect);
		clientPut.setTargetURI(redirectUri);
		return clientPut;
	}

	private ClientPut createClientPutDirect(String uri, String identifier, long length, InputStream payload) {
		ClientPut clientPut = new ClientPut(uri, identifier, UploadFrom.direct);
		clientPut.setDataLength(length);
		clientPut.setPayloadInputStream(payload);
		return clientPut;
	}

	private class ClientPutDialog extends FcpDialog<Optional<Key>> {

		private final AtomicReference<FcpMessage> originalClientPut = new AtomicReference<>();
		private final AtomicReference<String> directory = new AtomicReference<>();
		private final AtomicReference<Key> finalKey = new AtomicReference<>();
		private final AtomicBoolean putFinished = new AtomicBoolean();

		public ClientPutDialog() throws IOException {
			super(ClientPutCommandImpl.this.threadPool, ClientPutCommandImpl.this.connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return putFinished.get();
		}

		@Override
		protected Optional<Key> getResult() {
			return Optional.ofNullable(finalKey.get());
		}

		@Override
		public ListenableFuture<Optional<Key>> send(FcpMessage fcpMessage) throws IOException {
			originalClientPut.set(fcpMessage);
			String filename = fcpMessage.getField("Filename");
			if (filename != null) {
				directory.set(new File(filename).getParent());
			}
			return super.send(fcpMessage);
		}

		@Override
		protected void consumeURIGenerated(URIGenerated uriGenerated) {
			for (Consumer<String> keyGenerated : keyGenerateds) {
				keyGenerated.accept(uriGenerated.getURI());
			}
		}

		@Override
		protected void consumePutSuccessful(PutSuccessful putSuccessful) {
			finalKey.set(new Key(putSuccessful.getURI()));
			putFinished.set(true);
		}

		@Override
		protected void consumePutFailed(PutFailed putFailed) {
			putFinished.set(true);
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			if (protocolError.getCode() == 25) {
				setIdentifier(directory.get());
				sendMessage(new TestDDARequest(directory.get(), true, false));
			} else {
				putFinished.set(true);
			}
		}

		@Override
		protected void consumeTestDDAReply(TestDDAReply testDDAReply) {
			try {
				String readContent = Files.readAllLines(new File(testDDAReply.getReadFilename()).toPath()).get(0);
				sendMessage(new TestDDAResponse(directory.get(), readContent));
			} catch (IOException e) {
				sendMessage(new TestDDAResponse(directory.get(), "failed-to-read"));
			}
		}

		@Override
		protected void consumeTestDDAComplete(TestDDAComplete testDDAComplete) {
			setIdentifier(originalClientPut.get().getField("Identifier"));
			sendMessage(originalClientPut.get());
		}

	}

}
