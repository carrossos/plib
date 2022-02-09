package net.carrossos.plib.persistency.impl.excel;

import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Table;

import net.carrossos.plib.persistency.PersistencyException;

public class TableSpliterator implements Spliterator<RowReader> {

	private final Sheet sheet;

	private final Table table;

	private int index;

	private int maptoColumn(String name) {
		return table.findColumnIndex(name) + table.getStartColIndex();
	}

	@Override
	public int characteristics() {
		return DISTINCT & ORDERED & IMMUTABLE & SIZED;
	}

	@Override
	public long estimateSize() {
		return table.getTotalsRowCount();
	}

	@Override
	public boolean tryAdvance(Consumer<? super RowReader> action) {
		if (index > table.getEndRowIndex()) {
			return false;
		} else {
			action.accept(new RowReader(sheet.getRow(index), this::maptoColumn));
			index++;

			return true;
		}
	}

	@Override
	public Spliterator<RowReader> trySplit() {
		return null;
	}

	public TableSpliterator(Sheet sheet, Table table) {
		this.sheet = sheet;
		this.table = table;
		this.index = table.getStartRowIndex() + table.getHeaderRowCount();

		if (table.getHeaderRowCount() != 1) {
			throw new PersistencyException("Cannot read from table '" + table.getName() + "' without headers");
		}
	}

}
