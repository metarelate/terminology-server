package net.metarelate.terminology.commandline;

import java.util.ArrayList;
import java.util.Arrays;

import com.hp.hpl.jena.rdf.model.Model;

import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.modelBuilders.TerminologyModelBuilderFromRDF;
import net.metarelate.terminology.utils.SSLogger;

public class CommandIngest extends TsCommand {
	private boolean labelsOnly=false;
	private ArrayList<String> files=new ArrayList<String>();
	public CommandIngest(Initializer myInitializer,String[] args ) {
		super(myInitializer,args);
		boolean nextAreFiles=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-lo") || arg.equalsIgnoreCase("-labelsOnly")) {
				labelsOnly=true;
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
		if(debugOn) System.out.println("On");
		else System.out.println("Off");
		System.out.println("Message: "+message);
		if(labelsOnly) System.out.println("Only fetching labels");
		System.out.println("Going to read files:");
		for (String file:files) {
			System.out.println("\t"+file);
		}
		
		try {
			TerminologyModelBuilderFromRDF builder=new TerminologyModelBuilderFromRDF(myInitializer.myFactory);
			builder.setGlobalOwnerURI(myInitializer.getDefaultUserURI());
			builder.setActionMessage(message);
			Model globalInput=readIntoModel(files);
				// TODO change builder to accommodate these....
			if(labelsOnly) {
				builder.setGlobalConfigurationModel(globalInput);
				myInitializer.myFactory.getLabelManager().registerLabels(builder.getLabels());
					
			}
			else {
					builder.generateModel(globalInput);
			}
				
				
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problems in creating model from rdf files");
		}	
		
		
		SSLogger.log("Number of known sets "+myInitializer.myFactory.getAllSets().size(),SSLogger.DEBUG);
		SSLogger.log("Number of known individuals "+myInitializer.myFactory.getAllIndividuals().size(),SSLogger.DEBUG);

	}
		

	

}
