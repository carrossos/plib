package net.carrossos.plib.utils.concurrent;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.carrossos.plib.utils.function.ThrowingRunnable;

public class Retryer {

	private static final Logger LOGGER = LogManager.getLogger(Retryer.class);

	private static final double EXP_COEF = 0.2;

	private static final int EXP_MAX = 10;

	private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1,
			new PoolFactory("retryer", true));

	private static void runRetrying(ThrowingRunnable<?> runnable, int retry, int max, long base, String message) {
		long delay = retry == 0 ? 0 : calculateDelay(base, retry - 1);

		SCHEDULER.schedule(() -> {
			try {
				runnable.run();
			} catch (Throwable t) {
				if (retry == max) {
					LOGGER.error("Unhandled exception when trying to " + message + ". Giving up after " + retry
							+ " attempts", t);
				} else {
					LOGGER.warn("Unhandled exception when trying to " + message + ". Trying again (attempt #"
							+ (retry + 1) + ") in " + Duration.ofMillis(calculateDelay(base, retry)) + "...", t);

					runRetrying(runnable, retry + 1, max, base, message);
				}
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	static long calculateDelay(long base, int failures) {
		return Math.round(base * Math.exp(EXP_COEF * Math.min(failures, EXP_MAX)));
	}

	public static void runRetrying(ThrowingRunnable<?> runnable, int retries, Duration delay, String msg) {
		runRetrying(runnable, 0, retries, delay.toMillis(), msg);
	}
}
