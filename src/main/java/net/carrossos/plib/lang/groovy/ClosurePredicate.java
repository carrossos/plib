package net.carrossos.plib.lang.groovy;

import java.util.function.Predicate;

import groovy.lang.Closure;

public class ClosurePredicate<T> implements Predicate<T> {

	private final Closure<Boolean> closure;

	@Override
	public boolean test(T t) {
		return closure.call(t);
	}

	private ClosurePredicate(Closure<Boolean> closure) {
		this.closure = closure;
	}

	public static <T> ClosurePredicate<T> wrap(Closure<Boolean> closure) {
		return new ClosurePredicate<>(closure);
	}
}
