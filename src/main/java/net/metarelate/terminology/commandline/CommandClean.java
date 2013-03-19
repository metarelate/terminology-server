package net.metarelate.terminology.commandline;

import net.metarelate.terminology.instanceManager.Initializer;

public class CommandClean extends TsCommand {

	public CommandClean(Initializer myInitializer, String[] args) {
		super(myInitializer, args);
		for(String arg:args) if(arg.equals("help")) {
			localHelp();
			return;
		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public void localExecute() {
		// TODO Auto-generated method stub

	}

	@Override
	public void localHelp() {
		System.out.println("This method is not implemented yet. Please come back later!");
		
	}
}
