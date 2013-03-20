package net.metarelate.terminology.commandline;

import java.util.Arrays;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;

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
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-sys") || arg.equalsIgnoreCase("system")) {
				nextIsSysDir=true;
			}
			if(arg.equalsIgnoreCase("-d")) {
				debug=true;
				System.out.println("Debug mode On");
			}
			if(nextIsSysDir) {
				sysDir=arg;
				nextIsSysDir=false;
			}
		}
		
		/**
		 * Building the initializer
		 */
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
			command =new CommandIngest(myInitializer,Arrays.copyOfRange(args,2,args.length)); // TODO excpet the first two!
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
		System.out.println("Unknown command.\n" +
				"Usage: ts [-d] ingest 	[help] [parameters] (import terminologies in rdf or labels)\n" +
				"       ts [-d] publish [help] [parameters]	(publish terminology)\n" +
				"       ts [-d] check 	[help] [parameters]	(check for constraints)\n" +
				"       ts [-d] clean 	[help] [parameters]	(remove from terminology)\n"+
				"       ts [-d] command [help] [parameters]	(single term actions)\n"+
				"       ts [-d] tag 	[help] [parameters]	(tag the current terminology state)\n" +
				"		ts [-d] web 	[help] [parameters]	(start the edit interface on port 8080)");
	}

}
