package net.metarelate.terminology.commandline;

import java.util.ArrayList;
import java.util.Arrays;

import com.hp.hpl.jena.rdf.model.Model;

import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.modelBuilders.TerminologyModelBuilder;
import net.metarelate.terminology.utils.SSLogger;

public class CommandIngest extends TsCommand {
	private boolean labelsOnly=false;
	boolean updateMode=false;
	private ArrayList<String> files=new ArrayList<String>();
	public CommandIngest(Initializer myInitializer,String[] args ) {
		super(myInitializer,args);
		boolean nextAreFiles=false;
		
		for(String arg:args) if(arg.equals("help")) {
			localHelp();
			return;
		}
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
		
		// TODO if index of -l (label only): gets only the labels
		// TODO -m "" this is the message
		// TODO collect files
	}
	
	@Override
	public void localExecute() {
		
		System.out.println("Starting execution");
		System.out.print("Debug mode is: ");
		System.out.println(debugOn);
		if(debugOn) System.out.println("On");
		else System.out.println("Off");
		System.out.println("Message: "+message);
		if(labelsOnly) System.out.println("Only fetching labels");
		System.out.println("Going to read files:");
		for (String file:files) {
			System.out.println("\t"+file);
		}
		
		try {
			TerminologyModelBuilder builder=new TerminologyModelBuilder(myInitializer);
			//builder.setGlobalOwnerURI(myInitializer.getDefaultUserURI());
			builder.setActionMessage(message);
			Model globalInput=readIntoModel(files);
			
			// TODO change builder to accommodate these....
			if(labelsOnly) {
				//builder.setGlobalConfigurationModel(globalInput);
				builder.registerInput(globalInput);
				myInitializer.myFactory.getLabelManager().registerLabels(builder.getLabels());
					
			}
			else {
					builder.generateModel(globalInput,updateMode,message);
			}
				
				
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problems in creating model from rdf files");
		}	
		
		
		try {
			SSLogger.log("Number of known sets "+myInitializer.myFactory.getAllSets().size(),SSLogger.DEBUG);
			SSLogger.log("Number of known individuals "+myInitializer.myFactory.getAllIndividuals().size(),SSLogger.DEBUG);
		} catch (ModelException e) {
			SSLogger.log("Problems in message, unable to collect stats",SSLogger.DEBUG);
			e.printStackTrace();
		}
		

	}
	
	@Override
	public void localHelp() {
		System.out.println("ts ingest [-lo | -labelsOnly] [-u | -update] -f|-files LIST_OF_FILES");
		System.out.println("builds a model from the corresponding input files");
		System.out.println("if labelsOnly is set, it only imports labels from the specified files");
		System.out.println("if update is set, the system create a new version of a code/set if the entity is versioned, no version is specified and some statements change");
		System.out.println("Note that configuration parameters should be set in configuration files, not in the files to be imported");
	
		
	}

	

}
