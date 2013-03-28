package net.metarelate.terminology.commandline;

import java.util.Arrays;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SSLogger;

public class ts {
	public static Initializer myInitializer=null;
	
	public static void main(String[] args) {
		System.out.println("Starting tServer v."+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")");
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
				System.out.println("Debug mode On");
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
				System.out.println(CommandCommand.getStaticLocalHelpMessage());
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
		SSLogger.showDebug(debug);
		try {
			if(sysDir!=null) myInitializer=new Initializer(sysDir,debug);
			else myInitializer=new Initializer();
		} catch (ConfigurationException e) {
			System.out.println("Sorry, could not start the system");
			e.printStackTrace();
		}
		
		/*************
		 * Building command executors		
		 */
		String argument=args[0];
		if(debug) argument=args[1];
		
		TsCommand command=null;
		if(argument.equalsIgnoreCase("ingest")) {
			System.out.println("Command: Ingest");
			command =new CommandIngest(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("publish")) {
			command =new CommandPublish(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("check")) {
			command =new CommandCheck(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("clean")) {
			command =new CommandClean(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("command")) {
			command =new CommandCommand(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("tag")) {
			command =new TagCommand(myInitializer,args,debug); // TODO excpet the first two!
		}
		else if(argument.equalsIgnoreCase("web")) {
			command =new WebCommand(myInitializer,args,debug); // TODO excpet the first two!
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
		}
		
	}
	
	private static void commandUnknownError() {
		System.out.println("Unknown command");
		System.out.println(getGenericHelpMessage());
	}
	private static String getGenericHelpMessage() {
		return	"Usage: ts [-d] [help] ingest   [parameters]    (import terminologies in rdf or labels)\n" +
				"       ts [-d] [help] publish  [parameters]    (publish terminology)\n" +
				"       ts [-d] [help] check    [parameters]    (check for constraints)\n" +
				"       ts [-d] [help] clean    [parameters]    (remove from terminology)\n"+
				"       ts [-d] [help] command  [parameters]    (single term actions)\n"+
				"       ts [-d] [help] tag      [parameters]    (tag the current terminology state)\n" +
				"       ts [-d] [help] web      [parameters]    (start the edit interface on port 8080)";
	}

}
