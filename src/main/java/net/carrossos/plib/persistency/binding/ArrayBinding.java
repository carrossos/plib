package net.carrossos.plib.persistency.binding;

import java.lang.reflect.Array;

import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.TypeUtils;
import net.carrossos.plib.persistency.config.PersistencyOptions;
import net.carrossos.plib.persistency.mapper.Mapper;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class ArrayBinding extends Binding {

	private final Class<?> valueType;

	private final Mapper mapper;

	private final Mapper valueMapper;

	private final Binding valueBinding;

	private final int options;

	private void readIndex(Context context, Object array, int index, ObjectReader reader) {
		if (!reader.isPresent()) {
			if ((options & PersistencyOptions.OPTIONAL) == 0) {
				throw new PersistencyException(
						"On " + reader.getLocation() + ", no value read for index '" + index + "'");
			} else {
				return;
			}
		}

		if (TypeUtils.isPrimitiveOrWrapper(valueType)) {
			if (valueType.equals(Boolean.TYPE) || valueType.equals(Boolean.class)) {
				Array.setBoolean(array, index, reader.readBoolean());
			} else if (valueType.equals(Character.TYPE) || valueType.equals(Character.class)) {
				throw new PersistencyException("Unsupported type: " + valueType.getName());
			} else if (valueType.equals(Byte.TYPE) || valueType.equals(Byte.class)) {
				throw new PersistencyException("Unsupported type: " + valueType.getName());
			} else if (valueType.equals(Short.TYPE) || valueType.equals(Short.class)) {
				Array.setShort(array, index, TypeUtils.toShort(reader.readInteger()));
			} else if (valueType.equals(Integer.TYPE) || valueType.equals(Integer.class)) {
				Array.setInt(array, index, reader.readInteger());
			} else if (valueType.equals(Long.TYPE) || valueType.equals(Long.class)) {
				Array.setLong(array, index, reader.readLong());
			} else if (valueType.equals(Float.TYPE) || valueType.equals(Float.class)) {
				Array.setFloat(array, index, TypeUtils.toFloat(reader.readDouble()));
			} else if (valueType.equals(Double.TYPE) || valueType.equals(Double.class)) {
				Array.setDouble(array, index, reader.readDouble());
			} else {
				throw new AssertionError();
			}
		} else if (valueType.equals(String.class)) {
			Array.set(array, index, reader.readString());
		} else {
			Array.set(array, index,
					readObject(context, valueType, options, valueBinding, reader, Array.get(array, index)));
		}
	}

	private Object validate(Object instance, int length) {
		if (instance == null) {
			instance = Array.newInstance(valueType, length);
		} else if (Array.getLength(instance) != length) {
			throw new PersistencyException("Length being read (" + length + ") is incompatible with existing instance ("
					+ Array.getLength(instance) + ")");
		}

		return instance;
	}

	@Override
	public Object read(Context context, ObjectReader reader, Object instance) throws PersistencyException {
		mapper.bind(reader);

		try {
			int length = mapper.getLength();
			instance = validate(instance, length);

			for (int i = 0; i < length; i++) {
				valueMapper.bind(mapper.readValue(i));

				readIndex(context, instance, i, valueMapper);
			}
		} finally {
			mapper.unbind();
			valueMapper.unbind();
		}

		return instance;
	}

	public ArrayBinding(Persistency persistency, Mapper mapper, Class<?> valueType, Mapper valueMapper, int options) {
		super(persistency);

		this.mapper = mapper;
		this.valueType = valueType;
		this.valueMapper = valueMapper;
		this.valueBinding = getBinding(valueType, 0, null);
		this.options = options;
	}

}
