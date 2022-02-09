package net.carrossos.plib.utils.concurrent;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {
	private static final int EXECUTOR_CLOSE_TIMEOUT = 2;

	private ExecutorUtils() {
	}

	public static void schedule(ScheduledExecutorService executor, Runnable command, Duration duration) {
		executor.schedule(command, duration.toMillis(), TimeUnit.MILLISECONDS);
	}

	public static void scheduleAtFixedRate(ScheduledExecutorService executor, Runnable command, Duration interval) {
		executor.scheduleAtFixedRate(command, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	public static void scheduleAtFixedRate(ScheduledExecutorService executor, Runnable command, Duration initial,
			Duration interval) {
		executor.scheduleAtFixedRate(command, initial.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	public static void shutdownAndWait(ExecutorService executor) {
		executor.shutdown();

		try {
			executor.awaitTermination(EXECUTOR_CLOSE_TIMEOUT, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		}
	}

	public static void shutdownNowAndWait(ExecutorService executor) {
		executor.shutdownNow();

		try {
			executor.awaitTermination(EXECUTOR_CLOSE_TIMEOUT, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
		}
	}
}
