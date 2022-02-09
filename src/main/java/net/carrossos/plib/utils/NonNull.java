package net.carrossos.plib.utils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import net.carrossos.plib.utils.function.ThrowingFunction;

public class NonNull {
	private NonNull() {
	}

	public static <T, R> R elseAndMap(T object, Function<T, R> function, T def) {
		if (object == null) {
			return function.apply(def);
		}

		return function.apply(object);
	}

	public static <T> List<T> elseEmpty(List<T> list) {
		return Objects.requireNonNullElse(list, List.of());
	}

	public static <K, V> Map<K, V> elseEmpty(Map<K, V> map) {
		return Objects.requireNonNullElse(map, Map.of());
	}

	public static <T> Set<T> elseEmpty(Set<T> set) {
		return Objects.requireNonNullElse(set, Set.of());
	}

	public static <T, R> R mapOrElse(T object, Function<T, R> function, R def) {
		if (object == null) {
			return def;
		}

		return function.apply(object);
	}

	public static <T, I, R> R tryMap(T object, Function<T, I> intermediatefunction, Function<I, R> function) {
		return tryMap(tryMap(object, intermediatefunction), function);
	}

	public static <T, R> R tryMap(T object, Function<T, R> function) {
		if (object == null) {
			return null;
		}

		return function.apply(object);
	}
	
	public static <T, R, E extends Exception> R tryMapThrowing(T object, ThrowingFunction<T, R, E> function) throws E {
		if (object == null) {
			return null;
		}

		return function.apply(object);
	}

	public static <T> List<T> unmodifiableElseEmpty(List<T> list) {
		return mapOrElse(list, Collections::unmodifiableList, List.of());
	}

	public static <K, V> Map<K, V> unmodifiableElseEmpty(Map<K, V> map) {
		return mapOrElse(map, Collections::unmodifiableMap, Map.of());
	}

	public static <T> Set<T> unmodifiableElseEmpty(Set<T> set) {
		return mapOrElse(set, Collections::unmodifiableSet, Set.of());
	}
}
