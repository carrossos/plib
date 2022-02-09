package net.carrossos.plib.persistency.impl.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;

public class CSVPersistency extends Persistency {

	private final ParamsMap map = new ParamsMap();

	public Container open(File file) throws IOException {
		return new CSVContainer(this, null, map, file.getAbsolutePath(), new FileInputStream(file));
	}

	public Container open(String name, InputStream input) throws IOException {
		return new CSVContainer(this, null, map, name, input);
	}

	public void setSeparator(String separator) {
		map.add("separator", separator);
	}

	public CSVPersistency(String name) {
		super(name);
	}

}
