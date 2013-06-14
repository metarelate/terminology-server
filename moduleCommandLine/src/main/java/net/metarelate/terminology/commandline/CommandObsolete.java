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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ImpossibleOperationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.utils.Loggers;

/**
 * Command to perform a batch obsolete
 * @see TSCommand for help
 * @author andrea_splendiani
 *
 */
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
