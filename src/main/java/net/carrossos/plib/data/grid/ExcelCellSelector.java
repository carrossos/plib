package net.carrossos.plib.data.grid;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelCellSelector implements CellSelector {

	private static final Pattern REF_PATTERN = Pattern.compile("^([a-z]+)(\\d+)$", Pattern.CASE_INSENSITIVE);

	private final Sheet sheet;

	private int baseCol = -1;

	private int col;

	private int row;

	private Row rowRef;

	private Cell getCell(boolean create) {
		if (rowRef == null || rowRef.getRowNum() != row) {
			rowRef = sheet.getRow(row);

			if (rowRef == null) {
				if (create) {
					rowRef = sheet.createRow(row);
				} else {
					return null;
				}
			}
		}

		Cell cell = rowRef.getCell(col);

		if (cell == null && create) {
			cell = rowRef.createCell(col);
		}

		return cell;
	}

	@Override
	public CellSelector append(Object value) {
		if (value != null) {
			getCell(true).setCellValue(getCell(true).getStringCellValue() + value);
		}

		return this;
	}

	@Override
	public CellSelector bottom() {
		row++;

		return this;
	}

	@Override
	public boolean exists() {
		Cell cell = getCell(false);

		if (cell == null) {
			return false;
		} else {
			return cell.getCellTypeEnum() != CellType.BLANK;
		}
	}

	@Override
	public int getCol() {
		return col;
	}

	@Override
	public int getRow() {
		return row;
	}

	@Override
	public CellSelector move(int col, int row) {
		this.row = row;
		this.col = col;
		this.baseCol = col;

		return this;
	}

	@Override
	public CellSelector move(String ref) {
		Matcher matcher = REF_PATTERN.matcher(ref);

		if (matcher.matches()) {
			return move(ExcelUtils.fromColumnName(matcher.group(1)), Integer.parseInt(matcher.group(2)) - 1);
		} else {
			throw new IllegalArgumentException("Invalid reference: " + ref);
		}
	}

	@Override
	public CellSelector nextRow() {
		if (baseCol == -1) {
			throw new IllegalStateException("Cannot move to next row without moving to a reference cell first");
		}

		return move(baseCol, row + 1);
	}

	@Override
	public double readDouble() {
		double value = getCell(false).getNumericCellValue();

		right();

		return value;
	}

	@Override
	public int readInteger() {
		return (int) readDouble();
	}

	@Override
	public String readString() {
		String value;
		Cell cell = getCell(false);

		value = ExcelUtils.asString(cell);

		right();

		return value;
	}

	@Override
	public CellSelector right(int length) {
		col += length;

		return this;
	}

	@Override
	public CellSelector up() {
		row--;

		return this;
	}

	@Override
	public CellSelector write(Date value) {
		if (value != null) {
			getCell(true).setCellValue(value);
		}

		return right();
	}

	@Override
	public CellSelector write(double value) {
		getCell(true).setCellValue(value);

		return right();
	}

	@Override
	public CellSelector write(int value) {
		getCell(true).setCellValue(value);

		return right();
	}

	@Override
	public CellSelector write(Object value) {
		if (value != null) {
			getCell(true).setCellValue(value.toString());
		}

		return right();
	}

	public ExcelCellSelector(Sheet sheet) {
		this.sheet = sheet;
	}

}
