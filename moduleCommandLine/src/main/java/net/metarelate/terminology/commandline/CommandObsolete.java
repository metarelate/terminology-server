package net.metarelate.terminology.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ImpossibleOperationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.utils.Loggers;

public class CommandObsolete extends TsCommand {
	private String listFile=null;
	public CommandObsolete(String sysDir, String[] args) {
		super(sysDir, args);
		Loggers.commandLogger.trace("Command obsolete");
		boolean nextIsList=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-list")) {
				nextIsList=true;
			}
			else if(nextIsList==true) {
				nextIsList=false;
				listFile=arg;
			}
		}
	}

	@Override
	public void localExecute() throws ModelException, UnknownURIException,
			ConfigurationException, WebWriterException, IOException, Exception {
		File inputFile=new File(listFile);
		BufferedReader myReader=new BufferedReader(new FileReader(inputFile));
		String line=myReader.readLine();
		String message="Obseleted via command line";
		while(line!=null) {
			Loggers.commandLogger.debug("Read URI to obsolete: >>"+line+"<<");
			if(myInitializer.myFactory.terminologyIndividualExist(line)) {
				TerminologyIndividual termToDelete=myInitializer.myFactory.getUncheckedTerminologyIndividual(line);
				Set<TerminologySet> parents=termToDelete.getContainers();
				if(parents.size()>1) throw new ModelException(line+" is in more than one register, it is in : "+parents.size());
				if(parents.size()==0) throw new ImpossibleOperationException("I cannot obsolete "+line+" as it's not in any register");

				TerminologySet register=parents.iterator().next();
				myInitializer.myTerminologyManager.delTermFromRegister(termToDelete.getURI(), register.getURI(), myInitializer.getDefaultUserURI(), message);
				Loggers.commandLogger.trace("Command succesfully executed");
			}
			else if(myInitializer.myFactory.terminologySetExist(line)) {
				TerminologySet setToDelete=myInitializer.myFactory.getUncheckedTerminologySet(line);
				myInitializer.myTerminologyManager.delTerm(setToDelete.getURI(), myInitializer.getDefaultUserURI(), message);
				Loggers.commandLogger.trace("Command succesfully executed");
			}
			else {
				Loggers.commandLogger.trace("Nothing happened");
				throw new UnknownURIException(line); 
			}
			line=myReader.readLine();
		}
	}

	@Override
	public boolean validate() {
		if(listFile==null) return false;
		if(!(new File(listFile)).exists()) return false;
		return true;
	}

	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}

	@Override
	public String getName() {
		return "Obsolete";
	}

	public static String getStaticLocalHelpMessage() {
		return "ts -list listOfObsoleteURL\n" +
				"listOfObsolete is a file containing one URI to be obsolete per line.\n" +
				"The action is considered performed from the default user.\n" +
				"The configured authorization and process consraints are applied."; // TODO to fill!
	}

}
