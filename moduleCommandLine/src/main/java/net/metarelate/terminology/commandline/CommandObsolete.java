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
				if(parents.size()!=1) throw new ModelException(line+" has not one parent, it has : "+parents.size());
				TerminologySet register=parents.iterator().next();
				myInitializer.myTerminologyManager.delTermFromRegister(termToDelete.getURI(), register.getURI(), myInitializer.getDefaultUserURI(), message);
			}
			else if(myInitializer.myFactory.terminologySetExist(line)) {
				TerminologySet setToDelete=myInitializer.myFactory.getUncheckedTerminologySet(line);
				myInitializer.myTerminologyManager.delTerm(setToDelete.getURI(), myInitializer.getDefaultUserURI(), message);
			}
			else throw new UnknownURIException(line); 
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
		return "Nothing yet"; // TODO to fill!
	}

	@Override
	public String getName() {
		return "Obsolete";
	}

}
