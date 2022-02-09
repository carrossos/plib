package net.carrossos.plib.db.jpa.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;

public class EclipseLinkLog4jSessionLog extends AbstractSessionLog {

	private static final Logger LOGGER = LogManager.getLogger("org.eclipse.persistence");

	@Override
	public void log(SessionLogEntry entry) {
		Marker marker;

		if (entry.getNameSpace() == null) {
			marker = null;
		} else {
			marker = MarkerManager.getMarker(entry.getNameSpace());
		}

		LOGGER.log(convertLevel(entry.getLevel()), marker, () -> formatMessage(entry), entry.getException());
	}

	private static Level convertLevel(int level) {
		switch (level) {
		case SessionLog.SEVERE:
			return Level.ERROR;
		case SessionLog.WARNING:
			return Level.WARN;
		case SessionLog.INFO:
			return Level.INFO;
		case SessionLog.CONFIG:
			return Level.INFO;
		case SessionLog.FINE:
			return Level.DEBUG;
		case SessionLog.FINER:
			return Level.TRACE;
		case SessionLog.FINEST:
			return Level.TRACE;
		default:
			throw new IllegalArgumentException("Unknown level: " + level);
		}
	}

}
