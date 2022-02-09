package net.carrossos.plib.persistency.impl.excel;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.binding.ObjectBinding;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class ExcelStreamContainer extends Container {

	private class ReferenceSpliterator implements Spliterator<RawRowReader> {

		@Override
		public int characteristics() {
			return DISTINCT & ORDERED & IMMUTABLE;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public boolean tryAdvance(Consumer<? super RawRowReader> action) {
			if (parseThread == null) {
				parseThread = new Thread(ExcelStreamContainer.this::parse,
						"excel-parsing(" + System.identityHashCode(this) + ")");
				parseThread.setDaemon(true);
				parseThread.start();
			}

			try {
				while (true) {
					RawRowReader row = rows.poll(500, TimeUnit.MILLISECONDS);

					if (row == null) {
						if (!parseThread.isAlive()) {
							if (parseException == null) {
								return false;
							} else {
								throw new PersistencyException("Error occurred while parsing", parseException);
							}
						}
					} else {
						action.accept(row);
						return true;
					}
				}
			} catch (InterruptedException e) {
				return false;
			}
		}

		@Override
		public Spliterator<RawRowReader> trySplit() {
			return null;
		}
	}

	private class SheetParser implements SheetContentsHandler {

		private final Map<Integer, String> schema = new HashMap<>();

		private final int headerRow;

		private RawRowReader current = null;

		private int row;

		@Override
		public void cell(String cellReference, String formattedValue, XSSFComment comment) {
			int col = new CellReference(cellReference).getCol();

			if (current == null) {
				if (row == headerRow) {
					schema.put(col, formattedValue.trim());
				}
			} else {
				String attribute = schema.get(col);

				if (attribute != null) {
					current.addContent(attribute, formattedValue);
				}
			}
		}

		@Override
		public void endRow(int rowNum) {
		}

		@Override
		public void headerFooter(String text, boolean isHeader, String tagName) {
		}

		@Override
		public void startRow(int rowNum) {
			if (current != null) {
				try {
					rows.put(current);
				} catch (InterruptedException e) {
				}
			}

			row = rowNum;

			if (rowNum > headerRow) {
				current = new RawRowReader(numberFormat, sheet, rowNum);
			}
		}

		public SheetParser(int headerRow) {
			this.headerRow = headerRow - 1;
		}

	}

	private final LinkedBlockingQueue<RawRowReader> rows = new LinkedBlockingQueue<>(250);

	private final String sheet;

	private final NumberFormat numberFormat;

	private Thread parseThread;

	private final StylesTable styles;

	private final ReadOnlySharedStringsTable strings;

	private final InputStream input;

	private PersistencyException parseException;

	private XMLReader sheetParser;

	private void parse() {
		InputSource sheetSource = new InputSource(input);

		try {
			sheetParser.parse(sheetSource);
		} catch (SAXException | IOException e) {
			parseException = new PersistencyException("I/O fatal error", e);
		} catch (RuntimeException e) {
			parseException = new PersistencyException("Unhandled parsing error", e);
		}
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public <T> Stream<T> read(Context context, Class<T> type, Consumer<Throwable> errorHandler) {
		ObjectBinding binding = getPersistency().getBinding(type);

		try {
			sheetParser = SAXHelper.newXMLReader();
			XSSFSheetXMLHandler handler = new XSSFSheetXMLHandler(styles, null, strings,
					new SheetParser(getParameters().key("header").orElse(binding.getParameters()).orElse("1").getInt()),
					new DataFormatter(), false);
			sheetParser.setContentHandler(handler);
		} catch (ParserConfigurationException | SAXException e) {
			parseException = new PersistencyException("XML parsing fatal error", e);
		}

		return mapToInstance(StreamSupport.stream(new ReferenceSpliterator(), false), context, type, errorHandler);
	}

	@Override
	public <T> T readObject(Context context, Class<T> type, ObjectReader reference) {
		// TODO Auto-generated method stub
		return null;
	}

	public ExcelStreamContainer(Persistency persistency, Container parent, ParamsMap parameters, String name,
			String sheet, StylesTable styles, ReadOnlySharedStringsTable strings, InputStream input)
			throws IOException {
		super(persistency, parent, parameters, name);

		this.sheet = sheet;
		this.styles = styles;
		this.strings = strings;
		this.input = input;
		this.numberFormat = NumberFormat.getNumberInstance(
				Locale.forLanguageTag(getParameters().key("locale").orElse(Locale.getDefault().toLanguageTag()).get()));
	}

}
