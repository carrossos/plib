package net.carrossos.plib.persistency.impl.excel;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.binding.ObjectBinding;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class ExcelWorkbookContainer extends Container {

	private final Workbook workbook;

	private final File file;

	private Stream<? extends ObjectReader> loadFromTable(String name) {
		if (workbook instanceof XSSFWorkbook) {
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet sheet = ((XSSFWorkbook) workbook).getSheetAt(i);

				for (XSSFTable table : sheet.getTables()) {
					if (name.equals(table.getName())) {
						return StreamSupport.stream(new TableSpliterator(sheet, table), false);
					}
				}
			}
		}

		throw new PersistencyException("Cannot find table '" + name + "' in " + file);
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public <T> Stream<T> read(Context context, Class<T> type, Consumer<Throwable> errorHandler) {
		ObjectBinding binding = getPersistency().getBinding(type);

		String tableName = binding.getParameters().key("table").get();

		if (tableName != null) {
			return mapToInstance(loadFromTable(tableName), context, type, errorHandler);
		}

		return null;
	}

	@Override
	public <T> T readObject(Context context, Class<T> type, ObjectReader reference) {
		// TODO Auto-generated method stub
		return null;
	}

	public ExcelWorkbookContainer(Persistency persistency, Container parent, ParamsMap parameters, File file)
			throws IOException {
		super(persistency, parent, parameters, file.getAbsolutePath());

		this.file = file;

		try {
			this.workbook = WorkbookFactory.create(file, null, true);
		} catch (EncryptedDocumentException | InvalidFormatException e) {
			throw new IOException("Failed to open workbook", e);
		}
	}
}
