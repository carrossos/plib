package net.carrossos.plib.data.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.carrossos.plib.utils.NonNull;

public class Table<C, R, V> {

	private final List<C> columnHeaders;

	private final List<R> rowHeaders;

	private final List<List<V>> values;

	public List<C> getColumnHeaders() {
		return columnHeaders;
	}

	public int getColumns() {
		return columnHeaders.size();
	}

	public List<R> getRowHeaders() {
		return rowHeaders;
	}

	public int getRows() {
		return rowHeaders.size();
	}

	public List<List<V>> getValues() {
		return values;
	}

	public Table<R, C, V> transpose() {
		List<List<V>> values = new ArrayList<>(getColumns());

		for (int c = 0; c < getColumns(); c++) {
			values.addAll(new ArrayList<>(Collections.nCopies(getRows(), null)));
		}

		for (int r = 0; r < getRows(); r++) {
			for (int c = 0; c < getColumns(); c++) {
				values.get(c).set(r, this.getValues().get(r).get(c));
			}
		}

		return new Table<>(rowHeaders, columnHeaders, values);
	}

	public Table(List<C> columnHeaders, List<R> rowHeaders, List<List<V>> values) {
		this.columnHeaders = columnHeaders;
		this.rowHeaders = rowHeaders;
		this.values = values;
	}

	private static <C, R, V> V tryGet(Map<C, Map<R, V>> map, List<R> rows, List<C> columns, int r, int c) {
		return NonNull.tryMap(map, m -> m.get(columns.get(c)), m -> m.get(rows.get(r)));
	}

	public static <T, C extends Comparable<? super C>, R extends Comparable<? super R>, V> Table<C, R, V> build(
			Collection<T> collection, Function<T, C> headerMapper, Function<T, Map<R, V>> valueMapper) {
		return build(collection, headerMapper, valueMapper, Comparator.naturalOrder(), Comparator.naturalOrder());
	}

	public static <T, C, R, V> Table<C, R, V> build(Collection<T> collection, Function<T, C> headerMapper,
			Function<T, Map<R, V>> valueMapper, Comparator<C> columnSort, Comparator<R> rowSort) {
		Map<C, Map<R, V>> map = new HashMap<>();

		for (T object : collection) {
			var rowHeader = headerMapper.apply(object);
			var row = map.get(rowHeader);

			if (row == null) {
				row = new HashMap<>();
				map.put(rowHeader, row);
			}

			row.putAll(valueMapper.apply(object));
		}

		List<C> columns = new ArrayList<>(map.keySet());
		columns.sort(columnSort);

		List<R> rows = map.values().stream().map(Map::keySet).flatMap(Set::stream).distinct().sorted(rowSort)
				.collect(Collectors.toList());

		List<List<V>> values = new ArrayList<>(rows.size());

		for (int r = 0; r < rows.size(); r++) {
			List<V> row = new ArrayList<>(columns.size());
			values.add(row);

			for (int c = 0; c < columns.size(); c++) {
				V value = tryGet(map, rows, columns, r, c);

				row.add(value);
			}
		}

		return new Table<>(columns, rows, values);
	}
}
