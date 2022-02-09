package net.carrossos.plib.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
	private CollectionUtils() {
	}

	public static <S, K, R> Collection<R> aggregateOver(List<S> list, Function<S, K> keyMap, Function<S, R> constructor,
			BiConsumer<S, R> aggregate) {
		return aggregateOver(list.stream(), keyMap, constructor, aggregate);
	}

	public static <S, K, R> Collection<R> aggregateOver(Stream<S> stream, Function<S, K> keyMap,
			Function<S, R> constructor, BiConsumer<S, R> aggregate) {
		Map<K, R> map = new LinkedHashMap<>();

		stream.forEach(s -> {
			R r = map.get(keyMap.apply(s));

			if (r == null) {
				r = constructor.apply(s);
				map.put(keyMap.apply(s), r);
			}

			aggregate.accept(s, r);
		});

		return map.values();
	}

	public static <K, T> Map<K, T> asKeyMap(Collection<T> collection, Function<T, K> keyMapper) {
		return asMap(collection, keyMapper, Function.identity());
	}

	public static <K, V, T> Map<K, V> asMap(Collection<T> collection, Function<T, K> keyMapper,
			Function<T, V> valueMapper) {
		return collection.stream().collect(HashMap::new, (m, e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)),
				Map::putAll);
	}

	public static String join(Collection<?> collection, CharSequence separator) {
		return collection.stream().map(String::valueOf).collect(Collectors.joining(separator));
	}

	public static String join(Collection<?> collection, CharSequence separator, CharSequence start) {
		if (collection.isEmpty()) {
			return "";
		}

		return start + collection.stream().map(String::valueOf).collect(Collectors.joining(separator));
	}

	public static String joinWithStart(Collection<?> collection, CharSequence separator) {
		return join(collection, separator, separator);
	}

	public static <K, V> Map<K, V> merge(Map<K, V> a, Map<K, V> b) {
		if (a == null) {
			if (b == null) {
				return null;
			} else {
				return new HashMap<>(b);
			}
		} else {
			if (b == null) {
				return new HashMap<>(a);
			} else {
				Map<K, V> result = new HashMap<>(a);

				result.putAll(b);

				return result;
			}
		}
	}

	public static <T> T mergeFailDuplicate(T a, T b) {
		throw new IllegalArgumentException("Duplicate: " + a + " and " + b);
	}
}
