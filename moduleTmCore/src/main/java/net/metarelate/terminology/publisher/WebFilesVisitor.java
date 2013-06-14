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

package net.metarelate.terminology.publisher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Model;
/**
 * Visits a terminology to provide a file based representation of terms and registers.
 * (visits also versions)
 * @author andreasplendiani
 *
 */
public class WebFilesVisitor extends PublisherVisitor {
	TemplateManager tm=null;
	Initializer myInitializer=null;
	boolean overwriteFiles=false;
	private String registryBaseURL=null;
	public WebFilesVisitor(Initializer initializer,TemplateManager myTm, String baseURL) {
		tm=myTm;
		myInitializer=initializer;
		this.registryBaseURL=baseURL;
	}
	public void setOverwriteFiles(boolean ow) {
		overwriteFiles=ow;
	}
	
	/**
	 * crqwl the tree (recursive)
	 * @param root
	 * @throws WebWriterException
	 * @throws ModelException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public void crawl(TerminologySet root) throws WebWriterException, ModelException, IOException, ConfigurationException {
		root.accept(this);
		Set<TerminologyIndividual> inds=root.getAllKnownContainedInviduals();
		for(TerminologyIndividual ind:inds) ind.accept(this);
		Set<TerminologySet> sets=root.getAllKnownContainedCollections();
		for(TerminologySet set:sets) crawl(set);
	}

	
	public void visit (TerminologySet set) throws WebWriterException, IOException, ConfigurationException, ModelException {
		String type=PublisherConfig.setStemString;
		String version=set.getLastVersion();
		String collectionDirectoryPath=myInitializer.myCache.getValueFor(set.getURI(), PublisherConfig.uriHasDisk);
		String collectionBaseURL=myInitializer.myCache.getValueFor(set.getURI(), PublisherConfig.uriHasUrl);
		if(collectionDirectoryPath==null || collectionBaseURL==null) throw new WebWriterException("Unable to find disk or base URL for "+set.getURI());
		makeFiles(set,type,collectionDirectoryPath,collectionBaseURL,version);
		if(set.isVersioned()) {
			String[] versions=set.getVersions();
			for(String subVersion:versions) {
				String basePath=collectionDirectoryPath+"/"+subVersion+"/";
				String baseURL=collectionBaseURL+"/"+subVersion+"/";
				makeFiles(set,type,basePath,baseURL,subVersion);
			}
		}
	}
	
	public void visit(TerminologyIndividual ind) throws WebWriterException, IOException, ConfigurationException, ModelException {
		String type=PublisherConfig.individualStemString; //TODO better name for stems (they are sort of types...)
		String version=ind.getLastVersion();
		String individualDirectoryPath=myInitializer.myCache.getValueFor(ind.getURI(), PublisherConfig.uriHasDisk);
		String indidvidualURL=myInitializer.myCache.getValueFor(ind.getURI(), PublisherConfig.uriHasUrl);
		if(individualDirectoryPath==null || indidvidualURL==null) throw new WebWriterException("Unable to find disk or base URL for "+ind.getURI());
		makeFiles(ind,type,individualDirectoryPath,indidvidualURL,version);
		if(ind.isVersioned()) {
			String[] versions=ind.getVersions();
			for(String subVersion:versions) {
				String basePath=individualDirectoryPath+"/"+subVersion+"/";
				String baseURL=indidvidualURL+"/"+subVersion+"/";
				makeFiles(ind,type,basePath,baseURL,subVersion);
			}
		}
	}
	
	
	private void makeFiles(TerminologyEntity entity, String type, String collectionDirectoryPath, String collectionBaseURL, String version) throws IOException, WebWriterException, ConfigurationException, ModelException {
		Loggers.publishLogger.debug(">>Writing "+entity.getURI()+" to "+collectionDirectoryPath);
		
		String[] languages=tm.getLanguages();
		String[] languageFilesPaths=new String[languages.length];
		String[] languageFilesLinks=new String[languages.length];
		int i=0;
		for(String language:languages) {
			languageFilesPaths[i]=collectionDirectoryPath+"/"+type+"."+language+".html";
			languageFilesLinks[i]=collectionBaseURL+"/"+type+"."+language+".html";
			i++;
		}
		
		String registerVarFile=collectionDirectoryPath+"/"+type+".var";
		
		String registerRDFFile=collectionDirectoryPath+"/"+type+".rdf"; 	// file (rdf)
		String registerRDFLink=collectionBaseURL+"/"+type+".rdf";		// url (rdf)
		
		String registerTTLFile=collectionDirectoryPath+"/"+type+".ttl"; 	// file (rdf)
		String registerTTLLink=collectionBaseURL+"/"+type+".ttl";		// url (ttl)
		
		String registerQRImageFile=collectionDirectoryPath+"/"+type+".gif";	//QR image
		String registerQRImageLink=collectionDirectoryPath+"/"+type+".gif";
		
		Loggers.publishLogger.trace("version "+version);
		
		File directory=myCheckedMkDir(collectionDirectoryPath,overwriteFiles);
		
		writeToFile(registerVarFile,makeContentNegotiationFile(type,languages));
		IDrenderer.writeToFile(registerQRImageFile,entity.getURI());
	
		/*
		SortedMap<String,String> stdMap=new TreeMap<String,String>();
		stdMap.put("RDF/XML",registerRDFLink);
		stdMap.put("Turtle",registerTTLLink);
		*/
		
		//TODO a bit ugly, but on the other hand this is a private method to factorize a bit of procedural code
		for(i=0;i<languages.length;i++) {
			if(entity.isSet()) writeToFile(languageFilesPaths[i],tm.getPageForLang(languages[i],(TerminologySet)entity,version,0,collectionBaseURL,myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(),registryBaseURL,null)); //TODO we may need to pass more infos to the template!
			if(entity.isIndividual()) writeToFile(languageFilesPaths[i],tm.getPageForLang(languages[i],(TerminologyIndividual)entity,version,0,collectionBaseURL,myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(),registryBaseURL,null));
		}
		
		Model modelToWrite=RDFrenderer.prepareModel(entity, version);
		modelToWrite.setNsPrefixes(myInitializer.getPrefixMap());
		FileOutputStream myOutRDF=	 new FileOutputStream(registerRDFFile);
		FileOutputStream myOutTTL=	 new FileOutputStream(registerTTLFile);
		modelToWrite.write(myOutRDF);
		modelToWrite.write(myOutTTL,"TTL");
		
	
		

	}


	private void writeToFile(String fileName, String content) throws IOException {
		FileWriter fWriter = new FileWriter(fileName);
		BufferedWriter fBWriter = new BufferedWriter(fWriter);
		fBWriter.write(content);
		fBWriter.close();
	}
	
	private File myCheckedMkDir(String directortyPath, boolean canOvewrite) throws WebWriterException {
		File directory=new File(directortyPath);
		if(directory.isDirectory() && directory.exists()) {
			if(!canOvewrite) throw new WebWriterException("Cannot overwrite directory "+directortyPath+" check the -ow option is this is the intended behaviour");
		}
		else {
			boolean success = directory.mkdir();
			if (success) 
				Loggers.publishLogger.debug("Created directory " + directortyPath);
			else throw new WebWriterException("Unable to create directory "+directortyPath);
		}
		return directory;
		
	}

	private String makeContentNegotiationFile(String type, String[] languages) {
		StringBuilder st=new StringBuilder();
		st.append("URI: "+type+"\n\n");
		for(String language:languages) {
			st.append("URI: "+type+"."+language+".html\n" +
					"Content-type: text/html\n" +
					"Content-language: "+language+"\n\n");
		}
		st.append("URI: "+type+".rdf\n" +
				"Content-type: application/rdf+xml\n\n" +
				"URI: "+type+".ttl\n" +
				"Content-type: text/turtle\n");
		return st.toString();
	}

	

}
