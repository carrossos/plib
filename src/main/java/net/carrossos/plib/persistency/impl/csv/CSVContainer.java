package net.carrossos.plib.persistency.impl.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.carrossos.plib.persistency.Container;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class CSVContainer extends Container {

	private static final String MAX_READAHEAD = "10";

	public class CSVSpliterator implements Spliterator<RowReader> {

		private final Consumer<Throwable> errorHandler;

		private int index = 0;

		@Override
		public int characteristics() {
			return DISTINCT & ORDERED & IMMUTABLE;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		private List<String> tryParse() throws IOException {
			String line = reader.readLine();

			if (line == null) {
				return null;
			}

			for (int i = 0; i < maxReadAhead; i++) {
				try {
					return split(separator, line, index);
				} catch (IllegalArgumentException e) {
					// Read ahead
					line += "\n";

					String next = reader.readLine();

					if (next == null) {
						throw new PersistencyException("Imbalanced quotes while reaching end of file");
					}

					line += next;
				}
			}

			throw new IOException(
					"Imbalanced quotes while reading ahead for " + maxReadAhead + " lines: read so far: " + line);
		}

		@Override
		public boolean tryAdvance(Consumer<? super RowReader> action) {
			try {
				List<String> list;

				try {
					list = tryParse();

					if (list == null) {
						return false;
					}
				} catch (PersistencyException e) {
					errorHandler.accept(e);

					list = List.of();
				}

				rowReader.next(list);
				action.accept(rowReader);

				return true;
			} catch (IOException e) {
				throw new PersistencyException("I/O error while reading stream", e);
			}
		}

		@Override
		public Spliterator<RowReader> trySplit() {
			return null;
		}

		public CSVSpliterator(Consumer<Throwable> errorHandler) {
			this.errorHandler = errorHandler;
		}

	}

	private final BufferedReader reader;

	private final char separator;

	private final int maxReadAhead;

	private final Map<String, Integer> schema = new HashMap<>();

	private final RowReader rowReader = new RowReader(this, s -> schema.getOrDefault(s, -1));

	@Override
	public void close() throws IOException {
	}

	@Override
	public <T> Stream<T> read(Context context, Class<T> type, Consumer<Throwable> errorHandler) {
		return mapToInstance(StreamSupport.stream(new CSVSpliterator(errorHandler), false), context, type,
				errorHandler);
	}

	@Override
	public <T> T readObject(Context context, Class<T> type, ObjectReader reference) {
		return null;
	}

	public CSVContainer(Persistency persistency, Container parent, ParamsMap parameters, String name, InputStream input)
			throws IOException {
		super(persistency, parent, parameters, name);

		this.reader = new BufferedReader(new InputStreamReader(input));
		this.separator = parameters.key("separator").get().charAt(0);
		this.maxReadAhead = parameters.key("max_readahead").orElse(MAX_READAHEAD).getInt();

		int i = 0;
		for (String attribute : split(separator, reader.readLine())) {
			schema.put(attribute.toUpperCase(), i++);
		}
	}

	private static List<String> split(char separator, String line, int lineIdx) {
		List<String> list = new ArrayList<>();
		StringBuilder builder = new StringBuilder(line.length());
		boolean quoted = false;
		boolean start = true;
		int i = 0;

		while (i < line.length()) {
			char c = line.charAt(i);

			if (c == separator) {
				if (quoted) {
					start = false;
					builder.append(c);
				} else {
					start = true;
					list.add(builder.toString());
					builder.delete(0, builder.length());
				}
			} else if (c == '"') {
				if (start) {
					quoted = true;
				} else if (quoted) {
					if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
						builder.append('"');
						i++;
					} else if (i + 1 == line.length() || line.charAt(i + 1) == separator) {
						quoted = false;
					} else {
						throw new PersistencyException("Cannot parse '\"' at line " + lineIdx + " and index " + i
								+ " (double quote missing?): " + line);
					}
				} else {
					builder.append(c);
				}

				start = false;
			} else {
				builder.append(c);
				start = false;
			}

			i++;
		}

		if (quoted) {
			throw new IllegalArgumentException("Imbalanced quotes");
		}

		list.add(builder.toString());

		return list;

	}

	static List<String> split(char separator, String line) {
		return split(separator, line, 0);
	}
}
