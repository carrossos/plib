package net.carrossos.plib.data.grid;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelUtils {

	private static final int SECONDS_PER_MINUTE = 60;

	private static final int MINUTES_PER_HOUR = 60;

	private static final int HOURS_PER_DAY = 24;

	private static final int SECONDS_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

	private static final BigDecimal BD_NANOSEC_DAY = BigDecimal.valueOf(SECONDS_PER_DAY * 1e9);

	private static final BigDecimal BD_MILISEC_RND = BigDecimal.valueOf(0.5 * 1e6);

	private static final BigDecimal BD_SECOND_RND = BigDecimal.valueOf(0.5 * 1e9);

	private ExcelUtils() {
	}

	public static String asString(Cell cell) {
		switch (cell.getCellTypeEnum()) {
		case BLANK:
			return null;
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		case ERROR:
			return "#VALUE!";
		case STRING:
		case FORMULA:
			return cell.getStringCellValue();
		case NUMERIC:
			return String.valueOf(cell.getNumericCellValue());
		default:
			throw new IllegalStateException("Unkonwn type: " + cell.getCellTypeEnum());
		}
	}

	public static int fromColumnName(String name) {
		int result = 0;
		int pow = 1;

		name = name.toLowerCase();
		for (int i = name.length() - 1; i >= 0; i--) {
			result += pow * (name.charAt(i) - 'a' + 1);
			pow *= 26;
		}

		return result - 1;
	}

	public static Cell getCell(Row row, int col, boolean create) throws IOException {
		Cell cell = row.getCell(col);

		if (cell != null) {
			return cell;
		} else if (create) {
			return row.createCell(col);
		} else {
			throw newLocatedException(cell, new IOException("Required value not found"));
		}
	}

	public static LocalDateTime getExcelDate(double date) {
		return getExcelDate(date, false, true);
	}

	public static LocalDateTime getExcelDate(double date, boolean use1904windowing, boolean roundSeconds) {
		BigDecimal bd = new BigDecimal(date);

		int wholeDays = bd.intValue();

		int startYear = 1900;
		int dayAdjust = -1; // Excel thinks 2/29/1900 is a valid date, which it isn't
		if (use1904windowing) {
			startYear = 1904;
			dayAdjust = 1; // 1904 date windowing uses 1/2/1904 as the first day
		} else if (wholeDays < 61) {
			// Date is prior to 3/1/1900, so adjust because Excel thinks 2/29/1900 exists
			// If Excel date == 2/29/1900, will become 3/1/1900 in Java representation
			dayAdjust = 0;
		}

		LocalDateTime ldt = LocalDateTime.of(startYear, 1, 1, 0, 0);
		ldt = ldt.plusDays(wholeDays + dayAdjust - 1);

		long nanosTime = bd.subtract(BigDecimal.valueOf(wholeDays)).multiply(BD_NANOSEC_DAY)
				.add(roundSeconds ? BD_SECOND_RND : BD_MILISEC_RND).longValue();

		ldt = ldt.plusNanos(nanosTime);
		ldt = ldt.truncatedTo(roundSeconds ? ChronoUnit.SECONDS : ChronoUnit.MILLIS);

		return ldt;
	}

	public static String getLocationMessage(Cell cell) {
		return getLocationMessage(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex());
	}

	public static String getLocationMessage(Row row, int col) {
		return getLocationMessage(row.getSheet().getSheetName(), row.getRowNum(), col);
	}

	public static String getLocationMessage(String sheet, int row) {
		return sheet + "!" + (row + 1);
	}

	public static String getLocationMessage(String sheet, int row, int col) {
		return sheet + "!" + ExcelUtils.toColumnName(col) + (row + 1);
	}

	public static Row getRow(Sheet sheet, int rowIndex, boolean create) throws IOException {
		Row row = sheet.getRow(rowIndex);

		if (row != null) {
			return row;
		} else if (create) {
			return sheet.createRow(rowIndex);
		} else {
			throw newLocatedException(sheet, rowIndex, new IOException("Required row not found"));
		}
	}

	@Deprecated
	public static IOException newLocatedException(Cell cell, Throwable t) {
		return newLocatedException(cell.getRow(), cell.getColumnIndex(), t);
	}

	@Deprecated
	public static IOException newLocatedException(Row row, int col, Throwable t) {
		return new IOException("At " + row.getSheet().getSheetName() + "!" + toColumnName(col) + (row.getRowNum() + 1));
	}

	@Deprecated
	public static IOException newLocatedException(Row row, Throwable t) {
		return newLocatedException(row.getSheet(), row.getRowNum(), t);
	}

	public static IOException newLocatedException(Sheet sheet, int row, Throwable t) {
		return new IOException("At " + sheet.getSheetName() + "!*" + row + 1);
	}

	public static String toColumnName(int index) {
		char[] result = new char[10];
		int i = result.length - 1;

		while (index >= 26) {
			result[i--] = (char) ('A' + index % 26);
			index = index / 26 - 1;
		}
		result[i] = (char) ('A' + index);

		return new String(result, i, result.length - i);
	}
}
