package net.metarelate.terminology.utils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Loggers {
	

	

	static boolean debugOn=false;
	
	public static void init() {
		BasicConfigurator.configure();
		debugAuxOff();
		// TODO this should be replaced by a proper configuration
	}
	public static final Logger topLogger=Logger.getLogger("net.metarelate");
	public static final Logger coreLogger=Logger.getLogger("net.metarelate.core");
	public static final Logger importLogger=Logger.getLogger("net.metarelate.import");
	public static final Logger pragmaLogger=Logger.getLogger("net.metarelate.import.pragma");
	public static final Logger publishLogger=Logger.getLogger("net.metarelate.publish");
	public static final Logger commandLogger=Logger.getLogger("net.metarelate.command");
	public static final Logger authLogger=Logger.getLogger("net.metarelate.auth");
	public static final Logger processLogger=Logger.getLogger("net.metarelate.process");
	public static final Logger validationLogger=Logger.getLogger("net.metarelate.validation");
	public static final Logger policyLogger=Logger.getLogger("net.metarelate.policy");
	public static final Logger reasonerLogger=Logger.getLogger("net.metarelate.inference");
	public static final Logger webAdminLogger=Logger.getLogger("net.metarelate.webadmin");
	public static final Logger jettyLogger=Logger.getLogger("org.eclipse.jetty");
	public static final Logger jenaLogger=Logger.getLogger("com.hp.hpl.jena");
	public static void debugOn() {
		setLevel(org.apache.log4j.Level.DEBUG);
		debugOn=true;
	}
	public static void setLevel(Level level) {
		topLogger.setLevel(level);
	}
	public static void warningOn() {
		setLevel(org.apache.log4j.Level.WARN);
		debugOn=false;
	}
	
	public static void infoOn() {
		setLevel(org.apache.log4j.Level.INFO);
		debugOn=false;
	}
	public static void traceOn() {
		setLevel(org.apache.log4j.Level.TRACE);
		debugOn=true;
		
	}
	public static boolean isDebugOn() {
		return debugOn;
	}
	private static void debugAuxOn() {
		jenaLogger.setLevel(org.apache.log4j.Level.DEBUG);
		jettyLogger.setLevel(org.apache.log4j.Level.DEBUG);
	}
	private static void debugAuxOff() {
		jenaLogger.setLevel(org.apache.log4j.Level.WARN);
		jettyLogger.setLevel(org.apache.log4j.Level.WARN);
	}
	
}
