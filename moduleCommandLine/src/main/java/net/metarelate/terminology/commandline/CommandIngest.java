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

/**
 * TODO some residual from the previous code-base to be cleaned up.
 */
package net.metarelate.terminology.commandline;

import java.util.ArrayList;

import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.modelBuilders.TerminologyModelBuilder;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Command to import a terminology
 * @see TSCommand for help
 * @author andrea_splendiani
 *
 */
public class CommandIngest extends TsCommand {
	private boolean labelsOnly=false;
	boolean updateMode=false;
	private ArrayList<String> files=new ArrayList<String>();
	public CommandIngest(String sysDir,String[] args) {
		super(sysDir,args);
		boolean nextAreFiles=false;
		
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-lo") || arg.equalsIgnoreCase("-labelsOnly")) {
				labelsOnly=true;
			}
			if(arg.equalsIgnoreCase("-u") || arg.equalsIgnoreCase("-update")) {
				updateMode=true;
			}
			else if(arg.equalsIgnoreCase("-f") || arg.equalsIgnoreCase("-files")) {
				nextAreFiles=true;
			}
			else if(nextAreFiles) {
				files.add(arg);
			}
		}
		
		
	}
	
	@Override
	public void localExecute() {
		Loggers.commandLogger.debug("Starting execution");
		Loggers.commandLogger.debug("Message: "+nextIsMessage);
		//if(debugOn) System.out.println("On");
		//else System.out.println("Off");
		
		if(labelsOnly) System.out.println("Only fetching labels");
		for (String file:files) {
			Loggers.commandLogger.trace("Reading file:"+file);
		}
		
		try {
			TerminologyModelBuilder builder=new TerminologyModelBuilder(myInitializer);
			//builder.setGlobalOwnerURI(myInitializer.getDefaultUserURI());
			builder.setActionMessage(nextIsMessage);
			Model globalInput=readIntoModel(files);
			
			// TODO change builder to accommodate these....
			if(labelsOnly) {
				//builder.setGlobalConfigurationModel(globalInput);
				builder.registerInput(globalInput);
				myInitializer.myFactory.getLabelManager().registerLabels(builder.getLabels());
					
			}
			else {
					builder.generateModel(globalInput,updateMode,nextIsMessage);
			}
				
				
		} catch (Exception e) {
			e.printStackTrace();
			Loggers.commandLogger.fatal("Problems in creating model from rdf files");
		}	
		
		
		try {
			Loggers.commandLogger.debug("Number of known sets "+myInitializer.myFactory.getAllSets().size());
			Loggers.commandLogger.debug("Number of known individuals "+myInitializer.myFactory.getAllIndividuals().size());
		} catch (ModelException e) {
			Loggers.commandLogger.fatal("Problems in message, unable to collect stats");
			e.printStackTrace();
		}
		

	}
	
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	

	public static String getStaticLocalHelpMessage() {
		return 	"ts ingest [-lo | -labelsOnly] [-u | -update] -f|-files LIST_OF_FILES\n"+
				"builds a model from the corresponding input files\n"+
				"if labelsOnly is set, it only imports labels from the specified files\n"+
				"if update is set, the system create a new version of a code/set if the entity is versioned, no version is specified and some statements change\n"+
				"Note that configuration parameters should be set in configuration files, not in the files to be imported";
	}

	@Override
	public boolean validate() {
		if(files.size()==0) return false;
		else return true;
	}

	@Override
	public String getName() {
		return "ingest";
	}

	

}
