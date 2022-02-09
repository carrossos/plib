package net.carrossos.plib.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatisticsTest {
	@Test
	void rolling() {
		final double DELTA = 1e-5;
		double[] sample = new double[] { 1, 997, 42, -6331, 26544, 8 };

		Statistics statistics = Statistics.calculate(sample);

		assertEquals(3543.5, statistics.getAverage(), DELTA);
		assertEquals(134064596.3, statistics.getVariance(), DELTA);
		assertEquals(11578.6267018157, statistics.getStdDev(), DELTA);

		double[] newSample = new double[] { 1, 5, 2, 3, 4, 8 };

		for (int i = 0; i < sample.length; i++) {
			statistics.push(sample[i], newSample[i]);
		}

		assertEquals(3.83333333333333, statistics.getAverage(), DELTA);
		assertEquals(6.16666666666667, statistics.getVariance(), DELTA);
		assertEquals(2.48327740429189, statistics.getStdDev(), DELTA);
	}

	@Test
	void simpleTest() {
		final double DELTA = 1e-10;
		double[] sample = new double[] { 1, 5, 2, 3, 4, 8 };

		Statistics statistics = Statistics.calculate(sample);

		assertEquals(3.83333333333333, statistics.getAverage(), DELTA);
		assertEquals(6.16666666666667, statistics.getVariance(), DELTA);
		assertEquals(2.48327740429189, statistics.getStdDev(), DELTA);
	}

}
