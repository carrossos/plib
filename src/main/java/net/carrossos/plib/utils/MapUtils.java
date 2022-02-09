package net.carrossos.plib.utils;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class MapUtils {
	private MapUtils() {

	}

	public static <K> double getAverage(Map<K, Double> map) {
		return map.values().stream().filter(Objects::nonNull).collect(Collectors.averagingDouble(Double::doubleValue));
	}

	public static <K> double getAverage(SortedMap<K, Double> map, K fromInc, K toExc) {
		return getAverage(map.subMap(fromInc, toExc));
	}
}
