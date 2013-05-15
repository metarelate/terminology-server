package net.metarelate.terminology.commandline;

import java.io.IOException;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;

import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.publisher.PublisherManager;
import net.metarelate.terminology.publisher.WebWriter;
import net.metarelate.terminology.utils.SSLogger;

public class CommandPublish extends TsCommand {
	boolean overwrite=false;				//default
	boolean cleanCache=false;				//default
	String selectedURI=null;
	String template="plain";				//default
	int port=80;						//default
	String language="en";				//TODO default settings should be factorized
	String tag=null;
	String outFile=null;
	int mode=PublisherManager.WEB_FILES; 	//default
	private ArrayList<String> files=new ArrayList<String>();
	public CommandPublish(Initializer myInitializer,String[] args, boolean debug) {
		super(myInitializer,args,debug);
		boolean nextIsURI=false;
		boolean nextIsTemplate=false;
		boolean nextIsPort=false;
		boolean nextAreFiles=false;
		boolean nextIsTag=false;
		boolean nextIsOutFile=false;
		boolean nextIsLanguage=false;
		
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-ow") || arg.equalsIgnoreCase("-overwrite")) {
				overwrite=true;
			}
			else if(arg.equalsIgnoreCase("-clean") || arg.equalsIgnoreCase("-cc")) {
				cleanCache=true;
			}
			else if(arg.equalsIgnoreCase("-uri") || arg.equalsIgnoreCase("-entity")) {
				nextIsURI=true;
			}
			else if(arg.equalsIgnoreCase("-template") || arg.equalsIgnoreCase("-t")) {
				nextIsTemplate=true;
			}
			else if(arg.equalsIgnoreCase("-port") || arg.equalsIgnoreCase("-p")) {
				nextIsPort=true;
			}
			else if(arg.equalsIgnoreCase("-tag") || arg.equalsIgnoreCase("-t")) {
				nextIsTag=true;
			}
			else if(arg.equalsIgnoreCase("-out") || arg.equalsIgnoreCase("-o")) {
				nextIsOutFile=true;
			}
			else if(arg.equalsIgnoreCase("-lang") || arg.equalsIgnoreCase("-l")) {
				nextIsLanguage=true;
			}
			else if(arg.equalsIgnoreCase("-online") ) {
				mode=PublisherManager.ONLINE;
			}
			else if(arg.equalsIgnoreCase("-doc") ) {
				mode=PublisherManager.DOC_FILE;
			}
			else if(arg.equalsIgnoreCase("-web") ) {
				mode=PublisherManager.WEB_FILES;
			}
			else if(arg.equalsIgnoreCase("-f") || arg.equalsIgnoreCase("-files")) {
				nextAreFiles=true;
			}
			else if(nextAreFiles) {
				files.add(arg);
			}
			else if(nextIsURI==true) {
				selectedURI=arg;
				nextIsURI=false;
			}
			else if(nextIsTemplate==true) {
				template=arg;
				nextIsTemplate=false;
			}
			else if(nextIsPort==true) {
				port=Integer.parseInt(arg);
				nextIsPort=false;
			}
			else if(nextIsTag==true) {
				tag=arg;
				nextIsTag=false;
			}
			else if(nextIsOutFile==true) {
				outFile=arg;
				nextIsOutFile=false;
			}
			else if(nextIsLanguage==true) {
				language=arg;
				nextIsLanguage=false;
			}
			
		}
	}

	@Override
	public void localExecute() throws Exception {
		SSLogger.log("Publishing resources",SSLogger.DEBUG);
		SSLogger.log("Mode is: "+mode,SSLogger.DEBUG);
		if(mode==PublisherManager.WEB_FILES) SSLogger.log("Web file with template "+template,SSLogger.DEBUG);
		if(mode==PublisherManager.ONLINE) SSLogger.log("Online file with template "+template+" on port "+port,SSLogger.DEBUG);
		if(mode==PublisherManager.DOC_FILE) SSLogger.log("Doc files with template "+template,SSLogger.DEBUG);
		if(selectedURI==null) SSLogger.log("Publishing all",SSLogger.DEBUG);
		else SSLogger.log("Publishing "+selectedURI,SSLogger.DEBUG);
		SSLogger.log("Overwrite: "+overwrite,SSLogger.DEBUG);
		SSLogger.log("Cleancache: "+cleanCache,SSLogger.DEBUG);
		if(files.size()>0) {
			SSLogger.log("Extra configuration supplied :",SSLogger.DEBUG);
			for(String file:files) SSLogger.log("File: "+file,SSLogger.DEBUG);
		}
		Model globalInput=readIntoModel(files);
		
		// We always need templates
		myInitializer.myPublisherManager.setTemplateLocation(template);
		
		if(mode==PublisherManager.WEB_FILES || mode==PublisherManager.ONLINE) {
			if(cleanCache) myInitializer.myPublisherManager.cleanCache();
		}
		
		if(mode==PublisherManager.WEB_FILES) {
			if(selectedURI!=null) myInitializer.myPublisherManager.publishWebFiles(selectedURI,globalInput);
			else {
				TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
				if(roots==null) {
					System.out.println("Cannot finde roots! (something went wrong...)");
					System.exit(0);
				}
				for(TerminologySet root: roots) myInitializer.myPublisherManager.publishWebFiles(root.getURI(),globalInput);
			}
		}
		if(mode==PublisherManager.DOC_FILE)	{
			myInitializer.myPublisherManager.publishDoc(tag, language, outFile);
		}
		if(mode==PublisherManager.ONLINE) {
			myInitializer.myPublisherManager.publishOnline(globalInput,port);
		}
		
		/**
		 * TODO only a basic total publishing is implemented in the current model
		 * (inherited from past design). All is going to change with the new publisher design.
		 */
		/*
		TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
		if(roots==null) {
			System.out.println("Cannot finde roots! (something went wrong...)");
			System.exit(0);
		}
		//if(selectedURI==null) {
		//SSLogger.log("No filters");
			for(TerminologySet root: roots) {
				try {
					SSLogger.log("Generating web layout for: "+root.getURI());
					WebWriter myWriter=new WebWriter(root,myInitializer.getConfigurationGraph(),overwrite);
					myWriter.setPrefixMap(myInitializer.getPrefixMap());		// TODO verify consistency with publisher/builder/factory
					myWriter.write();
						
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Problems in writing to web");
				}
			}
		//}
		 
		 */
		
			
		
	}
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	public static String getStaticLocalHelpMessage() {
		return "ts publish [under refactoring!]";
	
		
	}

	@Override
	public boolean validate() {
		// TODO to complete with a check of which parameters are needed for each modality
		if(mode==PublisherManager.DOC_FILE)	{
			if(tag==null) return false;
			if(outFile==null) return false;
		}
		return true;
	}



}
