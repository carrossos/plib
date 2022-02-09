package net.carrossos.plib.persistency;

import java.util.HashMap;
import java.util.Map;

public class ParamsMap {

	public static class ValueBuilder {

		private final String key;

		private String value;

		public String get() {
			if (value == null) {
				throw new PersistencyException("Missing mandatory parameter: " + key);
			}

			return value;
		}

		public boolean getBool() {
			try {
				return Boolean.valueOf(get());
			} catch (NumberFormatException e) {
				throw new PersistencyException("Invalid parameter value for " + key, e);
			}
		}

		public int getInt() {
			try {
				return Integer.valueOf(get());
			} catch (NumberFormatException e) {
				throw new PersistencyException("Invalid parameter value for " + key, e);
			}
		}

		public ValueBuilder orElse(ParamsMap params) {
			if (value == null) {
				value = params.map.get(key);
			}

			return this;
		}

		public ValueBuilder orElse(String def) {
			if (value == null) {
				value = def;
			}

			return this;
		}

		public ValueBuilder(String key, String value) {
			this.key = key;
			this.value = value;
		}

	}

	private final Map<String, String> map = new HashMap<>();

	public void add(String key, String value) {
		if (map.put(key, value) != null) {
			throw new PersistencyException("Duplicate key: " + key);
		}
	}

	public ValueBuilder key(String key) {
		return new ValueBuilder(key, map.get(key));
	}

	public ParamsMap() {
		this(null);
	}

	public ParamsMap(String[] array) {
		if (array != null) {
			for (String param : array) {
				String[] split = param.split("=");

				if (split.length != 2) {
					throw new PersistencyException("Invalid parameter " + param);
				} else {
					add(split[0], split[1]);
				}
			}
		}
	}
}