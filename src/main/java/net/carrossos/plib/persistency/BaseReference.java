package net.carrossos.plib.persistency;

import java.util.Arrays;
import java.util.Objects;

public class BaseReference implements Reference {

	private final String[] reference;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		} else {
			return Arrays.equals(reference, ((BaseReference) obj).reference);
		}
	}

	@Override
	public int hashCode() {
		return 31 + Arrays.hashCode(reference);
	}

	@Override
	public String toString() {
		return "Reference " + Arrays.toString(reference);
	}

	public BaseReference(Object... attributes) {
		Objects.nonNull(attributes);

		this.reference = new String[attributes.length];

		for (int i = 0; i < attributes.length; i++) {
			this.reference[i] = Objects.requireNonNull(String.valueOf(attributes[i]));
		}
	}

	public BaseReference(String... attributes) {
		Objects.requireNonNull(attributes);

		this.reference = attributes;
		for (String attribute : attributes) {
			Objects.requireNonNull(attribute);
		}
	}
}
