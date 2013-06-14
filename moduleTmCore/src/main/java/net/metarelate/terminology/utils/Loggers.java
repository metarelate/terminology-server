/* 
 (C) British Crown Copyright 2011 - 2013, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

package net.metarelate.terminology.utils;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 * Controls loggers (log4j)
 * @author andrea_splendiani
 *
 */
public class Loggers {

	private static boolean debugOn=false;
	
	/**
	 * Initializes loggers
	 */
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
	
	private static void setLevel(Level level) {
		topLogger.setLevel(level);
	}
	
	/**
	 * Sets the debug level to warning (can be lower for libraries)
	 */
	public static void warningOn() {
		setLevel(org.apache.log4j.Level.WARN);
		debugAuxOff();
		debugOn=false;
	}
	
	/**
	 * Sets the debug level to info (can be lower for libraries)
	 */
	public static void infoOn() {
		setLevel(org.apache.log4j.Level.INFO);
		debugAuxOff();
		debugOn=false;
	}
	
	/**
	 * Sets the debug level to debug (can be lower for libraries)
	 */
	public static void debugOn() {
		setLevel(org.apache.log4j.Level.DEBUG);
		debugAuxOff();
		debugOn=true;
	}
	
	/**
	 * Sets the debug level to trace (can be lower for libraries)
	 */
	public static void traceOn() {
		setLevel(org.apache.log4j.Level.TRACE);
		debugAuxOn();
		debugOn=true;
		
	}
	/**
	 * @return true if the terminology server is running in debug mode
	 */
	public static boolean isDebugOn() {
		return debugOn;
	}
	
	private static void debugAuxOn() {
		jenaLogger.setLevel(org.apache.log4j.Level.WARN);
		jettyLogger.setLevel(org.apache.log4j.Level.DEBUG);
	}
	private static void debugAuxOff() {
		jenaLogger.setLevel(org.apache.log4j.Level.ERROR);
		jettyLogger.setLevel(org.apache.log4j.Level.WARN);
	}
	
}
