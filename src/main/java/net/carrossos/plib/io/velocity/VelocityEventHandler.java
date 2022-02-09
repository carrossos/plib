package net.carrossos.plib.io.velocity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.event.InvalidReferenceEventHandler;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.Info;

class VelocityEventHandler implements InvalidReferenceEventHandler, MethodExceptionEventHandler {

	private static final Logger LOGGER = LogManager.getLogger(VelocityEventHandler.class);

	private boolean strict;

	private void checkStrict(Exception e) {
		if (strict) {
			throw new IllegalStateException("Unhandled exception", e);
		}
	}

	private void checkStrict(String reference, Info info) {
		if (strict) {
			throw new IllegalStateException(String.format("Invalid reference '%s' at '%s'", reference, info));
		}
	}

	@Override
	public Object invalidGetMethod(Context context, String reference, Object object, String property, Info info) {
		LOGGER.warn("Invalid reference '{}' during 'get' on property '{}' at {}", reference, property, info);

		checkStrict(reference, info);

		return null;
	}

	@Override
	public Object invalidMethod(Context context, String reference, Object object, String method, Info info) {
		LOGGER.warn("Invalid method '{}' on reference '{}' at {}", method, reference, info);

		checkStrict(reference, info);

		return null;
	}

	@Override
	public boolean invalidSetMethod(Context context, String leftreference, String rightreference, Info info) {
		LOGGER.warn("Invalid reference '{}' during 'set' from '{}' to '{}' at {}", leftreference, rightreference, info);

		checkStrict(leftreference, info);

		return false;
	}

	@Override
	public Object methodException(Context context, @SuppressWarnings("rawtypes") Class clazz, String method,
			Exception e, Info info) {
		LOGGER.error(String.format("Caught exception '%s' while calling '%s' at %s", e.getClass().getSimpleName(),
				method, info), e);

		checkStrict(e);

		return null;
	}

	VelocityEventHandler(boolean strict) {
		this.strict = strict;
	}
}
