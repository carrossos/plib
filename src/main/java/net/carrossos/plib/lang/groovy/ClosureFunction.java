package net.carrossos.plib.lang.groovy;

import java.util.function.Function;

import groovy.lang.Closure;

public class ClosureFunction<T, R> implements Function<T, R> {

	private final Closure<R> closure;

	@Override
	public R apply(T t) {
		return closure.call(t);
	}

	private ClosureFunction(Closure<R> closure) {
		this.closure = closure;
	}

	public static <T, R> ClosureFunction<T, R> wrap(Closure<R> closure) {
		return new ClosureFunction<>(closure);
	}
}
