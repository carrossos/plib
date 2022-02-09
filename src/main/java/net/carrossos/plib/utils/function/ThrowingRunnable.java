package net.carrossos.plib.utils.function;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
	public void run() throws E;
}
