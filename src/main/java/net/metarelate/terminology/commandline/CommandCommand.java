package net.metarelate.terminology.commandline;

import net.metarelate.terminology.instanceManager.Initializer;

public class CommandCommand extends TsCommand {

	public CommandCommand(Initializer myInitializer, String[] args, boolean debug) {
		super(myInitializer, args,debug);
		for(String arg:args) if(arg.equals("help")) {
			System.out.println(getStaticLocalHelpMessage());
			return;
		}
	}

	@Override
	public void localExecute() {
		// TODO Nothing to do here yet

	}
	
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	
	public static String getStaticLocalHelpMessage() {
		return "This method is not implemented yet. Please come back later!";
		
	}

	@Override
	public boolean validate() {
		return true;
	}
	
	

}
