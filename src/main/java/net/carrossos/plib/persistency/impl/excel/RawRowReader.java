package net.carrossos.plib.persistency.impl.excel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.DateUtil;

import net.carrossos.plib.data.grid.ExcelUtils;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.reader.DefaultReader;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class RawRowReader extends DefaultReader {

	private class AttributeReader extends DefaultReader {

		private String attribute;

		private String value;

		@Override
		public String getLocation() {
			return ExcelUtils.getLocationMessage(sheet, row) + " (" + attribute + ")";
		}

		@Override
		public boolean isPresent() {
			return value != null;
		}

		public void move(String attribute, String value) {
			this.attribute = attribute;

			if (value != null && value.isEmpty()) {
				this.value = null;
			} else {
				this.value = value;
			}
		}

		@Override
		public boolean readBoolean() {
			switch (value) {
			case "0":
				return false;
			case "1":
				return true;
			default:
				throw new PersistencyException("Invalid boolean: " + value);
			}
		}

		@Override
		public double readDouble() {
			if ("- 0".equals(value)) {
				return 0;
			}

			if ("-".equals(value.trim())) {
				return 0;
			}

			try {
				return numberFormat.parse(value).doubleValue();
			} catch (ParseException e) {
				throw new PersistencyException("Failed to parse number  '" + value + "'");
			}
		}

		@Override
		public LocalDateTime readLocalDate() {
			return LocalDateTime.ofInstant(DateUtil.getJavaCalendar(readDouble()).toInstant(), ZoneId.systemDefault());
		}

		@Override
		public String readString() {
			return value;
		}
	}

	private final NumberFormat numberFormat;

	private final int row;

	private final Map<String, String> data = new HashMap<>();

	private final String sheet;

	private final AttributeReader reader = new AttributeReader();

	public void addContent(String attribute, String value) {
		data.put(attribute.toUpperCase(), value);
	}

	@Override
	public String getLocation() {
		return ExcelUtils.getLocationMessage(sheet, row);
	}

	@Override
	public boolean isPresent() {
		return true;
	}

	@Override
	public ObjectReader readAttribute(String attribute) {
		reader.move(attribute, data.get(attribute.toUpperCase()));

		return reader;
	}

	public RawRowReader(NumberFormat numberFormat, String sheet, int row) {
		this.numberFormat = numberFormat;
		this.sheet = sheet;
		this.row = row;
	}

}
