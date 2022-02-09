package net.carrossos.plib.data.aggregation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Aggregator<T, A, P extends Comparable<P>> {

	private class AggregationSpliterator implements Spliterator<A> {

		private final Iterator<T> transactions;

		private final Map<String, A> periodData = new LinkedHashMap<>();

		private final Map<String, List<A>> lastObservations = new HashMap<>();

		private final List<A> emptyObservations;

		private final int maxLastObs;

		private P current;

		private Iterator<A> aggregated = null;

		private T nextTransaction;

		private void aggregate(T transaction) {
			totalCount++;

			if (totalCount % 100000 == 0) {
				LOGGER.info("Aggregated " + totalCount + " transactions, period is currently '" + current + "'");
			}

			if (!aggregation.filter(transaction)) {
				return;
			}

			String id = aggregation.identify(transaction);
			A aggregated = periodData.get(id);

			if (aggregated == null) {
				aggregated = aggregation.createFrom(transaction);

				aggregatedCount++;
				processedCount++;
				periodData.put(id, aggregated);
			} else {
				if (aggregation.aggregate(transaction, aggregated)) {
					processedCount++;
				}
			}

		}

		private boolean aggregatePeriod() {
			if (nextTransaction != null) {
				startPeriod();

				aggregate(nextTransaction);
				nextTransaction = null;

				while (transactions.hasNext()) {
					T transaction = transactions.next();
					P period = periodFunction.apply(transaction);

					if (period.equals(current)) {
						aggregate(transaction);
					} else if (period.compareTo(current) < 0) {
						throw new IllegalStateException(
								"Transaction occurred before current period '" + current + "': " + transaction);
					} else {
						nextTransaction = transaction;
						break;
					}
				}

				endPeriod();

				return true;
			} else {
				return false;
			}
		}

		private void endPeriod() {
			for (Iterator<Map.Entry<String, A>> iterator = periodData.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, A> entry = iterator.next();
				List<A> lastObs = lastObservations.get(entry.getKey());

				if (!aggregation.analyze(entry.getValue(),
						lastObs == null ? emptyObservations : Collections.unmodifiableList(lastObs))) {
					iterator.remove();

					abandonnedAggregationCount++;
					aggregatedCount--;
				}
			}

			for (Entry<String, A> entry : periodData.entrySet()) {
				List<A> lastObs = lastObservations.get(entry.getKey());

				aggregation.finalize(entry.getValue(),
						lastObs == null ? emptyObservations : Collections.unmodifiableList(lastObs));
			}

			aggregation.periodEnd();

			for (Map.Entry<String, A> entry : periodData.entrySet()) {
				List<A> lastObs = lastObservations.get(entry.getKey());

				if (lastObs == null) {
					lastObs = new ArrayList<>(Collections.nCopies(maxLastObs, null));
					lastObservations.put(entry.getKey(), lastObs);
				} else {
					for (int i = lastObs.size() - 2; i >= 0; i--) {
						lastObs.set(i + 1, lastObs.get(i));
					}
				}

				lastObs.set(0, entry.getValue());
			}

			aggregated = periodData.values().stream().iterator();
		}

		private void startPeriod() {
			current = periodFunction.apply(nextTransaction);
			periodData.clear();

			aggregation.periodStart(current);
		}

		@Override
		public int characteristics() {
			return DISTINCT & ORDERED & IMMUTABLE;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public boolean tryAdvance(Consumer<? super A> consumer) {
			while (true) {
				if (aggregated == null) {
					if (aggregatePeriod()) {
						continue;
					} else {
						return false;
					}
				} else {
					if (aggregated.hasNext()) {
						consumer.accept(aggregated.next());
						return true;
					} else {
						aggregated = null;
						continue;
					}
				}
			}
		}

		@Override
		public Spliterator<A> trySplit() {
			return null;
		}

		public AggregationSpliterator(Stream<T> transactions, int maxLastObs) {
			this.transactions = transactions.iterator();
			this.maxLastObs = maxLastObs;
			this.emptyObservations = Collections.nCopies(maxLastObs, null);

			if (this.transactions.hasNext()) {
				this.nextTransaction = this.transactions.next();
			}
		}

	}

	private static final Logger LOGGER = LogManager.getLogger(Aggregator.class);

	private final Aggregation<T, A, P> aggregation;

	private final Function<? super T, ? extends P> periodFunction;

	private long totalCount = 0;

	private long processedCount = 0;

	private long aggregatedCount = 0;

	private long abandonnedAggregationCount = 0;

	public Stream<A> aggregate(Stream<T> transactions, int lastObsWindow) {
		return StreamSupport.stream(new AggregationSpliterator(transactions, lastObsWindow), false);
	}

	public long getAbandonnedAggregationCount() {
		return abandonnedAggregationCount;
	}

	public long getAggregatedCount() {
		return aggregatedCount;
	}

	public Aggregation<T, A, P> getAggregation() {
		return aggregation;
	}

	public long getProcessedCount() {
		return processedCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public Aggregator(Function<? super T, ? extends P> periodFunction, Aggregation<T, A, P> aggregation) {
		this.periodFunction = periodFunction;
		this.aggregation = aggregation;
	}
}
