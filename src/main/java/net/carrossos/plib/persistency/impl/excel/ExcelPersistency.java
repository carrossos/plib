package net.carrossos.plib.persistency.impl.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;

public class ExcelPersistency extends Persistency {

	private final ParamsMap map = new ParamsMap();

	public Container open(File file) throws IOException {
		return new ExcelFileContainer(this, null, map, file.getAbsolutePath(), new FileInputStream(file));
	}

	public Container openFull(File file) throws IOException {
		return new ExcelWorkbookContainer(this, null, new ParamsMap(null), file);
	}

	public void setLocale(Locale locale) {
		map.add("locale", locale.toLanguageTag());
	}

	public ExcelPersistency(String name) {
		super(name);
	}

}
