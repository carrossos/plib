package net.carrossos.plib.persistency.binding;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.TypeUtils;
import net.carrossos.plib.persistency.config.PersistencyOptions;
import net.carrossos.plib.persistency.mapper.Mapper;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class CollectionBinding extends Binding {

	private final Class<?> type;

	private final Class<?> valueType;

	private final Mapper mapper;

	private final Mapper valueMapper;

	private final Binding valueBinding;

	private final int options;

	private Object read(Context context, ObjectReader reader) {
		if (!reader.isPresent()) {
			if ((options & PersistencyOptions.OPTIONAL) == 0) {
				throw new PersistencyException("On " + reader.getLocation() + ", no value read in collection");
			} else {
				return null;
			}
		}

		if (TypeUtils.isPrimitiveOrWrapper(valueType)) {
			if (valueType.equals(Boolean.TYPE) || valueType.equals(Boolean.class)) {
				return reader.readBoolean();
			} else if (valueType.equals(Character.TYPE) || valueType.equals(Character.class)) {
				throw new PersistencyException("Unsupported type: " + valueType.getName());
			} else if (valueType.equals(Byte.TYPE) || valueType.equals(Byte.class)) {
				throw new PersistencyException("Unsupported type: " + valueType.getName());
			} else if (valueType.equals(Short.TYPE) || valueType.equals(Short.class)) {
				return TypeUtils.toShort(reader.readInteger());
			} else if (valueType.equals(Integer.TYPE) || valueType.equals(Integer.class)) {
				return reader.readInteger();
			} else if (valueType.equals(Long.TYPE) || valueType.equals(Long.class)) {
				return reader.readLong();
			} else if (valueType.equals(Float.TYPE) || valueType.equals(Float.class)) {
				return TypeUtils.toFloat(reader.readDouble());
			} else if (valueType.equals(Double.TYPE) || valueType.equals(Double.class)) {
				return reader.readDouble();
			} else {
				throw new AssertionError();
			}
		} else if (valueType.equals(String.class)) {
			return reader.readString();
		} else {
			return readObject(context, valueType, options, valueBinding, reader, null);
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> validate(Object instance) {
		if (instance == null) {
			try {
				return (Collection<Object>) type.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new PersistencyException("Failed to create instance", e);
			}
		} else if (!instance.getClass().equals(type)) {
			throw new PersistencyException("Current instance type (" + instance.getClass().getName() + ") is invalid");
		} else {
			return (Collection<Object>) instance;
		}
	}

	@Override
	public Object read(Context context, ObjectReader reader, Object instance) throws PersistencyException {
		mapper.bind(reader);

		try {
			int length = mapper.getLength();
			Collection<Object> collection = validate(instance);

			for (int i = 0; i < length; i++) {
				valueMapper.bind(mapper.readValue(i));

				collection.add(read(context, valueMapper));
			}

			return collection;
		} finally {
			mapper.unbind();
			valueMapper.unbind();
		}
	}

	public CollectionBinding(Persistency persistency, Mapper mapper, Class<?> type, Class<?> valueType,
			Mapper valueMapper, int options) {
		super(persistency);

		this.mapper = mapper;
		this.type = type;
		this.valueType = valueType;
		this.valueMapper = valueMapper;
		this.valueBinding = getBinding(valueType, 0, null);
		this.options = options;
	}

}
