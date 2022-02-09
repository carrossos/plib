package net.carrossos.plib.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AveragerMatrix<C> {

	private static class AveragerKey<C> {
		private final C category;

		private final Object[] subCategories;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			AveragerKey<?> other = (AveragerKey<?>) obj;
			if (category == null) {
				if (other.category != null) {
					return false;
				}
			} else if (!category.equals(other.category)) {
				return false;
			}
			if (!Arrays.equals(subCategories, other.subCategories)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (category == null ? 0 : category.hashCode());
			result = prime * result + Arrays.hashCode(subCategories);
			return result;
		}

		@Override
		public String toString() {
			return "AveragerKey [category = " + category + ", subCategories = " + Arrays.toString(subCategories) + "]";
		}

		public AveragerKey(C category, Object[] subCategories) {
			this.category = category;
			this.subCategories = subCategories;
		}
	}

	private final Map<AveragerKey<C>, Double> map = new HashMap<>();

	public void clear() {
		map.clear();
	}

	public Set<C> getCategories() {
		return map.keySet().stream().map(c -> c.category).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	public double getRatio(C category, Object... subCategories) {
		return getTotal(category, subCategories) / getTotal(null, subCategories);
	}

	public double getTotal(C category, Object... subCategories) {
		return map.getOrDefault(new AveragerKey<>(category, subCategories), 0d);
	}

	public void put(double value, C category, Object... subCategories) {
		AveragerKey<C> key = new AveragerKey<>(category, subCategories);

		map.merge(key, value, (a, b) -> a + b);

		AveragerKey<C> total = new AveragerKey<>(null, subCategories);

		map.merge(total, value, (a, b) -> a + b);
	}
}
