package net.carrossos.plib.data.aggregation;

import java.util.List;
import java.util.function.Function;

import net.carrossos.plib.data.grid.CellSelector;

public interface Aggregation<T, A, P> {
	public boolean aggregate(T transaction, A aggregated);

	public boolean analyze(A aggregated, List<A> lastObs);

	public A createFrom(T transaction);

	public boolean filter(T transaction);

	public void finalize(A aggregated, List<A> lastObs);

	public String identify(T transaction);

	public void periodEnd();

	public void periodStart(P period);

	public void statistics(Function<String, CellSelector> tableProvider);
}
