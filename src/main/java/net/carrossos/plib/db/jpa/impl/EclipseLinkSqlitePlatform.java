package net.carrossos.plib.db.jpa.impl;

import org.eclipse.persistence.internal.databaseaccess.DatabaseCall;
import org.eclipse.persistence.internal.expressions.ExpressionSQLPrinter;
import org.eclipse.persistence.internal.expressions.SQLSelectStatement;
import org.eclipse.persistence.platform.database.DatabasePlatform;
import org.eclipse.persistence.queries.ValueReadQuery;

public class EclipseLinkSqlitePlatform extends DatabasePlatform {

	private static final long serialVersionUID = 3415537274088469265L;

	private static final String LIMIT = " LIMIT ";

	private static final String OFFSET = " OFFSET ";

	@Override
	public ValueReadQuery buildSelectQueryForIdentity() {
		ValueReadQuery selectQuery = new ValueReadQuery();
		selectQuery.setSQLString("select last_insert_rowid();");
		return selectQuery;
	}

	@Override
	public void printSQLSelectStatement(DatabaseCall call, ExpressionSQLPrinter printer, SQLSelectStatement statement) {
		int max = 0;
		if (statement.getQuery() != null) {
			max = statement.getQuery().getMaxRows();
		}
		if (max <= 0 || !this.shouldUseRownumFiltering()) {
			super.printSQLSelectStatement(call, printer, statement);
			return;
		}
		// statement.setUseUniqueFieldAliases(true);
		call.setFields(statement.printSQL(printer));
		printer.printString(LIMIT);
		printer.printParameter(DatabaseCall.MAXROW_FIELD);
		printer.printString(OFFSET);
		printer.printParameter(DatabaseCall.FIRSTRESULT_FIELD);
		call.setIgnoreFirstRowSetting(true);
		call.setIgnoreMaxResultsSetting(true);
	}

	@Override
	public boolean shouldUseJDBCOuterJoinSyntax() {
		return false;
	}
}
