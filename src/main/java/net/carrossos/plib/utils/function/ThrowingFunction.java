package net.carrossos.plib.utils.function;

@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {
	public R apply(T t) throws E;
}
