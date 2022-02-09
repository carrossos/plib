package net.carrossos.plib.persistency.binding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import net.carrossos.plib.persistency.BaseReference;
import net.carrossos.plib.persistency.Context;
import net.carrossos.plib.persistency.Persistency;
import net.carrossos.plib.persistency.PersistencyException;
import net.carrossos.plib.persistency.TypeUtils;
import net.carrossos.plib.persistency.config.MapperConfig;
import net.carrossos.plib.persistency.config.PersistencyOptions;
import net.carrossos.plib.persistency.config.PersistentCollection;
import net.carrossos.plib.persistency.config.PersistentField;
import net.carrossos.plib.persistency.config.PersistentObject;
import net.carrossos.plib.persistency.mapper.DefaultMapper;
import net.carrossos.plib.persistency.mapper.Mapper;
import net.carrossos.plib.persistency.mapper.MapperFactory;
import net.carrossos.plib.persistency.reader.ObjectReader;

public abstract class Binding {

	private final Persistency persistency;

	// public Persistency getPersistency() {
	// return persistency;
	// }

	private <A> A getAnnotation(A[] annotations, Function<A, String> sourceFunc) {
		A def = null;

		for (A annotation : annotations) {
			String source = sourceFunc.apply(annotation);

			if ("".equals(source)) {
				def = annotation;
			} else if (persistency.getName().equals(source)) {
				return annotation;
			}
		}

		return def;
	}

	Mapper buildMappers(Class<?> type, MapperConfig[] configs) throws PersistencyException {
		Mapper root = new DefaultMapper();
		Mapper current = root;

		if (configs != null) {
			for (int i = configs.length - 1; i >= 0; i--) {
				try {
					Class<? extends DefaultMapper> clazz = configs[i].mapper();

					if (DefaultMapper.class.equals(clazz)) {
						clazz = new MapperFactory().getDefault(type);
					}

					if (clazz == null) {
						throw new PersistencyException("No default mapper available for " + type);
					}

					current = clazz.getConstructor(Mapper.class, ObjectReader.class, String[].class).newInstance(root,
							current, configs[i].value());
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					throw new PersistencyException("Failed to create mapper!", e);
				}
			}
		}

		return current;
	}

	Binding getBinding(Class<?> type, int options, PersistentCollection collectionAnnotation) {
		if (TypeUtils.isPrimitiveOrWrapper(type)) {
			return null;
		} else if ((options & PersistencyOptions.REFERENCE) > 0) {
			return null;
		} else if ((options & PersistencyOptions.OBJECT) > 0) {
			return persistency.getBinding(type);
		} else if (type.isArray()) {
			Class<?> valueType = collectionAnnotation == null ? Void.class : collectionAnnotation.valueType();

			if (Void.class.equals(valueType)) {
				valueType = type.getComponentType();
			}

			if (collectionAnnotation == null) {
				return new ArrayBinding(persistency, buildMappers(type, null), valueType, buildMappers(type, null), 0);
			} else {
				return new ArrayBinding(persistency, buildMappers(type, collectionAnnotation.mappers()), valueType,
						buildMappers(type, collectionAnnotation.valueMappers()), collectionAnnotation.options());
			}
		} else if (Collection.class.isAssignableFrom(type)) {
			Class<?> valueType = collectionAnnotation == null ? Void.class : collectionAnnotation.valueType();

			if (Void.class.equals(valueType)) {
				throw new PersistencyException(
						"Due to generics type-erasure, value type of a collection must be specified explicitely");
			}

			if (type.isInterface()) {
				if (List.class.isAssignableFrom(type)) {
					type = ArrayList.class;
				} else if (Set.class.isAssignableFrom(type)) {
					type = HashSet.class;
				} else {
					throw new PersistencyException(
							"Unable to infer implementation type automatically for class " + type.getClass().getName());
				}
			}

			return new CollectionBinding(persistency, buildMappers(type, collectionAnnotation.mappers()), type,
					valueType, buildMappers(type, collectionAnnotation.valueMappers()), collectionAnnotation.options());
		} else if (Map.class.isAssignableFrom(type)) {
			// TODO
			throw new PersistencyException();
		} else {
			return null;
		}
	}

	PersistentObject getClassAnnotation(Class<?> type) {
		return getAnnotation(type.getDeclaredAnnotationsByType(PersistentObject.class), PersistentObject::source);
	}

	PersistentCollection getCollectionAnnotation(Field field) {
		return getAnnotation(field.getDeclaredAnnotationsByType(PersistentCollection.class),
				PersistentCollection::source);
	}

	PersistentField getFieldAnnotation(Field field) {
		return getAnnotation(field.getDeclaredAnnotationsByType(PersistentField.class), PersistentField::source);
	}

	Object readObject(Context context, Class<?> type, int options, Binding binding, ObjectReader reader,
			Object instance) {
		if ((options & PersistencyOptions.REFERENCE) > 0) {
			String value = reader.readString();
			Object referedInstance = context.readReference(type, new BaseReference(value));

			if (referedInstance == null) {
				throw new PersistencyException(
						"On " + reader.getLocation() + ", unresolved reference '" + value + "' was encountered");
			}

			return referedInstance;
		} else if ((options & PersistencyOptions.OBJECT) > 0) {
			return persistency.getBinding(type).read(context, reader, instance);
		} else if (type.isArray()) {
			return binding.read(context, reader, instance);
		} else if (Collection.class.isAssignableFrom(type)) {
			return binding.read(context, reader, instance);
		} else if (Map.class.isAssignableFrom(type)) {
			// TODO
			throw new PersistencyException();
		} else if (LocalDateTime.class.isAssignableFrom(type)) {
			// FIXME: should work for all Temporal, using ::from static construct
			return reader.readLocalDate();
		} else {
			return reader.readObject(type);
		}
	}

	public abstract Object read(Context context, ObjectReader reader, Object instance) throws PersistencyException;

	Binding(Persistency persistency) {
		this.persistency = persistency;
	}

}
