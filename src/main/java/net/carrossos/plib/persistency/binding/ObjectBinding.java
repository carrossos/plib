package net.carrossos.plib.persistency.binding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.carrossos.plib.persistency.BaseReference;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.ParamsMap;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.Reference;
import net.carrossos.plib.persistency.TypeUtils;
import net.carrossos.plib.persistency.config.PersistencyOptions;
import net.carrossos.plib.persistency.config.PersistentField;
import net.carrossos.plib.persistency.config.PersistentObject;
import net.carrossos.plib.persistency.mapper.Mapper;
import net.carrossos.plib.persistency.reader.ObjectReader;

public class ObjectBinding extends Binding {

	private class FieldConfig {

		public Class<?> type;

		public Mapper mapper;

		public Mapper valueMapper;

		public Binding binding;

		public Field field;

		public String name;

		public int options;
	}

	private final Class<?> boundClass;

	private final ParamsMap parameters;

	private final Set<FieldConfig> fields = new HashSet<>();

	private FieldConfig primary;

	private final Set<FieldConfig> alternates = new HashSet<>();

	private final String name;

	private void init() throws PersistencyException {
		for (Field field : boundClass.getDeclaredFields()) {
			field.setAccessible(true);

			PersistentField annotation = getFieldAnnotation(field);

			if (annotation != null) {
				FieldConfig config = new FieldConfig();

				config.name = annotation.value();

				if (Void.class.equals(annotation.type())) {
					config.type = field.getType();
				} else {
					config.type = annotation.type();

					if (!field.getType().isAssignableFrom(config.type)) {
						throw new PersistencyException("Invalid configuration: custom type for field '" + field
								+ "' cannot be assigned to field type");
					}
				}

				if (config.name.length() == 0) {
					config.name = field.getName();
				}

				config.mapper = buildMappers(field.getType(), annotation.mappers());
				config.valueMapper = buildMappers(field.getType(), annotation.valueMappers());
				config.options = annotation.options();

				if (TypeUtils.isPrimitiveOrWrapper(field.getType())
						&& (config.options & PersistencyOptions.REFERENCE) > 0) {
					throw new PersistencyException(
							"Invalid configuration: field '" + field + "' cannot be a reference");
				}

				if ((config.options & PersistencyOptions.PRIMARY) > 0) {
					if (primary != null) {
						throw new PersistencyException(
								field + " cannot be configured as primary field, only one primary field is supported");
					}

					primary = config;
				} else if ((config.options & PersistencyOptions.ALTERNATE) > 0) {
					alternates.add(config);
				}

				config.field = field;
				config.binding = getBinding(config.type, config.options, getCollectionAnnotation(field));

				fields.add(config);
			}
		}
	}

	private void readField(Context context, FieldConfig config, ObjectReader reader, Object instance) {
		if (!reader.isPresent()) {
			if ((config.options & PersistencyOptions.OPTIONAL) == 0) {
				throw new PersistencyException("On " + reader.getLocation() + ", no value read for field '"
						+ config.field + "' with name '" + config.name + "'");
			} else {
				return;
			}
		}

		try {
			if (TypeUtils.isPrimitiveOrWrapper(config.type)) {
				if (config.type.equals(Boolean.TYPE) || config.type.equals(Boolean.class)) {
					config.field.setBoolean(instance, reader.readBoolean());
				} else if (config.type.equals(Character.TYPE) || config.type.equals(Character.class)) {
					throw new PersistencyException("Unsupported type: " + config.type.getName());
				} else if (config.type.equals(Byte.TYPE) || config.type.equals(Byte.class)) {
					throw new PersistencyException("Unsupported type: " + config.type.getName());
				} else if (config.type.equals(Short.TYPE) || config.type.equals(Short.class)) {
					config.field.setShort(instance, TypeUtils.toShort(reader.readInteger()));
				} else if (config.type.equals(Integer.TYPE) || config.type.equals(Integer.class)) {
					config.field.setInt(instance, reader.readInteger());
				} else if (config.type.equals(Long.TYPE) || config.type.equals(Long.class)) {
					config.field.setLong(instance, reader.readLong());
				} else if (config.type.equals(Float.TYPE) || config.type.equals(Float.class)) {
					config.field.setFloat(instance, TypeUtils.toFloat(reader.readDouble()));
				} else if (config.type.equals(Double.TYPE) || config.type.equals(Double.class)) {
					config.field.setDouble(instance, reader.readDouble());
				} else {
					throw new AssertionError();
				}
			} else if (config.type.equals(String.class)) {

				config.field.set(instance, Objects.requireNonNull(reader.readString()));
			} else {
				config.field.set(instance, Objects.requireNonNull(readObject(context, config.type, config.options,
						config.binding, reader, config.field.get(instance))));
			}
		} catch (IllegalArgumentException | IllegalAccessException | PersistencyException e) {
			throw new PersistencyException(
					"On " + reader.getLocation() + ", Failed to read field '" + config.field + "'", e);
		}
	}

	public Class<?> getBoundClass() {
		return boundClass;
	}

	public String getName() {
		return name;
	}

	public ParamsMap getParameters() {
		return parameters;
	}

	@Override
	public Object read(Context context, ObjectReader reader, Object instance) throws PersistencyException {
		Reference reference = reader.getReference();

		if (reference == null) {
			if (primary != null) {
				reference = new BaseReference(primary.valueMapper.bindAndRetrieve(
						primary.mapper.bindAndRetrieve(reader, r -> r.readAttribute(primary.name)),
						ObjectReader::readString));
			}
		}

		if (reference != null) {
			instance = context.readReference(boundClass, reference);
		}

		if (instance == null) {
			try {
				instance = boundClass.getConstructor().newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new PersistencyException("Failed to create object!", e);
			}

			if (reference != null) {
				context.saveReference(boundClass, reference, instance);

				for (FieldConfig config : alternates) {
					if (config.binding == null) {
						context.saveReference(boundClass,
								new BaseReference(config.valueMapper.bindAndRetrieve(
										config.mapper.bindAndRetrieve(reader, r -> r.readAttribute(config.name)),
										ObjectReader::readString)),
								instance);
					} else {
						String[] array = (String[]) readObject(context, config.type, config.options, config.binding,
								config.mapper.bindAndRetrieve(reader, r -> r.readAttribute(config.name)), null);

						for (String value : array) {
							context.saveReference(boundClass, new BaseReference(value), instance);
						}
					}
				}
			}
		}

		for (FieldConfig config : fields) {
			config.mapper.bind(reader);

			try {
				config.valueMapper.bind(config.mapper.readAttribute(config.name));

				readField(context, config, config.valueMapper, instance);
			} finally {
				config.mapper.unbind();
				config.valueMapper.unbind();
			}
		}

		return instance;
	}

	public ObjectBinding(Persistency persistency, Class<?> boundClass) throws PersistencyException {
		super(persistency);

		this.boundClass = boundClass;

		PersistentObject persistentObject = getClassAnnotation(boundClass);

		if (persistentObject == null) {
			throw new PersistencyException(boundClass.getCanonicalName() + " is not persistent");
		}

		this.name = "".equals(persistentObject.value()) ? boundClass.getSimpleName() : persistentObject.value();

		try {
			parameters = new ParamsMap(persistentObject.parameters());
		} catch (PersistencyException e) {
			throw new PersistencyException("Invalid parameters for class " + boundClass.getName(), e);
		}

		init();
	}

}
