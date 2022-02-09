package net.carrossos.plib.utils.function;

@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
	public T get() throws E;
}
