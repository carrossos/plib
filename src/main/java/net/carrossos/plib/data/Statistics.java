package net.carrossos.plib.data;

import java.util.Arrays;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class Statistics {

	private final long window;

	private long count;

	private double average;

	private double variance;

	public double getAverage() {
		return average;
	}

	public long getCount() {
		return count;
	}

	public double getStdDev() {
		return Math.sqrt(variance);
	}

	public double getVariance() {
		return variance;
	}

	public long getWindow() {
		return window;
	}

	public void push(double out, double value) {
		count++;

		double oldAverage = average;

		average = oldAverage + (value - out) / window;
		variance += (value - out) * (value - average + out - oldAverage) / (window - 1);
	}

	public <T> void push(T out, T in, ToDoubleFunction<T> function) {
		push(function.applyAsDouble(out), function.applyAsDouble(in));
	}

	private Statistics(long count, double average, double variance) {
		this.count = count;
		this.window = count;
		this.average = average;
		this.variance = variance;
	}

	public static <T> Statistics calculate(double[] values) {
		return calculate(Arrays.stream(values).iterator());
	}

	public static <T> Statistics calculate(OfDouble values) {
		if (!values.hasNext()) {
			throw new IllegalArgumentException("No values!");
		}

		long count = 1;
		double value = values.next();
		double sum = value;
		double c = 0;
		double m = value;
		double s = 0;

		while (values.hasNext()) {
			count++;
			value = values.next();

			double t = sum + value;
			if (Math.abs(sum) >= Math.abs(value)) {
				c += sum - t + value;
			} else {
				c += value - t + sum;
			}

			sum = t;

			double oldM = m;
			m += (value - m) / count;
			s += (value - m) * (value - oldM);
		}

		return new Statistics(count, (sum + c) / count, s / (count - 1));
	}

	public static <T> Statistics calculate(Stream<T> values, ToDoubleFunction<T> function) {
		return calculate(values.mapToDouble(function).iterator());
	}
}
