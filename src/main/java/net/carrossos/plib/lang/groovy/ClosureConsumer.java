package net.carrossos.plib.lang.groovy;

import java.util.function.Consumer;

import groovy.lang.Closure;

public class ClosureConsumer<T> implements Consumer<T> {

	private final Closure<?> closure;

	@Override
	public void accept(T t) {
		closure.call(t);
	}

	private ClosureConsumer(Closure<?> closure) {
		this.closure = closure;
	}

	public static <T> ClosureConsumer<T> wrap(Closure<?> closure) {
		return new ClosureConsumer<>(closure);
	}
}
