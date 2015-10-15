package net.pterodactylus.fcp.quelaton;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.ClientPutDiskDir;
import net.pterodactylus.fcp.Key;
import net.pterodactylus.fcp.ProtocolError;
import net.pterodactylus.fcp.PutSuccessful;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link ClientPutDiskDirCommand} implemented based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ClientPutDiskDirCommandImpl implements ClientPutDiskDirCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final Supplier<String> identifierGenerator;
	private final AtomicReference<String> directory = new AtomicReference<>();
	private final AtomicReference<String> uri = new AtomicReference<>();

	public ClientPutDiskDirCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		this.identifierGenerator = identifierGenerator;
	}

	@Override
	public WithUri fromDirectory(File directory) {
		this.directory.set(Objects.requireNonNull(directory).getPath());
		return this::uri;
	}

	public Executable<Optional<Key>> uri(String uri) {
		this.uri.set(Objects.requireNonNull(uri));
		return () -> threadPool.submit(this::execute);
	}

	private Optional<Key> execute() throws IOException, ExecutionException, InterruptedException {
		ClientPutDiskDir clientPutDiskDir = new ClientPutDiskDir(uri.get(), identifierGenerator.get(), directory.get());
		try (ClientPutDiskDirDialog clientPutDiskDirDialog = new ClientPutDiskDirDialog()) {
			return clientPutDiskDirDialog.send(clientPutDiskDir).get();
		}
	}

	private class ClientPutDiskDirDialog extends FcpDialog<Optional<Key>> {

		public ClientPutDiskDirDialog() throws IOException {
			super(threadPool, connectionSupplier.get(), Optional.<Key>empty());
		}

		@Override
		protected void consumePutSuccessful(PutSuccessful putSuccessful) {
			setResult(Optional.of(new Key(putSuccessful.getURI())));
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finish();
		}

	}

}
