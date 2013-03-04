package net.metarelate.terminology.commandline;

import java.util.ArrayList;

import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.publisher.WebWriter;
import net.metarelate.terminology.utils.SSLogger;

public class CommandPublish extends TsCommand {
	boolean overwrite=false;
	String selectedURI=null;
	String rootPath=null;
	public CommandPublish(Initializer myInitializer,String[] args) {
		super(myInitializer,args);
		boolean nextIsURI=false;
		boolean nextIsRootPath=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-ow") || arg.equalsIgnoreCase("-overwrite")) {
				overwrite=true;
			}
			else if(arg.equalsIgnoreCase("-uri") || arg.equalsIgnoreCase("-entity")) {
				overwrite=true;
			}
			else if(arg.equalsIgnoreCase("-root") || arg.equalsIgnoreCase("-entityRoot")) {
				nextIsRootPath=true;
			}
			else if(nextIsURI==true) {
				selectedURI=arg;
				nextIsURI=false;
			}
			else if(nextIsRootPath==true) {
				rootPath=arg;
				nextIsRootPath=false;
			}
			
		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public void localExecute() {
		//SSLogger.log("ModelFactory knows "+myInitializer.myFactory.getAllSets().size()+ " sets",SSLogger.DEBUG);
		//SSLogger.log("ModelFactory knows "+myInitializer.myFactory.getAllIndividuals().size()+ " individuals",SSLogger.DEBUG);
		TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
		if(roots==null) {
			System.out.println("Cannot finde roots! (something went wrong...)");
			System.exit(0);
		}
		if(selectedURI==null) {
			SSLogger.log("No filters");
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
		}
		else {
			SSLogger.log("Filter on: "+selectedURI);
			for(TerminologySet root:roots) {
				System.out.println("+"+selectedURI+"+"+selectedURI.length());
				System.out.println("+"+root.getURI()+"+"+root.getURI().length());
				if(selectedURI.equals(root.getURI()))  {
					try {
						SSLogger.log("Generating web layout for: "+root.getURI());
						WebWriter myWriter=new WebWriter(root,myInitializer.getConfigurationGraph(),overwrite);
						myWriter.setPrefixMap(myInitializer.getPrefixMap());		// TODO verify consistency with publisher/builder/factory
						myWriter.write(rootPath,selectedURI);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Problems in writing to web");
					}
				}
			}
		}
			
		
	}

	

}