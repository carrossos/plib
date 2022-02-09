package net.carrossos.plib.persistency.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.carrossos.plib.net.CIDRv4;
import net.carrossos.plib.net.IPv4;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.reader.ObjectReader;
import net.carrossos.plib.persistency.reader.StringReader;

public class MapperFactory {

	public static class ArraySplitMapper extends DefaultMapper {

		private final String separator;

		private final StringReader reader;

		private String[] array;

		private void build() {
			if (array == null) {
				if (super.isPresent()) {
					array = readString().split(separator);
				} else {
					array = new String[0];
				}
			}
		}

		@Override
		public void bind(ObjectReader reader) {
			super.bind(reader);

			array = null;
		}

		@Override
		public int getLength() {
			build();

			return array.length;
		}

		@Override
		public boolean isPresent() {
			return true;
		}

		@Override
		public ObjectReader readValue(int index) {
			reader.accept(array[index]);

			return reader;
		}

		public ArraySplitMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.separator = parameters.key("split").get();
			this.reader = new StringReader();
		}

	}

	public static class BooleanMapper extends DefaultMapper {

		private final boolean ignoreCase;

		private final String trueString;

		@Override
		public boolean readBoolean() {
			String value = readString();

			if (ignoreCase) {
				return trueString.equalsIgnoreCase(value);
			} else {
				return trueString.equals(value);
			}

		}

		public BooleanMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.trueString = parameters.key("true").get();
			this.ignoreCase = parameters.key("ignorecase").orElse("true").getBool();
		}
	}

	public static class DateMapper extends DefaultMapper {

		private final DateTimeFormatter format;

		@Override
		// XXX: why this is needed?
		public LocalDateTime readLocalDate() {
			try {
				return format.parse(readString(), LocalDateTime::from);
			} catch (DateTimeParseException e) {
				throw new PersistencyException("Invalid format", e);
			}
		}

		@Override
		public Object readObject(Class<?> type) {
			return readLocalDate();
		}

		public DateMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.format = DateTimeFormatter.ofPattern(parameters.key("format").get());
		}
	}

	public static class LocalDateMapper extends DefaultMapper {

		private final DateTimeFormatter format;

		@Override
		public Object readObject(Class<?> type) {
			return format.parse(readString(), LocalDate::from);
		}

		public LocalDateMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.format = DateTimeFormatter.ofPattern(parameters.key("format").get());
		}
	}

	public static class DefaultValueMapper extends DefaultMapper {

		private final String defaultValue;

		@Override
		public boolean isPresent() {
			return true;
		}

		@Override
		public String readString() {
			if (super.isPresent()) {
				return super.readString();
			} else {
				return defaultValue;
			}
		}

		public DefaultValueMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.defaultValue = parameters.key("default").get();
		}
	}

	public static class EnumStringMapper extends DefaultMapper {

		private Method method = null;

		@Override
		public Object readObject(Class<?> type) {
			if (method == null) {
				try {
					method = type.getMethod("valueOf", String.class);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new PersistencyException("Failed to find factory method for enum", e);
				}
			}

			try {
				return method.invoke(null, readString().toUpperCase());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new PersistencyException("Failed to create enum", e);
			}
		}

		public EnumStringMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);
		}

	}

	public static class OptionalMapper extends DefaultMapper {

		@Override
		public boolean isPresent() {
			return true;
		}

		@Override
		public Object readObject(Class<?> type) {
			if (type.equals(OptionalDouble.class)) {
				if (super.isPresent()) {
					return OptionalDouble.of(readDouble());
				} else {
					return OptionalDouble.empty();
				}
			} else if (type.equals(OptionalInt.class)) {
				if (super.isPresent()) {
					return OptionalInt.of(readInteger());
				} else {
					return OptionalInt.empty();
				}
			} else if (type.equals(OptionalLong.class)) {
				if (super.isPresent()) {
					return OptionalLong.of(readLong());
				} else {
					return OptionalLong.empty();
				}
			} else {
				throw new PersistencyException("Unsupported type: " + type.getName());
			}
		}

		public OptionalMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);
		}

	}

	public static class StringCleanerMapper extends DefaultMapper {

		private final Pattern clean;

		@Override
		public String readString() {
			return clean.matcher(super.readString()).replaceAll("");
		}

		public StringCleanerMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.clean = Pattern.compile("[" + parameters.key("clean").get() + "]+");
		}
	}

	public static class RegExMapper extends StringToNumberMapper {

		private final Pattern pattern;

		@Override
		public String readString() {
			String value = super.readString();
			Matcher m = pattern.matcher(value);

			if (m.find()) {
				return m.group(1);
			} else {
				throw new PersistencyException("Failed to parse: " + value);
			}

		}

		public RegExMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.pattern = Pattern.compile(parameters.key("pattern").get(), Pattern.MULTILINE);
		}

	}

	public static class StringToNumberMapper extends DefaultMapper {

		@Override
		public double readDouble() {
			return Double.parseDouble(readString());
		}

		@Override
		public int readInteger() {
			return Integer.parseInt(readString());
		}

		@Override
		public long readLong() {
			return Long.parseLong(readString());
		}

		public StringToNumberMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);
		}

	}

	public static class IPMapper extends DefaultMapper {

		@Override
		public Object readObject(Class<?> type) {
			if (type.equals(IPv4.class)) {
				return IPv4.fromString(readString());
			} else if (type.equals(CIDRv4.class)) {
				return CIDRv4.fromString(readString());
			} else {
				throw new IllegalArgumentException("Unsupported IP class: " + type.getSimpleName());
			}
		}

		public IPMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);
		}

	}

	public static class YearMonthMapper extends DefaultMapper {

		private final DateTimeFormatter format;

		private final boolean parse;

		@Override
		public Object readObject(Class<?> type) {
			if (parse) {
				var value = super.readString();
				
				try {			
					return YearMonth.parse(value, format);
				} catch (DateTimeParseException e) {
					throw new PersistencyException("Invalid format: " + value, e);
				}
			} else {
				try {
					return YearMonth.from(super.readLocalDate());
				} catch (PersistencyException e) {
					var value = readString().replaceAll("\\s", "");

					try {
						return YearMonth.parse(value, format);
					} catch (DateTimeParseException e2) {
						var throwing = new PersistencyException("Invalid format '" + value + "'", e2);
						throwing.addSuppressed(e);

						throw throwing;
					}
				}
			}
		}

		public YearMonthMapper(Mapper root, ObjectReader next, String[] params) {
			super(root, next, params);

			this.parse = parameters.key("parse").orElse("false").getBool();
			this.format = new DateTimeFormatterBuilder().parseLenient().parseCaseInsensitive()
					.appendPattern(parameters.key("format").get()).toFormatter();
		}

	}

	private final Map<Class<?>, Class<? extends DefaultMapper>> defaults = new HashMap<>();

	public Class<? extends DefaultMapper> getDefault(Class<?> clazz) {
		if (clazz.isEnum()) {
			return EnumStringMapper.class;
		}

		return defaults.get(clazz);
	}

	public MapperFactory() {
		defaults.put(Date.class, DateMapper.class);
		defaults.put(YearMonth.class, YearMonthMapper.class);
		defaults.put(OptionalLong.class, OptionalMapper.class);
		defaults.put(OptionalInt.class, OptionalMapper.class);
		defaults.put(OptionalDouble.class, OptionalMapper.class);
		defaults.put(IPv4.class, IPMapper.class);
		defaults.put(CIDRv4.class, IPMapper.class);
	}

}
