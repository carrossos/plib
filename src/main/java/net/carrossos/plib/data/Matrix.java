package net.carrossos.plib.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Matrix<R, C, V> {

	private final Map<R, Map<C, V>> map = new HashMap<>();

	public V get(R row, C col) {
		Map<C, V> rowMap = map.get(row);

		if (rowMap == null) {
			return null;
		} else {
			return rowMap.get(col);
		}
	}

	public Map<R, V> getCol(C col) {
		Map<R, V> colMap = new HashMap<>();

		for (Map.Entry<R, Map<C, V>> entry : map.entrySet()) {
			V value = entry.getValue().get(col);

			if (value != null) {
				colMap.put(entry.getKey(), value);
			}
		}

		return Collections.unmodifiableMap(colMap);
	}

	public Set<C> getCols() {
		return map.values().stream().flatMap(e -> e.keySet().stream()).collect(Collectors.toSet());
	}

	public V getOrElse(R row, C col, V def) {
		V value = get(row, col);

		if (value == null) {
			return def;
		} else {
			return value;
		}
	}

	public Map<C, V> getRow(R row) {
		Map<C, V> rowMap = map.get(row);

		if (rowMap == null) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(rowMap);
		}
	}

	public Set<R> getRows() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public V put(R row, C col, V val) {
		Map<C, V> rowMap = map.get(row);

		if (rowMap == null) {
			rowMap = new HashMap<>();
			map.put(row, rowMap);
		}

		return rowMap.put(col, val);
	}

	@Override
	public String toString() {
		return map.toString();
	}

	public Matrix<C, R, V> transpose() {
		Matrix<C, R, V> matrix = new Matrix<>();

		for (Map.Entry<R, Map<C, V>> row : map.entrySet()) {
			for (Map.Entry<C, V> value : row.getValue().entrySet()) {
				matrix.put(value.getKey(), row.getKey(), value.getValue());
			}
		}

		return matrix;
	}
}
