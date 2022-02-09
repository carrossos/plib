package net.carrossos.plib.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import net.carrossos.plib.data.grid.CellSelector;
import net.carrossos.plib.data.grid.ExcelCellSelector;

public class MatrixReader {

	public static <R, C, V> Matrix<R, C, V> readFromSelector(Matrix<R, C, V> matrix, CellSelector selector,
			Function<String, R> rowMap, Function<String, C> colMap, Function<String, V> valMap) {
		List<C> cols = new ArrayList<>();

		if (selector.exists()) {
			selector.right();

			while (selector.exists()) {
				cols.add(colMap.apply(selector.readString()));
			}

			selector.nextRow();

			while (selector.exists()) {
				R row = rowMap.apply(selector.readString());

				int c = 0;
				while (selector.exists()) {
					matrix.put(row, cols.get(c), valMap.apply(selector.readString()));
					c++;
				}

				selector.nextRow();
			}
		}

		return matrix;
	}

	public static <R, C, V> Matrix<R, C, V> readFromSheet(Matrix<R, C, V> matrix, Workbook workbook, String sheetName,
			String ref, Function<String, R> rowMap, Function<String, C> colMap, Function<String, V> valMap)
			throws IOException {
		Sheet sheet = workbook.getSheet(sheetName);

		if (sheet == null) {
			throw new IOException("Sheet not found: " + sheetName);
		} else {

			CellSelector selector = new ExcelCellSelector(sheet);

			selector.move(ref);

			return readFromSelector(matrix, selector, rowMap, colMap, valMap);
		}
	}
}
