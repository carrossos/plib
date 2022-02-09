package net.carrossos.plib.db.jpa.impl;

import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;

import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class EclipseLinkLog4jSessionLog extends AbstractSessionLog {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("org.eclipse.persistence");

	@Override
	public void log(SessionLogEntry entry) {
		Marker marker;

		if (entry.getNameSpace() == null) {
			marker = null;
		} else {
			marker = MarkerFactory.getMarker(entry.getNameSpace());
		}

		switch (level) {
		case SessionLog.SEVERE:
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(marker, formatMessage(entry), entry.getException());
			}

			return;
		case SessionLog.WARNING:
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(marker, formatMessage(entry), entry.getException());
			}

			return;
		case SessionLog.INFO:
		case SessionLog.CONFIG:
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(marker, formatMessage(entry), entry.getException());
			}

			return;
		case SessionLog.FINE:
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(marker, formatMessage(entry), entry.getException());
			}

			return;
		case SessionLog.FINER:
		case SessionLog.FINEST:
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace(marker, formatMessage(entry), entry.getException());
			}

			return;

		default:
			LOGGER.warn(marker, "Unkown Eclipselink level {} for {}", level, formatMessage(entry),
					entry.getException());
		}

	}

}
