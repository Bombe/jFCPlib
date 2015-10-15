package net.pterodactylus.fcp;

/**
 * Progress information about a request.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class RequestProgress {

	private final int total;
	private final int required;
	private final int failed;
	private final int fatallyFailed;
	private final long lastProgress;
	private final int succeeded;
	private final boolean finalizedTotal;
	private final int minSuccessFetchBlocks;

	public RequestProgress(int total, int required, int failed, int fatallyFailed, long lastProgress, int succeeded,
		boolean finalizedTotal, int minSuccessFetchBlocks) {
		this.total = total;
		this.required = required;
		this.failed = failed;
		this.fatallyFailed = fatallyFailed;
		this.lastProgress = lastProgress;
		this.succeeded = succeeded;
		this.finalizedTotal = finalizedTotal;
		this.minSuccessFetchBlocks = minSuccessFetchBlocks;
	}

	public int getTotal() {
		return total;
	}

	public int getRequired() {
		return required;
	}

	public int getFailed() {
		return failed;
	}

	public int getFatallyFailed() {
		return fatallyFailed;
	}

	public long getLastProgress() {
		return lastProgress;
	}

	public int getSucceeded() {
		return succeeded;
	}

	public boolean isFinalizedTotal() {
		return finalizedTotal;
	}

	public int getMinSuccessFetchBlocks() {
		return minSuccessFetchBlocks;
	}

}
