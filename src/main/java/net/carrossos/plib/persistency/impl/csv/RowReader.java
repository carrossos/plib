package net.carrossos.plib.persistency.impl.csv;

import java.util.List;
import java.util.function.ToIntFunction;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.Reference;
import net.carrossos.plib.persistency.reader.DefaultReader;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class RowReader extends DefaultReader {

	private class ValueReader extends DefaultReader {

		private String attribute;

		private int col;

		@Override
		public String getLocation() {
			return container.getName() + ", at row " + (row + 1) + ", column '" + attribute + "'";
		}

		@Override
		public boolean isPresent() {
			return col >= 0 && col < values.size() && values.get(col) != null && !values.get(col).isEmpty();
		}

		public void move(String attribute) {
			this.attribute = attribute;
			col = schema.applyAsInt(attribute.toUpperCase());
		}

		@Override
		public String readString() {
			return values.get(col);
		}
	}

	private final ToIntFunction<String> schema;

	private final ValueReader reader = new ValueReader();

	private final Container container;

	private List<String> values;

	private long row = 0;

	void next(List<String> values) {
		this.values = values;
		row++;
	}

	@Override
	public String getLocation() {
		return "On row " + row;
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

	public RowReader(Container container, ToIntFunction<String> schema) {
		this.container = container;
		this.schema = schema;
	}

}
