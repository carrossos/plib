package net.carrossos.plib.persistency.impl.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.SAXException;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class ExcelFileContainer extends Container {

	private final OPCPackage pckg;

	@Override
	public void close() throws IOException {
		pckg.revert();
	}

	@Override
	public <T> Stream<T> read(Context context, Class<T> type, Consumer<Throwable> errorHandler) {
		Stream<T> result = Stream.empty();

		try {
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pckg);
			XSSFReader xssfReader = new XSSFReader(pckg);
			StylesTable styles = xssfReader.getStylesTable();

			XSSFReader.SheetIterator it = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

			while (it.hasNext()) {
				try (InputStream input = it.next()) {
					String sheetName = it.getSheetName();

					try (ExcelStreamContainer container = new ExcelStreamContainer(getPersistency(), this,
							getParameters(), getName() + " (" + sheetName + ")", sheetName, styles, strings, input)) {

						result = Stream.concat(result, container.read(context, type, errorHandler));
					}
				}
			}

			return result;
		} catch (IOException | SAXException | OpenXML4JException e) {
			throw new PersistencyException("I/O error while reading file '" + getName() + "'", e);
		}
	}

	@Override
	public <T> T readObject(Context context, Class<T> type, ObjectReader reference) {
		// TODO Auto-generated method stub
		return null;
	}

	ExcelFileContainer(Persistency persistency, Container parent, ParamsMap parameters, String name, InputStream input)
			throws IOException {
		super(persistency, parent, parameters, name);

		try {
			this.pckg = OPCPackage.open(input);
		} catch (InvalidFormatException e) {
			throw new IOException("Invalid Excel format", e);
		}
	}
}
