/* 
 (C) British Crown Copyright 2011 - 2012, Met Office

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

import java.util.Map;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.modelBuilders.TerminologyModelBuilderFromRDF;
import net.metarelate.terminology.publisher.WebWriter;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
/**
 * Control the execution of the mapping from RDF to the terminology model, 
 * and of the terminology model to the web representation.
 * @author andreasplendiani
 *
 */
public class MetMap extends CommandLineTool {
	private boolean fromRDF=false;
	private boolean toWeb=false;
	private boolean writeOverwrite=false;
	private boolean autoDefaults=false;
	private String globalOwnerURI=null;
	private String selectedURI=null;
	private String rootURL=null;
	private String rootPath=null;
	
	private String[] rdfFiles=null;
	private Model myConfigModel=null;
	private TerminologySet[] roots=null;
	Map<String,String>prefixMap=null;
	Model labelsModel=null;
	
	public static void main(String[] args) {
		MetMap myMetMap=new MetMap();
		myMetMap.startCommand(args);
	}
	
	@Override
	protected void parseLocal(String[] args) {
		int indexOfI=-1;
		int indexOfO=-1;
		int indexOfF=-1;
		int indexOfGO=-1;
		int indexOfP=-1;
		int indexOfRootPath=-1;
		int indexOfRootURL=-1;
		int indexOfURI=-1;
		
		//Parameters identification
		for(int i=0;i<args.length;i++) {
			if(args[i].equals("-i")) {
				indexOfI=i;
			}
			if(args[i].equals("-o")) {
				indexOfO=i;
			}
			if(args[i].equals("-uri")) {
				indexOfURI=i;
			}
			if(args[i].equals("-rootPath")) {
				indexOfRootPath=i;
			}
			if(args[i].equals("-rootURL")) {
				indexOfRootURL=i;
			}
			
			if(args[i].equals("-ow")) {
				writeOverwrite=true;
				SSLogger.log("Overwrite on",SSLogger.DEBUG);
			}
			if(args[i].equals("-go")) {
				indexOfGO=i;
			}
			
			if(args[i].equals("-auto")) {
				autoDefaults=true;
				SSLogger.log("Auto on",SSLogger.DEBUG);
			}
			
			if(args[i].equals("-p")) {
				indexOfP=i;
			}
			
			if(args[i].equals("-f")) {
				indexOfF=i;
			}
			
		}
		// At least one of -i (from RDF to model) or -o (from model to Web) should be specified.
		if(indexOfI<0 && indexOfO<0) exitWrongUsage("Nothing to do specified");	
		
		// Request to build a model from RDF
		if(indexOfI>=0) {
			fromRDF=true;
			if(!(args[indexOfI+1].equals("rdf"))) exitWrongUsage("Only RDF input is supported");
			if(indexOfF<1) exitWrongUsage("No RDF files provided");
		}
		
		// Processing -o options (output mode)
		if(indexOfO>=0) {
			toWeb=true;
			if(!(args[indexOfO+1].equals("web"))) exitWrongUsage("Only export to web is supported"); 
		}
		
		// Processing the -uri option
		if(indexOfURI>=0) {
			SSLogger.log("Export to web restricted to URI: "+args[indexOfURI+1],SSLogger.DEBUG);
			selectedURI=args[indexOfURI+1];
			if(indexOfRootURL<0 || indexOfRootPath<0) {
				exitWrongUsage("If export is specific to a URI, the base path and base url must be provided");
			}
		}
		
		if(indexOfRootPath>=0) {
			SSLogger.log("Base path: "+args[indexOfRootPath+1],SSLogger.DEBUG);
			rootPath=args[indexOfRootPath+1];
		}
		
		if(indexOfRootURL>=0) {
			SSLogger.log("Base url: "+args[indexOfRootURL+1],SSLogger.DEBUG);
			rootURL=args[indexOfRootURL+1];
		}
		
		// Processing the -go otion (owner)
		if(indexOfGO>=0) {
			SSLogger.log("Global owner: "+globalOwnerURI,SSLogger.DEBUG);
			globalOwnerURI=args[indexOfGO+1];
		}
		
		// Processing the -p option (prefix file)
		// TODO this could be made more robust and accept more syntaxes
		if(indexOfP>=0) {
			SSLogger.log("Setting prefixes from file: "+args[indexOfP+1],SSLogger.DEBUG);
			Model pModel=ModelFactory.createDefaultModel();
			try {
				pModel.read(args[indexOfP+1], "TTL");
			}
			catch (Exception e) {
				SSLogger.log("Problem in reading file: "+e.getMessage(),SSLogger.DEBUG);
				exitWrongUsage("Unable to read prefix file");	
			}
			prefixMap=pModel.getNsPrefixMap();
		}
		
		// Processing -f option (files)
		// TODO this could be made more robust and accept more syntaxes
		int noOfFiles=args.length-indexOfF-1;
		if(noOfFiles<1) {
			exitWrongUsage("No input files sepecified");
			
		}
		rdfFiles=new String[noOfFiles];
		for(int j=0;j<noOfFiles;j++) {
			rdfFiles[j]=args[j+indexOfF+1];
		}
	}
		
	@Override
	protected void executeCommand() {
		//This is a two step method, first the terminology model is constructed,
		//then the web layout is generated.
		//Only on or both of these steps can be performed, depending on the command.
		SSLogger.log("Using tdb model at "+tdbLocation,SSLogger.DEBUG);
		SSLogger.log("Planned operation:",SSLogger.DEBUG);
		if(fromRDF==true) SSLogger.log("Building terminology model",SSLogger.DEBUG);
		if(toWeb==true) SSLogger.log("Writing web layout",SSLogger.DEBUG);
		SSLogger.log("Input files ("+rdfFiles.length+"):",SSLogger.DEBUG);
		for(int j=0;j<rdfFiles.length;j++) {
			SSLogger.log(" - "+rdfFiles[j],SSLogger.DEBUG);
		}
		
		//We merge here all configuration files
		myConfigModel=ModelFactory.createDefaultModel();
		try {
			for(int j=0;j<rdfFiles.length;j++) {
				String suffix=rdfFiles[j].substring(rdfFiles[j].lastIndexOf('.')+1);
				if(suffix.equalsIgnoreCase("rdf")) myConfigModel.read(rdfFiles[j]);
				else if (suffix.equalsIgnoreCase("ttl")) myConfigModel.read(rdfFiles[j],"TTL");
				else {
					exitWrongUsage("Don't know which parser to use for :"+rdfFiles[j]);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			SSLogger.log("Problems in reading rdf files",SSLogger.DEBUG);
		}
		
		//First step
		if(fromRDF==true) {	
			System.out.println("******************************************");
			System.out.println("             MODEL BUILDING               ");
			System.out.println("******************************************");
			SSLogger.log("ModelFactory knows "+myFactory.getAllSets().size()+ " sets",SSLogger.DEBUG);
			SSLogger.log("ModelFactory knows "+myFactory.getAllIndividuals().size()+ " individuals",SSLogger.DEBUG);
			try {
				TerminologyModelBuilderFromRDF builder;
				builder=new TerminologyModelBuilderFromRDF(myFactory);
				if(autoDefaults==true) builder.setAutodefaults(true);
				if(globalOwnerURI!=null) builder.setGlobalOwnerURI(globalOwnerURI);
				builder.generateModel(myConfigModel);
				
				// TODO the following two statements should be obsoleted by the refactoring of ModelBuilder
				if (prefixMap==null) prefixMap=builder.getPrefixes();
				labelsModel=builder.getLabels();
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Problems in creating model from rdf files");
			}	
		}
		SSLogger.log("Number of known sets "+myFactory.getAllSets().size(),SSLogger.DEBUG);
		SSLogger.log("Number of known individuals "+myFactory.getAllIndividuals().size(),SSLogger.DEBUG);
		
	
		//Step two
		if(toWeb==true) {
			System.out.println("******************************************");
			System.out.println("              MODEL WRITING               ");
			System.out.println("******************************************");
			SSLogger.log("ModelFactory knows "+myFactory.getAllSets().size()+ " sets",SSLogger.DEBUG);
			SSLogger.log("ModelFactory knows "+myFactory.getAllIndividuals().size()+ " individuals",SSLogger.DEBUG);

			roots=myFactory.getRootCollections();
			if(roots==null) {
				System.out.println("Cannot finde roots! (something went wrong...)");
				System.exit(0);
			}
			System.out.println("Roots to be processed: ");
			for(int i=0;i<roots.length;i++) {
				SSLogger.log("root: "+roots[i].getURI());
			}
		
		
			if(selectedURI==null) {
				SSLogger.log("No filters");
				for(int i=0;i<roots.length;i++) {
					try {
						SSLogger.log("Generating web layout for: "+roots[i].getURI());
						WebWriter myWriter=new WebWriter(roots[i],myConfigModel,writeOverwrite);
						myWriter.setPrefixMap(prefixMap);		// TODO verify consistency with publisher/builder/factory
						myWriter.setLabelModel(labelsModel);	// TODO verify consistency with publisher/builder/factory
						myWriter.write();
						
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Problems in writing to web");
					}
				}
			}
			else {
				SSLogger.log("Filter on: "+selectedURI);
				for(int i=0;i<roots.length;i++) {
					System.out.println("+"+selectedURI+"+"+selectedURI.length());
					System.out.println("+"+roots[i].getURI()+"+"+roots[i].getURI().length());
					if(selectedURI.equals(roots[i].getURI()))  {
						try {
							SSLogger.log("Generating web layout for: "+roots[i].getURI());
							WebWriter myWriter=new WebWriter(roots[i],myConfigModel,writeOverwrite);
							myWriter.setPrefixMap(prefixMap);		// TODO verify consistency with publisher/builder/factory
							myWriter.setLabelModel(labelsModel);	// TODO verify consistency with publisher/builder/factory
							myWriter.write(rootPath,rootURL);
						} catch (Exception e) {
							e.printStackTrace();
							System.out.println("Problems in writing to web");
						}
					}
				}
			}
			
		}
	}

	
	

	@Override
	protected void printStartMessage() {
		System.out.println("MetMap v."+CoreConfig.VERSION_NUMBER+" \""+CoreConfig.VERSION_CODENAME+"\"");
		System.out.println("Generic Table to RDF/Web mapping and term management toolkit");	
	}
	
	@Override
	protected void printLocalHelp() {
		System.out.println("MetMap [-ow] -i [rdf|tdb-named] -o [tdb|web] -f list of files (.rdf or .ttl)");
		System.out.println("Builds a in memory representation of a code/terminology set from the source specified with -i and export it to the target specified with -o");
		System.out.println("-ow: overwrites a current layout. Issues error if this is not set and files already exist");
		System.out.println("(files are specified by their URL)");
		
		System.out.println("-i:\tBuilds a model from a specified input (only the \"rdf\" option is implemented)");
		System.out.println("\te.g: MetMap -i rdf -f list_rdf_input_files");
		System.out.println("-o:\tExport the content of a terminology model (only the \"web\" option is implemented)");
		System.out.println("-uri:\tRestricts the export to a specified URI \"root\".");
		System.out.println("-uri:\te.g.: if the uri for a register is specified,\n" +
				"\tall the elements in the register and its sub-registers will be mapped.");
		System.out.println("\tWhen this option is selected, baseURL and basePath should be provided.");
		System.out.println("-rootPath:\tThe root directory well the files for the web export should be written");
		System.out.println("-rootURL:\tThe base URL for all web-published elements");
		System.out.println("-ow:\tOverwrites files and directories during the web-export");
		System.out.println("\twhen not set, MetMap will stop if an overwrite attempt is done.");
		System.out.println("-go:\tSets the \"global owner\"\n" +
				"\t(All elements defined in this import operations will be associated to the specified owner.\n" +
				"\t\"owner\" is not intended in the iso19135 sense, but in the sense of whom has authority to ope");
		System.out.println("\te.g.: MetMap -i rdf -go http://metffice.gov.uk -f list_of_files");
		// TODO What if a term was already created ? Permissions shouldn't be changed. To verify.
		System.out.println("-auto:\tAutomatically set creation date to the current date, and action as \"import\"");
		// TODO other automatic sets ? To verify.
		System.out.println("-p:\tA file specifying prefixes to be used in the system");
		System.out.println("\tthe content of the file is not retained. If it must be read\n" +
				"it should be specified again under the f option.\n" +
				"This file must be written in Turtle and end with a .ttl extension.");

		System.out.println("-f:\tA list of files in input to the terminology mapping process.");
		System.out.println("-f:\tThis option should always appear last.\n" +
				"Files must be in rdf/xml or turtle and end respectively with a .ttl or .rdf extension");
		System.out.println("Examples:");
		System.out.println("MetMap -i rdf -go http://metoffice.gov.uk -auto -f d2rqout.rdf reasoningspecs.rdf extralabels.rdf");
		System.out.println("MetMap -o web -p gneralconf.rdf");
		System.out.println("MetMap -o web -uri http://reference.metoffice.gov/data/wmo/tdcf/b -baseURI http://reference.metoffice.gov/data/wmo/tdcf/ -basePath /var/local/www/data/wmo/tdcf/ -p gneralconf.rdf");
		// TODO are "/" required ?
		
	}


}
