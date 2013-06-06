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
 * TODO check if System.out is coherently used vs Logger 
 * (also across all comand line tools).
 * TODO add help
 */
package net.metarelate.terminology.commandline;

import java.io.File;
import java.util.ArrayList;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.PublisherManager;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Model;

public class CommandPublish extends TsCommand {
	boolean overwrite=false;				//default
	boolean cleanCache=false;				//default
	String selectedURI=null;
	String template="plain";				//default
	int port=CoreConfig.DEFAULT_PUBLISH_PORT;						//default
	String language="en";				//TODO default settings should be factorized
	String tag=null;
	String outFile=null;
	int mode=PublisherManager.WEB_FILES; 	//default
	private ArrayList<String> files=new ArrayList<String>();
	public CommandPublish(String sysDir,String[] args) {
		super(sysDir,args);
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
		Loggers.commandLogger.info("Publishing resources");
		if(mode==PublisherManager.WEB_FILES) Loggers.commandLogger.debug("Web file with template "+template);
		if(mode==PublisherManager.ONLINE) Loggers.commandLogger.debug("Online file with template "+template+" on port "+port);
		if(mode==PublisherManager.DOC_FILE) Loggers.commandLogger.debug("Doc files with template "+template);
		if(selectedURI==null) Loggers.commandLogger.info("Publishing all");
		else Loggers.commandLogger.info("Publishing "+selectedURI);
		Loggers.commandLogger.debug("Overwrite: "+overwrite);
		Loggers.commandLogger.debug("Cleancache: "+cleanCache);
		if(files.size()>0) {
			for(String file:files) Loggers.commandLogger.debug("Extra configuration in file: "+file);
		}
		Model globalInput=readIntoModel(files);
		
		// We always need templates
		File templateFile=new File(myInitializer.getTemplatesDirectory(),template);
		if(templateFile.isDirectory())
			myInitializer.myPublisherManager.setTemplateLocation(templateFile.getAbsolutePath());
		else if((new File(template)).isDirectory())
			myInitializer.myPublisherManager.setTemplateLocation(template);
		else throw new ConfigurationException("Invalid template specified: "+template);
		if(mode==PublisherManager.WEB_FILES || mode==PublisherManager.ONLINE) {
			if(cleanCache) myInitializer.myPublisherManager.cleanCache();
		}
		
		if(mode==PublisherManager.WEB_FILES) {
			if(selectedURI!=null) myInitializer.myPublisherManager.publishWebFiles(selectedURI,globalInput,overwrite);
			else {
				TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
				if(roots==null) {
					System.out.println("Cannot finde roots! (something went wrong...)");
					System.exit(0);
				}
				for(TerminologySet root: roots) myInitializer.myPublisherManager.publishWebFiles(root.getURI(),globalInput,overwrite);
			}
		}
		if(mode==PublisherManager.DOC_FILE)	{
			myInitializer.myPublisherManager.publishDoc(tag, language, outFile);
		}
		if(mode==PublisherManager.ONLINE) {
			myInitializer.myPublisherManager.publishOnline(globalInput,port);
		}
		
		
			
		
	}
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	public static String getStaticLocalHelpMessage() {
		// TODO missing local help
		return 	"Several usage of ts publish are possible:\n" +
				"(1) ts [-d|-t] publish -web [-ow|-overwrite] [-cc|-clean] [-uri rootURI] [-template templateName]\n" +
				"(2) ts [-d|-t] publish -online [-cc|-clean] [-port|-p portNo] [-template templateName]\n" +
				"(3) ts [-d|-t] publish -doc -tag tag [-lang language] -out fileName [-template templateName]\n" +
				"(1) issues the publication of codes as a set of files to be served by the web-server (Apache content negotation)\n" +
				"(2) opens an server publhing the terminology online\n" +
				"(3) publish the terminology as a latex/pdf document\n" +
				"Parameters are interpreted as follows:\n" +
				"[-d|-t] : debug level\n" +
				"[-web|-online|-doc] : publishing modality\n" +
				"[-cc|-clean] : force cleaning the whole URL/URI associations\n" +
				"[-URI] : publish the (sub) terminology from a given (sub) register uri only\n" +
				"[-ow|-overwrite] : force file overwriting\n" +
				"-template templateName : the template to be used for rendering (in the template dir). Note: defaults to \"plain\", (html), that may not work in your case and doesn't work for docs!!!\n" +
				"[-port|-p portNo] : the port number where to open the server (defaults to "+CoreConfig.DEFAULT_PUBLISH_PORT +")\n" +
				"-tag tag : the tag release to be published as a doc\n" +
				"[-lang language] : the language the doc should be published in (rdf encoding). Defaults to the system default language("+CoreConfig.DEFAULT_LANGUAGE+")\n" +
				"-out fileName : the file where the doc should be written to (absolute path)\n";
	
		
	}

	@Override
	public boolean validate() {
		// TODO to complete with a check of which parameters are needed for each modality
		// e.g. templates in doc...
		if(mode==PublisherManager.DOC_FILE)	{
			if(tag==null) return false;
			if(outFile==null) return false;
		}
		
		return true;
	}

	@Override
	public String getName() {
		return "publish";
	}



}
