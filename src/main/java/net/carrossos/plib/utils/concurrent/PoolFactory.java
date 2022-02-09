package net.carrossos.plib.utils.concurrent;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolFactory implements ThreadFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Map<String, AtomicInteger> GLOBAL_COUNTER = new HashMap<>();

	private final String name;

	private final boolean daemon;

	private final AtomicInteger counter = new AtomicInteger(0);

	private void errorHandler(Thread thread, Throwable t) {
		LOGGER.error("Pool '" + name + "' failure! Error caught in thread '" + thread.getName() + "'", t);
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, name + counter.incrementAndGet());
		thread.setDaemon(daemon);
		thread.setUncaughtExceptionHandler(this::errorHandler);

		return thread;
	}

	public PoolFactory(String name, boolean daemon) {
		this.daemon = daemon;

		synchronized (GLOBAL_COUNTER) {
			if (!GLOBAL_COUNTER.containsKey(name)) {
				GLOBAL_COUNTER.put(name, new AtomicInteger(0));
			}

			this.name = name + "-" + GLOBAL_COUNTER.get(name).incrementAndGet() + "-";
		}
	}
}