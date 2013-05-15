package net.metarelate.terminology.publisher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SSLogger;

public class WebFilesVisitor extends PublisherVisitor {
	TemplateManager tm=null;
	Initializer myInitializer=null;
	boolean overwriteFiles=false;
	public WebFilesVisitor(Initializer initializer,TemplateManager myTm) {
		tm=myTm;
		myInitializer=initializer;
	}
	public void setOverwriteFiles(boolean ow) {
		overwriteFiles=ow;
	}
	

	public void crawl(TerminologySet root) throws WebWriterException, ModelException, IOException, ConfigurationException {
		root.accept(this);
		Set<TerminologyIndividual> inds=root.getAllKnownContainedInviduals();
		for(TerminologyIndividual ind:inds) ind.accept(this);
		Set<TerminologySet> sets=root.getAllKnownContainedCollections();
		for(TerminologySet set:sets) crawl(set);
	}

	
	public void visit (TerminologySet set) throws WebWriterException, IOException, ConfigurationException {
		String type="register";
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
	
	public void visit(TerminologyIndividual ind) throws WebWriterException, IOException, ConfigurationException {
		String type="code";
		String version=ind.getLastVersion();
		String individualDirectoryPath=myInitializer.myCache.getValueFor(ind.getURI(), PublisherConfig.uriHasDisk);
		String indidvidualBaseURL=myInitializer.myCache.getValueFor(ind.getURI(), PublisherConfig.uriHasUrl);
		if(individualDirectoryPath==null || indidvidualBaseURL==null) throw new WebWriterException("Unable to find disk or base URL for "+ind.getURI());
		makeFiles(ind,type,individualDirectoryPath,indidvidualBaseURL,version);
		if(ind.isVersioned()) {
			String[] versions=ind.getVersions();
			for(String subVersion:versions) {
				String basePath=individualDirectoryPath+"/"+subVersion+"/";
				String baseURL=indidvidualBaseURL+"/"+subVersion+"/";
				makeFiles(ind,type,basePath,baseURL,subVersion);
			}
		}
	}
	
	
	private void makeFiles(TerminologyEntity entity, String type, String collectionDirectoryPath, String collectionBaseURL, String version) throws IOException, WebWriterException, ConfigurationException {
		SSLogger.log(">>Writing "+entity.getURI()+" to "+collectionDirectoryPath, SSLogger.DEBUG);
		
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
		
		System.out.println(">> version "+version);
		
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
			if(entity.isSet()) writeToFile(languageFilesPaths[i],tm.getPageForLang(languages[i],(TerminologySet)entity,version,0)); //TODO we may need to pass more infos to the template!
			if(entity.isIndividual()) writeToFile(languageFilesPaths[i],tm.getPageForLang(languages[i],(TerminologyIndividual)entity,version,0));
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
			  SSLogger.log("Created directory " + directortyPath,SSLogger.DEBUG);
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
