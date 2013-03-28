package net.metarelate.terminology.commandline;

import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;

public class TagCommand extends TsCommand {
	Initializer myInitializer=null;
	String tag=null;
	String message="";
	public TagCommand(Initializer myInitializer, String[] args, boolean debug) {
		super(myInitializer, args,debug);
		
	
		boolean nextIsTag=false;
		
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-t")) {
				nextIsTag=true;
			}
			else if(nextIsTag) {
				tag=arg;
				nextIsTag=false;
			}
			
		}
		this.myInitializer=myInitializer;
		
	}



	@Override
	public void localExecute() throws ModelException {
		
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
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	
	public static String getStaticLocalHelpMessage() {
		return "ts tag -t tag -m message\n"+
		"tag the current state of the terminology server\n"+
		"-t tag: required\n"+
		"-t message: optional ";
	}



	@Override
	public boolean validate() {
		if(tag==null) return false;
		else return true;
	}

}
