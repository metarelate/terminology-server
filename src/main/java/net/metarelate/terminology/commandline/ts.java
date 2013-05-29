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

package net.metarelate.terminology.commandline;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

public class ts {
	public static Initializer myInitializer=null;
	
	public static void main(String[] args) {
		Loggers.commandLogger.info("Starting tServer v."+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")");
		String sysDir=null;
		boolean debug=false;
		if(args.length<1) {
			commandUnknownError();
			return;
		}
		if(args[0].equals("-d") && args.length<2) {
			commandUnknownError();
			return;
		}
		
		/**
		 * We first collect things we need for the rest of the execution
		 */
		
		boolean nextIsSysDir=false;
		boolean nextIsHelp=false;
		String helpFocus=null;
		boolean helpOnly=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-sys") || arg.equalsIgnoreCase("system")) {
				nextIsSysDir=true;
			}
			else if(arg.equalsIgnoreCase("-d")) {
				debug=true;
				Loggers.commandLogger.debug("Debug mode On");
			}
			else if(nextIsSysDir) {
				sysDir=arg;
				nextIsSysDir=false;
			}
			else if (arg.equalsIgnoreCase("help")) {
				nextIsHelp=true;
				helpOnly=true;
			}
			else if(nextIsHelp) {
				helpFocus=arg;
				nextIsHelp=false;
			}
		}
		
		if(helpOnly) {		//We don't build anything in this case.
			if(helpFocus==null) System.out.println(getGenericHelpMessage());
			else if(helpFocus.equalsIgnoreCase("ingest")) {
				System.out.println(CommandIngest.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("publish")) {
				System.out.println(CommandPublish.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("check")) {
				System.out.println(CommandCheck.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("clean")) {
				System.out.println(CommandClean.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("command")) {
				System.out.println(OLD_CommandCommand.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("tag")) {
				System.out.println(TagCommand.getStaticLocalHelpMessage());
			}
			else if(helpFocus.equalsIgnoreCase("web")) {
				System.out.println(WebCommand.getStaticLocalHelpMessage());
			}
			
				
		}
		if(helpOnly) System.exit(0);
		/**
		 * Building the initializer
		 */
		if(debug) Loggers.debugOn();
		try {
			if(sysDir!=null) myInitializer=new Initializer(sysDir,debug);
			else myInitializer=new Initializer();
		} catch (ConfigurationException e) {
			Loggers.commandLogger.fatal("Sorry, could not start the system");
			e.printStackTrace();
		}
		
		/*************
		 * Building command executors		
		 */
		String argument=args[0];
		if(debug) argument=args[1];
		
		TsCommand command=null;
		if(argument.equalsIgnoreCase("ingest")) {
			Loggers.commandLogger.debug("Command: Ingest");
			command =new CommandIngest(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("publish")) {
			Loggers.commandLogger.debug("Command: Publish");
			command =new CommandPublish(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("check")) {	// TODO unimplemented
			command =new CommandCheck(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("clean")) {	// TODO unimplemented
			command =new CommandClean(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("command")) { // TODO unimplemented
			command =new OLD_CommandCommand(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("tag")) {		
			Loggers.commandLogger.debug("Command: Tag");
			command =new TagCommand(myInitializer,args,debug); 
		}
		else if(argument.equalsIgnoreCase("web")) {		
			Loggers.commandLogger.debug("Command: Web");
			command =new WebCommand(myInitializer,args,debug); 
		}
		else {
			commandUnknownError();
			return;
		}
		
		/*******
		 * Execution
		 */
		
		try {
			command.execute();
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void commandUnknownError() {
		Loggers.commandLogger.error("Unknown command");
		System.out.println(getGenericHelpMessage());
	}
	private static String getGenericHelpMessage() {
		return	"Usage: ts [-d] [help] ingest   [parameters]    (import terminologies in rdf or labels)\n" +
				"       ts [-d] [help] publish  [parameters]    (publish terminology)\n" +
				"       ts [-d] [help] check    [parameters]    (check for constraints)\n" +
				"       ts [-d] [help] clean    [parameters]    (remove from terminology)\n"+
				"       ts [-d] [help] command  [parameters]    (single term actions)\n"+
				"       ts [-d] [help] tag      [parameters]    (tag the current terminology state)\n" ;
	}

}
