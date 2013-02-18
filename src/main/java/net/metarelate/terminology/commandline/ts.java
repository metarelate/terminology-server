package net.metarelate.terminology.commandline;

import java.util.Arrays;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;

public class ts {
	public static Initializer myInitializer=null;
	
	public static void main(String[] args) {
		System.out.println("Starting tServer v."+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")");
		
		/**
		 * First we check if one of the argument contains the system directory, which we need for everything.
		 * TODO eventually we could catch here all parameters relevant to initializer, and provide the remaining
		 * or arguments in args.
		 */
		String sysDir=null;
		boolean nextIsSysDir=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-sys") || arg.equalsIgnoreCase("system")) {
				nextIsSysDir=true;
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
			// TODO see what to do with configurations...
			if(sysDir!=null) myInitializer=new Initializer(sysDir);
			else myInitializer=new Initializer();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//for(String arg: args) {
		//	System.out.println(arg);
		//}
		/*************
		 * Building command executors		
		 */
		
		TsCommand command=null;
		if(args[0].equalsIgnoreCase("ingest")) {
			System.out.println("Command: Ingest");
			command =new CommandIngest(myInitializer,Arrays.copyOfRange(args,2,args.length)); // TODO excpet the first two!
		}
		else if(args[0].equalsIgnoreCase("publish")) {
			command =new CommandPublish(myInitializer,args); // TODO excpet the first two!
		}
		else if(args[0].equalsIgnoreCase("check")) {
			command =new CommandCheck(myInitializer,args); // TODO excpet the first two!
		}
		else if(args[0].equalsIgnoreCase("clean")) {
			command =new CommandClean(myInitializer,args); // TODO excpet the first two!
		}
		else if(args[0].equalsIgnoreCase("command")) {
			command =new CommandCommand(myInitializer,args); // TODO excpet the first two!
		}
		else if(args[0].equalsIgnoreCase("tag")) {
			command =new TagCommand(myInitializer,args); // TODO excpet the first two!
		}
		else {
			commandUnknownError();
			return;
		}
		
		/*******
		 * Execution
		 */
		
		command.execute();
		
	}
	
	private static void commandUnknownError() {
		System.out.println("Unknown command.\n" +
				"Usage: ts ingest (import terminologies in rdf or labels)\n" +
				"       ts publish (publish terminology)\n" +
				"       ts check (check for constraints)\n" +
				"       ts clean (remove from terminology)\n"+
				"       ts command (single term actions)\n"+
				"       ts tag (tag the current terminology state)");
	}

}
