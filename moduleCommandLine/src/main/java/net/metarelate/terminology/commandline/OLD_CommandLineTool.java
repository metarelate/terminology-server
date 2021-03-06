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
/**
 * TODO This Class is only functional to @net.metarelate.terminology.commandline.MetOp
 * It should be removed once that class is removed.	
 */
package net.metarelate.terminology.commandline;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyFactoryTDBImpl;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;
/**
 * An abstract class that provides some basic structure to a command line program.
 * This class provides a method startCommand() that must be called by the implementing class.
 * In turns this calls:
 * - print message (abstract, demanded to implementing class)
 * - parse global arguments (implemented in this class)
 * - parse local arguments (abstract, demanded to implementing class)
 * - executeCommand (abstract, demanded to implementing class)
 * Argument are expected to folow the convention -XX value
 * Where -XX (prefixed by "-") can have any length, no spaces between "-" and "XX" and one or more spaces between "XX" and the value.
 * The order or parameters doesn't matter, but each -XX must be followed by the relative parameters (one or more. If more than one, order matters).
 * The last -XX option can have an arbitrary number of parameters, and is generally used to indicate a generic collection of files.
 * @author andreasplendiani
 *
 */

public abstract class OLD_CommandLineTool {
	protected String tdbLocation="";
	protected TerminologyFactory myFactory=null;
	private int indexOfTDB=-1;
	
	protected abstract void printStartMessage();
	protected abstract void parseLocal(String[] args);
	protected abstract void executeCommand() throws InvalidProcessException;
	
	public Initializer myInitializer=null;
	
	/**
	 * See discussion in the class description {@link OLD_CommandLineTool}
	 * @param args
	 */
	protected void startCommand(String[] args) {
		printStartMessage();
		parseGlobal(args);
		parseLocal(args);
		try {
			startupSystem();
			executeCommand();
		} catch (ConfigurationException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (InvalidProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// Implementation detail:
	// Parsing arguments is done in two stages: first options are found (and immediately executed when they don't take arguments)
	// Then all found options are evaluated for further arguments.
	
	private void startupSystem() throws ConfigurationException {
		// TODO things should change here in order to pass/override settings specified in the command line
		myInitializer=new Initializer();
		
	}
	protected void parseGlobal(String[] args) {
		//Parameters identification
		for(int i=0;i<args.length;i++) {
			if(args[i].equals("--help") || args[i].equals("-help") || args[i].equals("help")) {
				printHelp();
				System.exit(0);
			}
			if(args[i].equalsIgnoreCase("-d")) {
				Loggers.debugOn();
				Loggers.commandLogger.debug("debug=true");
			}
			if(args[i].equalsIgnoreCase("-w")) {
				Loggers.warningOn();
				Loggers.commandLogger.debug("warning=true");
			}
			if(args[i].equalsIgnoreCase("-tdb")) {
				indexOfTDB=i;
			}
		}
		//Parameters validation
		if(indexOfTDB>=0) {
			tdbLocation=args[indexOfTDB+1];
			myFactory=new TerminologyFactoryTDBImpl(tdbLocation);
		}
		else exitWrongUsage("Missing TDB");				
	}
	
	/**
	 * To be overridden with the function printing the help text for the specific command
	 */
	protected abstract void printLocalHelp() ;
	
	/**
	 * Called if an invalid command is executed (wrong or insufficient parameters)
	 * @param message
	 */
	protected void exitWrongUsage(String message) {
		System.out.print("Wrong usage: ");
		System.out.println(message);
		printHelp();
		System.exit(-1);
	}
	
	
	private void printHelp() {
		System.out.println("Help:");
		printLocalHelp();
		System.out.println("Additional paramaters:");
		System.out.println("-d (show debug) -w (show warnings) -help (show help)");
		
		
	}
	

}
