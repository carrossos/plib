package net.carrossos.plib.utils.concurrent;

import java.io.Closeable;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.carrossos.plib.utils.function.ThrowingRunnable;

public class Invoker implements Closeable {

	private static final Logger LOGGER = LogManager.getLogger(Invoker.class);

	private final long interval;

	private final String name;

	private final ThrowingRunnable<?> runnable;

	private final Semaphore semaphore;

	private final ExecutorService executor;

	private final List<CompletableFuture<Void>> completions = new LinkedList<>();

	private int failures;

	private void loop() {
		LOGGER.info("Invoker ready: {}", name);

		try {
			while (!Thread.interrupted()) {
				if (semaphore != null) {
					semaphore.acquire();

					while (semaphore.tryAcquire()) {
						;
					}
				}

				try {
					LOGGER.debug("Running: {} (failures = {})", name, failures);

					runnable.run();
					failures = 0;

					synchronized (this) {
						completions.forEach(c -> c.complete(null));
						completions.clear();
					}
				} catch (Throwable t) {
					if (t instanceof InterruptedException) {
						return;
					}

					failures++;

					LOGGER.warn("Failed to execute '" + name + "' (" + failures + " recent failures). Delay is now: "
							+ Duration.ofMillis(Retryer.calculateDelay(interval, failures)), t);

					synchronized (this) {
						completions.forEach(c -> c.completeExceptionally(t));
						completions.clear();
					}
				}

				Thread.sleep(Retryer.calculateDelay(interval, failures));
			}
		} catch (InterruptedException e) {
		} finally {
			synchronized (this) {
				completions.forEach(c -> c.completeExceptionally(new InterruptedException()));
				completions.clear();
			}

			LOGGER.info("Terminated invoker: {}", name);
		}
	}

	@Override
	public void close() {
		ExecutorUtils.shutdownNowAndWait(executor);
	}

	public Invoker start() {
		executor.execute(this::loop);

		return this;
	}

	public CompletableFuture<Void> trigger() {
		if (semaphore == null) {
			throw new IllegalStateException("Invoker '" + name + "' cannot be triggered!");
		}

		CompletableFuture<Void> future = new CompletableFuture<>();

		synchronized (this) {
			completions.add(future);
		}

		semaphore.release();

		return future;
	}

	private Invoker(String name, long interval, ThrowingRunnable<?> runnable, Semaphore semaphore) {
		this.name = name;
		this.interval = interval;
		this.runnable = runnable;
		this.semaphore = semaphore;
		this.executor = Executors.newSingleThreadExecutor(new PoolFactory(name, false));
	}

	public static Invoker loopEvery(String name, Duration interval, ThrowingRunnable<?> runnable) {
		return new Invoker(name, interval.toMillis(), runnable, null);
	}

	public static Invoker loopEveryConditionally(String name, Duration interval, ThrowingRunnable<?> runnable) {
		return new Invoker(name, interval.toMillis(), runnable, new Semaphore(0));
	}
}
