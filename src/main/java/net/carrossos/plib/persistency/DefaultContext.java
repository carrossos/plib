package net.carrossos.plib.persistency;

import java.util.HashMap;
import java.util.Map;

public class DefaultContext implements Context {

	private final Map<Class<?>, Map<Reference, Object>> references = new HashMap<>();

	@Override
	public Object readReference(Class<?> type, Reference reference) {
		Map<Reference, Object> typeReference = references.get(type);

		if (typeReference == null) {
			return null;
		} else {
			return typeReference.get(reference);
		}

	}

	@Override
	public void saveReference(Class<?> type, Reference reference, Object instance) {
		Map<Reference, Object> typeReference = references.get(type);

		if (typeReference == null) {
			typeReference = new HashMap<>();
			references.put(type, typeReference);
		}

		typeReference.put(reference, instance);
	}

}
