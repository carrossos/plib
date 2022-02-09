package net.carrossos.plib.persistency.impl.excel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.ToIntFunction;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import net.carrossos.plib.data.grid.ExcelUtils;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.Reference;
import net.carrossos.plib.persistency.reader.DefaultReader;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class RowReader extends DefaultReader {

	private class CellReader extends DefaultReader {

		private int col;

		private Cell cell;

		@Override
		public String getLocation() {
			return ExcelUtils.getLocationMessage(row, col);
		}

		@Override
		public boolean isPresent() {
			return cell != null && cell.getCellTypeEnum() != CellType.BLANK;
		}

		public void move(String attribute) {
			col = columnMapper.applyAsInt(attribute);

			if (col < 0) {
				throw new PersistencyException("Invalid column: " + attribute);
			}

			cell = row.getCell(col);
		}

		@Override
		public boolean readBoolean() {
			return cell.getBooleanCellValue();
		}

		@Override
		public double readDouble() {
			switch (cell.getCellTypeEnum()) {
			case FORMULA:
			case NUMERIC:
				return cell.getNumericCellValue();
			case STRING:
				try {
					return Double.valueOf(cell.getStringCellValue());
				} catch (NumberFormatException e) {
					throw new PersistencyException("Failed to parse number: " + cell.getStringCellValue(), e);
				}
			case BLANK:
			case BOOLEAN:
			case ERROR:
			default:
				throw new PersistencyException("Invalid cell value type: " + cell);
			}
		}

		@Override
		public LocalDateTime readLocalDate() {
			switch (cell.getCellTypeEnum()) {
			case FORMULA:
			case NUMERIC:
				return LocalDateTime.ofInstant(cell.getDateCellValue().toInstant(), ZoneId.systemDefault());
			case STRING:
			case BLANK:
			case BOOLEAN:
			case ERROR:
			default:
				throw new PersistencyException("Invalid cell value type: " + cell);
			}
		}

		@Override
		public String readString() {
			return ExcelUtils.asString(cell);
		}
	}

	private final ToIntFunction<String> columnMapper;

	private final Row row;

	private final CellReader reader = new CellReader();

	@Override
	public String getLocation() {
		return ExcelUtils.getLocationMessage(row.getSheet().getSheetName(), row.getRowNum());
	}

	@Override
	public Reference getReference() {
		return null;
	}

	@Override
	public boolean isPresent() {
		return true;
	}

	@Override
	public ObjectReader readAttribute(String attribute) {
		reader.move(attribute);

		return reader;
	}

	public RowReader(Row row, ToIntFunction<String> columnMapper) {
		this.row = row;
		this.columnMapper = columnMapper;
	}

}
