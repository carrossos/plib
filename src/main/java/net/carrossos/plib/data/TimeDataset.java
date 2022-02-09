package net.carrossos.plib.data;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class TimeDataset<K> {

	private final Map<K, OptionalDouble[]> monthlyValues = new HashMap<>();

	private final Map<K, OptionalDouble[]> yearlyValues = new HashMap<>();

	private final int startYear;

	private final int startMonth;

	private final int endYear;

	private final int endMonth;

	private void add0(int year, int month, OptionalDouble[] array, double value) {
		array[year * 12 + month - startYear * 12 - startMonth] = OptionalDouble.of(value);
	}

	private OptionalDouble[] getYearly(K key) {
		OptionalDouble[] yearly = yearlyValues.get(key);

		if (yearly != null) {
			return yearly;
		}

		OptionalDouble[] monthly = monthlyValues.get(key);

		if (monthly == null) {
			return null;
		}

		yearly = new OptionalDouble[endYear - startYear + 1];

		int m = 0;
		int year = startYear;
		int month = startMonth;
		double total = 0;
		int count = 0;

		while (m < monthly.length) {
			if (monthly[m].isPresent()) {
				total += monthly[m].getAsDouble();
				count++;
			}

			month++;
			m++;

			if (month == 13) {
				if (count > 0) {
					yearly[year - startYear] = OptionalDouble.of(total / count);
				} else {
					yearly[year - startYear] = OptionalDouble.empty();
				}

				count = 0;
				total = 0;
				month = 1;
				year++;
			}
		}

		yearlyValues.put(key, yearly);
		return yearly;
	}

	public Set<K> getAttributes() {
		return java.util.Collections.unmodifiableSet(monthlyValues.keySet());
	}

	public long getAvailableValueCount(K key) {
		OptionalDouble[] array = monthlyValues.get(key);

		if (array == null) {
			return 0;
		} else {
			return Stream.of(array).filter(OptionalDouble::isPresent).count();
		}
	}

	public YearMonth getEnd() {
		return YearMonth.of(endYear, endMonth);
	}

	public OptionalDouble getMonthly(K key, int year, int month) {
		if (year < startYear || year == startYear && month < startMonth) {
			return OptionalDouble.empty();
		} else if (year > endYear || year == endYear && month > endMonth) {
			return OptionalDouble.empty();
		}

		OptionalDouble[] array = monthlyValues.get(key);

		if (array == null) {
			return OptionalDouble.empty();
		} else {
			return array[year * 12 + month - startYear * 12 - startMonth];
		}
	}

	public OptionalDouble getMonthly(K key, YearMonth time) {
		return getMonthly(key, time.getYear(), time.getMonthValue());
	}

	public YearMonth getStart() {
		return YearMonth.of(startYear, startMonth);
	}

	public OptionalDouble getYearly(K key, int year) {
		if (year < startYear) {
			return OptionalDouble.empty();
		}

		OptionalDouble[] array = getYearly(key);

		if (array == null) {
			return OptionalDouble.empty();
		} else {
			return array[year - startYear];
		}
	}

	public TimeDataset(Matrix<K, YearMonth, Double> matrix) {
		SortedSet<YearMonth> months = new TreeSet<>(matrix.getCols());
		YearMonth start = months.first();
		YearMonth end = months.last();

		this.startYear = start.getYear();
		this.startMonth = start.getMonthValue();
		this.endYear = end.getYear();
		this.endMonth = end.getMonthValue();

		int size = 1 + end.getYear() * 12 + end.getMonthValue() - startYear * 12 - startMonth;
		for (K key : matrix.getRows()) {
			OptionalDouble[] array = new OptionalDouble[size];
			Arrays.fill(array, OptionalDouble.empty());
			monthlyValues.put(key, array);

			for (Map.Entry<YearMonth, Double> entry : matrix.getRow(key).entrySet()) {
				YearMonth time = entry.getKey();

				add0(time.getYear(), time.getMonthValue(), array, entry.getValue());
			}
		}
	}
}
