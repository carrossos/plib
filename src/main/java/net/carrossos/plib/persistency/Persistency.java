package net.carrossos.plib.persistency;

import java.util.HashMap;
import java.util.Map;

import net.carrossos.plib.persistency.binding.ObjectBinding;

public abstract class Persistency {

	private final String name;

	private final Map<Class<?>, ObjectBinding> bindings = new HashMap<>();

	// private final Map<String, ObjectBinding<?>> bindingsPerName = new
	// HashMap<>();

	// private final Map<ObjectReader, Object> objects = new HashMap<>();
	//
	// public void bind(Reference reference, Object instance) throws
	// PersistencyException {
	// // if (objects.put(reference, instance) != null) {
	// // throw new PersistencyException("Object at reference " + reference + "
	// // overriden!");
	// // }
	// }

	// private void registerBinding(ObjectBinding<?> binding) throws
	// PersistencyException {
	// bindingsPerName.put(binding.getName(), binding);
	// if (bindingsPerClass.put(binding.getBoundClass(), binding) != null) {
	// // Only keep binding per class if there is no conflict
	// bindingsPerClass.remove(binding.getBoundClass());
	// }
	// }
	//
	// public void registerClass(Class<?> clazz) {
	// registerClass(clazz, null);
	// }
	//
	// public void registerClass(Class<?> clazz, String name) {
	// ObjectBinding.buildBindings(this, clazz,
	// null).forEach(this::registerBinding);
	// }
	//
	// public <T> ObjectBinding<T> getBinding(String type) throws
	// PersistencyException {
	// @SuppressWarnings("unchecked")
	// ObjectBinding<T> binding = (ObjectBinding<T>) bindingsPerName.get(type);
	//
	// if (binding == null) {
	// throw new PersistencyException("No known binding for " + type);
	// } else {
	// return binding;
	// }
	// }

	public ObjectBinding getBinding(Class<?> boundClass) throws PersistencyException {
		ObjectBinding binding = bindings.get(boundClass);

		if (binding == null) {
			binding = new ObjectBinding(this, boundClass);
			bindings.put(boundClass, binding);
		}

		return binding;
	}

	public String getName() {
		return name;
	}

	public Persistency(String name) {
		this.name = name;
	}
}
