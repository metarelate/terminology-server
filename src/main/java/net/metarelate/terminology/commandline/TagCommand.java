package net.metarelate.terminology.commandline;

import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;

public class TagCommand extends TsCommand {
	Initializer myInitializer=null;
	String tag=null;
	String message="";
	public TagCommand(Initializer myInitializer, String[] args, boolean debug) {
		super(myInitializer, args);
		debugOn=debug;
		for(String arg:args) if(arg.equals("help")) {
			localHelp();
			return;
		}
		boolean nextIsTag=false;
		boolean nextIsMessage=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-t")) {
				nextIsTag=true;
			}
			else if(nextIsTag) {
				tag=arg;
				nextIsTag=false;
			}
			if(arg.equalsIgnoreCase("-m")) {
				nextIsMessage=true;
			}
			else if(nextIsTag) {
				message=arg;
				nextIsMessage=false;
			}
		}
		this.myInitializer=myInitializer;
		
	}



	@Override
	public void localExecute() throws ModelException {
		if(tag==null) {
			localHelp();
		}
		String authorURI=myInitializer.getDefaultUserURI();
		try {
			myInitializer.myTerminologyManager.tagRelease(authorURI, tag, message);
		} catch (AuthException e) {
			System.out.println("Sorry, could not do it.");
			if(debugOn) e.printStackTrace();
		}
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void localHelp() {
		System.out.println("ts tag -t tag -m message");
		System.out.println("tag the current state of the terminology server");
		System.out.println("-t tag: required ");
		System.out.println("-t message: optional ");
	}

}
