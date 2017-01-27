package black.arpanet.util.logging;

import org.apache.logging.log4j.Logger;

//Faster to check for enabled
//logging levels than to let
//the Log4J stack do it
//Also, use import static
//for cleaner logging
public class ArpanetLogUtil {

	public static void t(Logger logger, String message) {
		if(logger.isTraceEnabled()) {
			logger.trace(message);
		}
	}

	public static void t(Logger logger, String message, Throwable t) {
		if(logger.isTraceEnabled()) {
			logger.trace(message,t);
		}
	}

	public static void d(Logger logger, String message) {
		if(logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}

	public static void d(Logger logger, String message, Throwable t) {
		if(logger.isDebugEnabled()) {
			logger.debug(message,t);
		}
	}

	public static void i(Logger logger, String message) {
		if(logger.isInfoEnabled()) {
			logger.info(message);
		}
	}

	public static void i(Logger logger, String message, Throwable t) {
		if(logger.isInfoEnabled()) {
			logger.info(message,t);
		}
	}

	public static void w(Logger logger, String message) {
		logger.warn(message);
	}

	public static void w(Logger logger, String message, Throwable t) {
		logger.warn(message,t);
	}

	public static void e(Logger logger, String message) {
		logger.error(message);
	}

	public static void e(Logger logger, String message, Throwable t) {
		logger.error(message,t);
	}

	public static void f(Logger logger, String message) {
		logger.fatal(message);
	}

	public static void f(Logger logger, String message, Throwable t) {
		logger.fatal(message,t);
	}

	//Debug or Trace
	//Allows substituting a more verbose message if trace is turned on
	public static void dot(Logger logger, String traceMessage, String debugMessage) {
		if(logger.isTraceEnabled()) {
			logger.debug(traceMessage);
		} else if(logger.isDebugEnabled()) {
			logger.debug(debugMessage);
		}
	}

}
