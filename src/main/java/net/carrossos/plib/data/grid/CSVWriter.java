package net.carrossos.plib.data.grid;

import java.io.Closeable;
import java.io.PrintWriter;
import java.util.Date;

public class CSVWriter implements CellSelector, Closeable {

	private int col = 0;

	private int row = 1;

	private final PrintWriter output;

	private final char separator;

	private void next() {
		col++;

		if (col > 1) {
			output.print(separator);
		}
	}

	@Override
	public CellSelector append(Object value) {
		return write(value);
	}

	@Override
	public CellSelector bottom() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		output.close();
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public CellSelector move(String ref) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CellSelector nextRow() {
		output.print("\n");

		col = 0;
		row++;

		return this;
	}

	@Override
	public double readDouble() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int readInteger() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CellSelector right(int length) {
		for (int i = 1; i <= length; i++) {
			next();
		}

		return this;
	}

	@Override
	public CellSelector up() {
		throw new UnsupportedOperationException();
	}

	@Override
	public CellSelector write(Date value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CellSelector write(double value) {
		next();
		output.print(value);

		return this;
	}

	@Override
	public CellSelector write(int value) {
		next();
		output.print(value);

		return this;
	}

	@Override
	public CellSelector write(Object value) {
		next();

		if (value != null) {
			String str = String.valueOf(value);

			if (str.indexOf(separator) >= 0 || str.indexOf('"') == 0 || str.indexOf("\n") >= 0 || str.indexOf("\r") >= 0) {
				output.print('"');
				output.print(str.replaceAll("[\"]", "\"\""));
				output.print('"');
			} else {
				output.print(value);
			}
		}

		return this;
	}

	public CSVWriter(char separator, PrintWriter output) {
		super();
		this.separator = separator;
		this.output = output;
	}

}
