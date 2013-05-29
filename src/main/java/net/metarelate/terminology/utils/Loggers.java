package net.metarelate.terminology.utils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Loggers {
	

	public static void init() {
		BasicConfigurator.configure();
		// TODO this should be replaced by a proper configuration
	}
	public static final Logger topLogger=Logger.getLogger("net.metarelate");
	public static final Logger coreLogger=Logger.getLogger("net.metarelate.core");
	public static final Logger importLogger=Logger.getLogger("net.metarelate.import");
	public static final Logger publishLogger=Logger.getLogger("net.metarelate.publish");
	public static final Logger commandLogger=Logger.getLogger("net.metarelate.command");
	public static final Logger authLogger=Logger.getLogger("net.metarelate.auth");
	
	public static void debugOn() {
		setLevel(org.apache.log4j.Level.DEBUG);
	}
	public static void setLevel(Level level) {
		topLogger.setLevel(level);
	}
	public static void warningOn() {
		setLevel(org.apache.log4j.Level.WARN);
		
	}
}
