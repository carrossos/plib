package net.carrossos.plib.data.grid;

import java.util.Date;

public interface CellSelector {
	public CellSelector append(Object value);

	public CellSelector bottom();

//	public C cell();

	public boolean exists();

	public int getCol();

	public int getRow();

	public CellSelector move(int col, int row);

	public CellSelector move(String ref);

	public CellSelector nextRow();

	public double readDouble();

	public int readInteger();

	public String readString();

	public default CellSelector right() {
		return right(1);
	}

	public CellSelector right(int length);

	public CellSelector up();

	public CellSelector write(Date value);

	public CellSelector write(double value);

	public CellSelector write(int value);

	public CellSelector write(Object value);

	public default <T> CellSelector writeRow(Iterable<T> it) {
		for (T t : it) {
			write(t);
		}

		return this;
	}

}
