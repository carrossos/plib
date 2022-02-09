package net.carrossos.plib.lang.groovy;

import java.util.function.BiConsumer;

import groovy.lang.Closure;

public class ClosureBiConsumer<T, U> implements BiConsumer<T, U> {

	private final Closure<Void> closure;

	@Override
	public void accept(T t, U u) {
		closure.call(t, u);
	}

	private ClosureBiConsumer(Closure<Void> closure) {
		this.closure = closure;
	}

	public static <T, U> ClosureBiConsumer<T, U> wrap(Closure<Void> closure) {
		return new ClosureBiConsumer<>(closure);
	}
}
