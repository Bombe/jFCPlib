package net.pterodactylus.fcp.quelaton;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.fcp.GetNode;
import net.pterodactylus.fcp.NodeData;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Default {@link GetNodeCommandImpl} implementation based on {@link FcpReplySequence}.
 *
 * @author <a href="mailto:bombe@freenetproject.org">David ‘Bombe’ Roden</a>
 */
public class GetNodeCommandImpl implements GetNodeCommand {

	private final ListeningExecutorService threadPool;
	private final ConnectionSupplier connectionSupplier;
	private final AtomicBoolean giveOpennetRef = new AtomicBoolean(false);
	private final AtomicBoolean includePrivate = new AtomicBoolean(false);
	private final AtomicBoolean includeVolatile = new AtomicBoolean(false);

	public GetNodeCommandImpl(ExecutorService threadPool, ConnectionSupplier connectionSupplier) {
		this.threadPool = MoreExecutors.listeningDecorator(threadPool);
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public GetNodeCommand opennetRef() {
		giveOpennetRef.set(true);
		return this;
	}

	@Override
	public GetNodeCommand includePrivate() {
		includePrivate.set(true);
		return this;
	}

	@Override
	public GetNodeCommand includeVolatile() {
		includeVolatile.set(true);
		return this;
	}

	@Override
	public ListenableFuture<NodeData> execute() {
		return threadPool.submit(this::executeSequence);
	}

	private NodeData executeSequence() throws InterruptedException, ExecutionException, IOException {
		GetNode getNode = new GetNode(new RandomIdentifierGenerator().generate(), giveOpennetRef.get(),
			includePrivate.get(), includeVolatile.get());
		try (GetNodeReplySequence getNodeReplySequence = new GetNodeReplySequence()) {
			return getNodeReplySequence.send(getNode).get();
		}
	}

	private class GetNodeReplySequence extends FcpReplySequence<NodeData> {

		private final AtomicReference<NodeData> nodeData = new AtomicReference<>();

		public GetNodeReplySequence() throws IOException {
			super(threadPool, connectionSupplier.get());
		}

		@Override
		protected boolean isFinished() {
			return nodeData.get() != null;
		}

		@Override
		protected NodeData getResult() {
			return nodeData.get();
		}

		@Override
		protected void consumeNodeData(NodeData nodeData) {
			this.nodeData.set(nodeData);
		}

	}

}
