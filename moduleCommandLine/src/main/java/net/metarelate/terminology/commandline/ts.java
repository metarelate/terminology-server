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
	private static final int DEBUG_TRACE=0;	// TODO we could use directly log4j consts.
	private static final int DEBUG_DEBUG=1;
	private static final int DEBUG_INFO=2;
	
	private static final int COMMAND_UNDEF=0;
	private static final int COMMAND_HELP=1;
	private static final int COMMAND_INGEST=2;
	private static final int COMMAND_PUBLISH=3;
	private static final int COMMAND_TAG=4;
	private static final int COMMAND_WEB=5;
	private static final int COMMAND_CLEAN=10;
	private static final int COMMAND_CHECK=11;
	private static final int COMMAND_COMMAND=12;
	
	
	public static Initializer myInitializer=null;
	
	public static void main(String[] args) {
		Loggers.init();
		Loggers.infoOn();
		Loggers.commandLogger.info("Starting tServer v."+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")");
		String sysDir=null;
		int debugLevel=DEBUG_INFO;
		
		
		boolean nextIsSysDir=false;
		boolean nextIsHelp=false;
		String helpFocus=null;
		int command=COMMAND_UNDEF;
		
		int argCounter=0;
		String[] commandArgs=null;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-sys") || arg.equalsIgnoreCase("-system")) {
				nextIsSysDir=true;
			}
			else if(arg.equalsIgnoreCase("-d")) {
				debugLevel=DEBUG_DEBUG;
				Loggers.debugOn();
				Loggers.commandLogger.debug("Debug mode On");
			}
			else if(arg.equalsIgnoreCase("-t")) {
				Loggers.traceOn();
				Loggers.commandLogger.debug("Trace mode On");
			}
			else if(nextIsSysDir) {
				sysDir=arg;
				nextIsSysDir=false;
			}
			else if(arg.equalsIgnoreCase("help")) {
				nextIsHelp=true;
				command=COMMAND_HELP;
			}
			else if(nextIsHelp) {
				helpFocus=arg;
				nextIsHelp=false;
			}
			else if (arg.equalsIgnoreCase("ingest")) {
				if(command==COMMAND_UNDEF) command=COMMAND_INGEST;
			}
			else if (arg.equalsIgnoreCase("publish")) {
				if(command==COMMAND_UNDEF) command=COMMAND_PUBLISH;
			}
			else if (arg.equalsIgnoreCase("tag")) {
				if(command==COMMAND_UNDEF) command=COMMAND_TAG;
			}
			else if (arg.equalsIgnoreCase("web")) {
				if(command==COMMAND_UNDEF) command=COMMAND_WEB;
			}
			
			
			argCounter++;
		}
		// first chunk of param analysis done.
		
		//Help just print something, so in case we do it and exit now
		if(command==COMMAND_HELP) {
			processHelp(helpFocus); 
			System.exit(0);
		}
		
		//If we didn't understand what to do, we print an help message and exit now
		if(command==COMMAND_UNDEF) {
			commandUnknownError();
			System.exit(-1);
		}
		
		//Now we build the command, that parses all arguments in its constructor.
		//Note that some arguments (sys) are bound to be parsed twice and we could avoid it.
		//We don't assume in TsCommand that arguments consumed by TsServer are present. 
		
		TsCommand commandExec=null;
		if(command==COMMAND_INGEST) {
			Loggers.commandLogger.debug("Command: Ingest");
			commandExec =new CommandIngest(sysDir,args); 
		}
		else if(command==COMMAND_PUBLISH) {
			Loggers.commandLogger.debug("Command: Publish");
			commandExec =new CommandPublish(sysDir,args); 
		}
		else if(command==COMMAND_TAG) {		
			Loggers.commandLogger.debug("Command: Tag");
			commandExec =new TagCommand(sysDir,args); 
		}
		else if(command==COMMAND_WEB) {		
			Loggers.commandLogger.debug("Command: Web");
			commandExec =new WebCommand(sysDir,args); 
		}
		else if(command==COMMAND_CHECK) {	// TODO unimplemented
			commandExec =new CommandCheck(sysDir,args); 
		}
		else if(command==COMMAND_CLEAN) {	// TODO unimplemented
			commandExec =new CommandClean(sysDir,args); 
		}
		else if(command==COMMAND_COMMAND) { // TODO unimplemented
			commandExec =new OLD_CommandCommand(sysDir,args); 
		}
		
		else {
			commandUnknownError();
			System.exit(-1);
		}
		
		/***************************
		 * Execution
		 ***************************/
		Loggers.commandLogger.trace("Checking command constraints");
		if(!commandExec.validate()) {
			System.out.println("Missing parameters for "+commandExec.getName());
			System.out.println(commandExec.getLocalHelpMessage());
			System.exit(-1);
		}
		Loggers.commandLogger.trace("Command constraints passed");
		try {
			commandExec.execute();
		} catch (ModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void processHelp(String helpFocus) {
		Loggers.commandLogger.debug("Help for : "+helpFocus);
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
		else System.out.println(getGenericHelpMessage());
		
		
	}

	private static void commandUnknownError() {
		Loggers.commandLogger.error("Unknown command");
		System.out.println(getGenericHelpMessage());
	}
	private static String getGenericHelpMessage() {
		return	"Usage: ts [-d|-t] [-sys|-system dirName] [help] ingest   [parameters]    (import terminologies in rdf or labels)\n" +
				"       ts [-d|-t] [-sys|-system dirName] [help] publish  [parameters]    (publish terminology)\n" +
				//"       ts [-d|-t] [-sys|-system dirName] [help] check    [parameters]    (check for constraints)\n" +
				//"       ts [-d|-t] [-sys|-system dirName] [help] clean    [parameters]    (remove from terminology)\n"+
				//"       ts [-d|-t] [-sys|-system dirName] [help] command  [parameters]    (single term actions)\n"+
				"       ts [-d|-t] [-sys|-system dirName] [help] tag      [parameters]    (tag the current terminology state)\n" +
				"       ts [-d|-t] [-sys|-system dirName] [help] web      [parameters]    (starts the administration web interface)\n" +
				"       [-d|-t] : debug level option. Default is info only.\n" +
				"       [-sys|-system] : allow to specify the installation dir. Default $TSHOME or, if undefined $HOME/.tserver\n" +
				"       [help] provides help on the specific command (without initializing the system)\n" ;
	}

}
