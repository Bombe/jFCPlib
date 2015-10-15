package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import net.pterodactylus.fcp.LoadPlugin;
import net.pterodactylus.fcp.LoadPlugin.OfficialSource;
import net.pterodactylus.fcp.LoadPlugin.UrlType;
import net.pterodactylus.fcp.PluginInfo;
import net.pterodactylus.fcp.ProtocolError;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link LoadPluginCommand} implementation based on {@link FcpDialog}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LoadPluginCommandImpl implements LoadPluginCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final LoadPlugin loadPlugin;

	public LoadPluginCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier, Supplier<String> identifierGenerator) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
		loadPlugin = new LoadPlugin(identifierGenerator.get());
	}

	@Override
	public LoadPluginCommand addToConfig() {
		loadPlugin.setStore(true);
		return this;
	}

	@Override
	public Executable<Optional<PluginInfo>> officialFromFreenet(String pluginIdentifier) {
		loadPlugin.setUrlType(UrlType.OFFICIAL);
		loadPlugin.setOfficialSource(OfficialSource.FREENET);
		loadPlugin.setPluginUrl(pluginIdentifier);
		return this::execute;
	}

	@Override
	public Executable<Optional<PluginInfo>> officialFromHttps(String pluginIdentifier) {
		loadPlugin.setUrlType(UrlType.OFFICIAL);
		loadPlugin.setOfficialSource(OfficialSource.HTTPS);
		loadPlugin.setPluginUrl(pluginIdentifier);
		return this::execute;
	}

	@Override
	public Executable<Optional<PluginInfo>> fromFreenet(String key) {
		loadPlugin.setUrlType(UrlType.FREENET);
		loadPlugin.setPluginUrl(key);
		return this::execute;
	}

	@Override
	public Executable<Optional<PluginInfo>> fromFile(String filename) {
		loadPlugin.setUrlType(UrlType.FILE);
		loadPlugin.setPluginUrl(filename);
		return this::execute;
	}

	@Override
	public Executable<Optional<PluginInfo>> fromUrl(String url) {
		loadPlugin.setUrlType(UrlType.URL);
		loadPlugin.setPluginUrl(url);
		return this::execute;
	}

	private ListenableFuture<Optional<PluginInfo>> execute() {
		return threadPool.submit(this::executeDialog);
	}

	private Optional<PluginInfo> executeDialog() throws IOException, ExecutionException, InterruptedException {
		try (LoadPluginDialog loadPluginDialog = new LoadPluginDialog()) {
			return loadPluginDialog.send(loadPlugin).get();
		}
	}

	private class LoadPluginDialog extends FcpDialog<Optional<PluginInfo>> {

		private final AtomicBoolean finished = new AtomicBoolean();
		private final AtomicReference<PluginInfo> pluginInfo = new AtomicReference<>();

		public LoadPluginDialog() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return finished.get();
		}

		@Override
		protected Optional<PluginInfo> getResult() {
			return Optional.ofNullable(pluginInfo.get());
		}

		@Override
		protected void consumePluginInfo(PluginInfo pluginInfo) {
			this.pluginInfo.set(pluginInfo);
			finished.set(true);
		}

		@Override
		protected void consumeProtocolError(ProtocolError protocolError) {
			finished.set(true);
		}

	}

}
