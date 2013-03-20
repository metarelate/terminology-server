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

package net.metarelate.terminology.publisher;
// TODO to refactor
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * This class serializes a model to a set files on the file system. 
 * The class is initialized with a root collection, and the method write() perform a tree traversal and generates files.
 * The class accept a few extra parameters, like a model containing labels (these are "decoration" labels, not part of the terminology
 * information content. E.g.: they are the label for "skos:concept" itself, rather than for terms defined by wmo).
 * Note that the system assumed terms are laid out in a tree. There is not an injerent limitation in extending this model to a graph
 * (writing files is idempotent, and multiple traversal of the same don't generate errors). However, there may be some data strcuture that assumes
 * a unique location for a node, that should be changed to a list.
 * @author andreasplendiani
 *
 */
public class WebWriter {
	private Model extraTriplesInInput=null;				// An extended set of triples which contains essential parameters and optional information needed (or useful) for publishing a model on the web.	
	private TerminologySet rootCollection;				// The root of the tree (DAG) to layout
	private boolean overWriteFiles=false;				// Should we overwite files already present on the fle system ?
	private String cssUrl=null; 						// TODO maybe we want a default in CoreConfig
	
	//private Model labelRepository=null;					// A Jena model containing a set of triples with "optional" labels (non part of the wmo definitions)
	private Map<String,String> prefixMap=null;
	
	private Hashtable<String,String> uri2Url=null;
	private Hashtable<String,String> uri2Path=null;
	
	private String baseURL="/";
	
	// TODO Here we need s different constructors in case the system is called for a non-root.
	// This constructor should "ovverride" base URI and base file System
	public WebWriter(TerminologySet rootCollection, Model extraTriplesInput, boolean overWriteFiles) {
		SSLogger.log("Created WebWriter for root: "+rootCollection.getURI(),SSLogger.DEBUG);
		SSLogger.log("Model size: "+extraTriplesInput.size(),SSLogger.DEBUG);
		this.extraTriplesInInput=extraTriplesInput;
		this.rootCollection=rootCollection;
		this.overWriteFiles=overWriteFiles;
		this.uri2Url=new Hashtable<String,String>(); //We precomputer URLs
		this.uri2Path=new Hashtable<String,String>();
		this.prefixMap=extraTriplesInput.getNsPrefixMap();	
	}
	
	//public void setLabelModel(Model labelModel) {
	//	this.labelRepository=labelModel;
	//}
	
	public void setPrefixMap(Map<String,String> map) {
		this.prefixMap=map;
	}
	
	public void write() throws WebWriterException, IOException, ModelException, WebSystemException {
		Iterator<String> psm= prefixMap.keySet().iterator();
		while(psm.hasNext()) {
			String pi=psm.next();
			SSLogger.log("WRITING TO WEB WITH PREFIX: "+pi+" for "+prefixMap.get(pi),SSLogger.DEBUG);
		}
		//if(labelRepository==null) {
		//	labelRepository=extraTriplesInInput;
		//	SSLogger.log("No explicit label model selected, labels taken from config",SSLogger.DEBUG);
		//}
		
		//String sitePrefix="";
		String diskPrefix="";
		
		// TODO these should be moved to the constructor
		//NodeIterator myIter=extraTriplesInInput.listObjectsOfProperty(MetaLanguage.sitePrefixProperty);
		//if(!myIter.hasNext()) throw new WebWriterException("Unable to find a site prefix");
		//RDFNode node=myIter.nextNode();
		//if(!node.isLiteral()) throw new WebWriterException("Unable to find a literal for site prefix");
		//sitePrefix=((Literal) node).getValue().toString();
		
		NodeIterator  myIter=extraTriplesInInput.listObjectsOfProperty(MetaLanguage.diskPrefixProperty);
		if(!myIter.hasNext()) throw new WebWriterException("Unable to find a site prefix");
		RDFNode node=myIter.nextNode();
		if(!node.isLiteral()) throw new WebWriterException("Unable to find a literal for site prefix");
		diskPrefix=((Literal) node).getValue().toString();
		
		myIter=extraTriplesInInput.listObjectsOfProperty(MetaLanguage.baseURLProperty);
		if(!myIter.hasNext()) SSLogger.log("No base URL specified, going for /",SSLogger.DEBUG);
		node=myIter.nextNode();
		if(!node.isLiteral()) throw new WebWriterException("baseURL should be a literal!");
		baseURL=((Literal) node).getValue().toString();
		
		//TODO Obsolete
		myIter=extraTriplesInInput.listObjectsOfProperty(MetaLanguage.cssAddressProperty);
		if(myIter.hasNext()) {
			node=myIter.nextNode();
			if(node.isLiteral()){ 
				cssUrl=((Literal) node).getValue().toString();	
				SSLogger.log("Fond css file at: "+cssUrl,SSLogger.DEBUG);
			}
		}
		if(cssUrl==null) SSLogger.log("No css specified",SSLogger.DEBUG);
		
		
		
		SSLogger.log("Starting WebWriter",SSLogger.DEBUG);
		//SSLogger.log("site prefix: "+sitePrefix,SSLogger.DEBUG);
		SSLogger.log("disk prefix: "+diskPrefix,SSLogger.DEBUG);
		
		//Check that disk is viable.
		File baseFile=new File(diskPrefix);
		if(!baseFile.exists()) throw new WebWriterException("The directory: "+baseFile+" must be already present!\n This is a security check and it is required to create this directory outside this command.");
		
		//first we pre-compute URLs
		// TODO note: we just removed baseURL, let's check it works!
		preComputeReferences(rootCollection,baseURL,diskPrefix);
		writeSetToWeb(rootCollection);
	}
	
	public void write(String rootPath, String rootURL) throws WebWriterException, IOException, ModelException, WebSystemException {
		Iterator<String> psm= prefixMap.keySet().iterator();
		while(psm.hasNext()) {
			String pi=psm.next();
			SSLogger.log("WRITING TO WEB WITH PREFIX: "+pi+" for "+prefixMap.get(pi),SSLogger.DEBUG);
		}
		//if(labelRepository==null) {
		//	labelRepository=extraTriplesInInput;
		//	SSLogger.log("No explicit label model selected, labels taken from config",SSLogger.DEBUG);
		//}
		
		
		
		
		
		SSLogger.log("Starting WebWriter",SSLogger.DEBUG);
		SSLogger.log("site prefix: "+rootURL,SSLogger.DEBUG);
		SSLogger.log("disk prefix: "+rootPath,SSLogger.DEBUG);
		
		//Check that disk is viable.
		File baseFile=new File(rootPath);
		if(!baseFile.exists()) throw new WebWriterException("The directory: "+baseFile+" must be already present!\n This is a security check and it is required to create this directory outside this command.");
		
		//first we pre-compute URLs
		preComputeReferences(rootCollection,rootURL,rootPath);
		writeSetToWeb(rootCollection);
	}
	
	
	private void preComputeReferences(TerminologySet collection,String urlPrefix, String diskPrefix) throws ModelException {
		//System.out.println(">>>>Precomputing references for: "+collection.getURI());
		//Here we check for overrides
		String myNSBit=collection.getLocalNamespace();

		Literal basePath=SimpleQueriesProcessor.getOptionalLiteral(collection.getResource(), MetaLanguage.overrideBasePathProperty, extraTriplesInInput);
		Literal baseNamespace=SimpleQueriesProcessor.getOptionalLiteral(collection.getResource(), MetaLanguage.overrideBaseSiteProperty, extraTriplesInInput);
		String collectionURL=urlPrefix+"/"+myNSBit;
		// if we have an override directive, we re-define it
		if(baseNamespace!=null) {
			if(baseNamespace.isLiteral()) {
				collectionURL=baseNamespace.getValue().toString();
			}
		}
	
		String collectionPath=diskPrefix+"/"+myNSBit;
		// if we have an override directive, we re-define it
		if(basePath!=null) {
			if(basePath.isLiteral()) {
				collectionPath=basePath.getValue().toString();
			}
		}
		uri2Url.put(collection.getURI(), collectionURL);
		uri2Path.put(collection.getURI(),collectionPath);
		
		Iterator<TerminologySet> childrenEnum=collection.getAllKnownContainedCollections().iterator();
		while(childrenEnum.hasNext()) {
			preComputeReferences(childrenEnum.next(),collectionURL,collectionPath);
		}
		

		Iterator<TerminologyIndividual> myIndIter=collection.getAllKnownContainedInviduals().iterator();
		//System.out.println(">>>>For: "+collection.getURI()+" Total number of terms: "+collection.getAllKnownContainedInviduals().size());
		while(myIndIter.hasNext()) {
			TerminologyIndividual ind=myIndIter.next();
			String indNs=ind.getLocalNamespace();
			String indURL=collectionURL+"/"+indNs;
			String indPath=collectionPath+"/"+indNs;
			//System.out.println(">>>>PreCompute IND: "+ind.getURI()+" > "+indURL+" > "+indPath);
			uri2Url.put(ind.getURI(),indURL);
			uri2Path.put(ind.getURI(), indPath);
		}
	}
	

	
				
	
	
	/**
	 * Write a set of files for a collection
	 * @param collection
	 * @param accumulatedURLPrefix
	 * @param pathAccumulatedPrefix
	 * @throws WebWriterException
	 * @throws IOException
	 * @throws ModelException 
	 * @throws WebSystemException 
	 * @throws ConfigFileException
	 */
	//TODO no need to propagate namespace anymore, and also directory could be pre-computed.

	private void writeSetToWeb(TerminologySet collection) throws WebWriterException, IOException, ModelException, WebSystemException {
		WebRendererSet myRenderer=new WebRendererSet(collection,uri2Url.get(collection.getURI()));
		myRenderer.registerUrlMap(uri2Url);
		// TODO these two values could be overridden to write a sub-tree of the file system
		String collectionDirectoryPath=uri2Path.get(collection.getURI()); 	// the directory path
		String collectionBaseURL=uri2Url.get(collection.getURI());
		
		SSLogger.log(">>Writing "+collection.getURI()+" to "+uri2Url.get(collection.getURI()), SSLogger.DEBUG);
		
		// TODO we hard-code two languages (en it), but we could easily have a loop on an array of languages 
		// In total we write and index.var file for content negotation, files in two languages and teh model in rdf/xml and turtle
		String registerVarFile=collectionDirectoryPath+"/"+"register.var";
		String registerHtmlFileEN=collectionDirectoryPath+"/"+"register.en.html"; 	// file (html)
		String registerHtmlFileIT=collectionDirectoryPath+"/"+"register.it.html"; 	// file (html)
		String registerRDFFile=collectionDirectoryPath+"/"+"register.rdf"; 	// file (rdf)
		String registerTTLFile=collectionDirectoryPath+"/"+"register.ttl"; 	// file (rdf)
		String registerJSONFile=collectionDirectoryPath+"/"+"register.json"; 	// file (rdf)
		String registerQRImageFile=collectionDirectoryPath+"/"+"register.gif";	//QR image
		
		String registerHtmlEnLink=collectionBaseURL+"/"+"register.en.html";
		String registerHtmlItLink=collectionBaseURL+"/"+"register.it.html";
		String registerRDFLink=collectionBaseURL+"/"+"register.rdf";		// url (rdf)
		String registerTTLLink=collectionBaseURL+"/"+"register.ttl";		// url (ttl)
		String registerJSONLink=collectionBaseURL+"/"+"register.json";		// url (json)
		
		
		String lastVersion=collection.getLastVersion();
		System.out.println(">>Last version "+lastVersion);
		
		File directory=myCheckedMkDir(collectionDirectoryPath,overWriteFiles);
		
		FileWriter indexHtmlStreamEN = new FileWriter(registerHtmlFileEN);
		BufferedWriter iHtmlOutEN = new BufferedWriter(indexHtmlStreamEN);
		FileWriter indexHtmlStreamIT = new FileWriter(registerHtmlFileIT);
		BufferedWriter iHtmlOutIT = new BufferedWriter(indexHtmlStreamIT);
		FileWriter indexHtmlStreamVar = new FileWriter(registerVarFile);
		BufferedWriter iHtmlOutVar = new BufferedWriter(indexHtmlStreamVar);
	
		File registerQRImageFileFW= new File(registerQRImageFile);
		FileOutputStream registerQRImageFileBW = new FileOutputStream(registerQRImageFileFW);
		QRCode.from(collection.getURI()).to(ImageType.GIF).withSize(120,120).writeTo(registerQRImageFileBW);
		registerQRImageFileBW.close();
		
		Model modelToWrite=ModelFactory.createDefaultModel();
		
		Set<TerminologySet> totalChildrenSet=collection.getAllKnownContainedCollections();
		Set<TerminologySet> localChildrenSet=collection.getCollections(lastVersion);
		Set<TerminologyIndividual> indSet=collection.getIndividuals(lastVersion);
		Set<TerminologyIndividual> totalIndSet=collection.getAllKnownContainedInviduals();
		
		
		//First we need to write the map in register.var
		String registerVarBlock="URI: register\n\n" +
				"URI: register.en.html\n" +
				"Content-type: text/html\n" +
				"Content-language: en\n\n" +
				"URI: register.it.html\n" +
				"Content-type: text/html\n" +
				"Content-language: it\n\n" +
				"URI: register.rdf\n" +
				"Content-type: application/rdf+xml\n\n" +
				"URI: register.ttl\n" +
				"Content-type: text/turtle\n";
		iHtmlOutVar.write(registerVarBlock);
		iHtmlOutVar.close();
		
		SortedMap<String,String> stdMap=new TreeMap<String,String>();
		stdMap.put("RDF/XML",registerRDFLink);
		stdMap.put("Turtle",registerTTLLink);
		
		SortedMap<String,String> langMap=new TreeMap<String,String>();
		langMap.put("en", registerHtmlEnLink);
		langMap.put("it", registerHtmlItLink);
		iHtmlOutEN.write(myRenderer.getHtmlRepresentation(lastVersion, false, "en",stdMap,langMap,baseURL));
		iHtmlOutEN.close();
		iHtmlOutIT.write(myRenderer.getHtmlRepresentation(lastVersion, false, "it",stdMap,langMap,baseURL));
		iHtmlOutIT.close();
		
		triplifyStatus(collection,collection.getLastVersion(),modelToWrite);
		triplifyMeta(collection,modelToWrite);
		triplifyLinks(collection,localChildrenSet,indSet,modelToWrite);
		modelToWrite.add(MetaLanguage.filterForData(collection.getStatements(collection.getLastVersion())));

		if(collection.isVersioned()) {
			triplifyVersion( (TerminologySet)collection, modelToWrite,lastVersion);
		}
		
		
		writeModel(modelToWrite,registerRDFFile,registerTTLFile,registerJSONFile);

			
		
		if(collection.isVersioned()) {
			String[] versions=collection.getVersions();
			//SSLogger.log("Writing vers. collection: "+collection.getURI(),SSLogger.DEBUG);
			for(int i=0;i<versions.length;i++) {
				SSLogger.log("Version: "+versions[i],SSLogger.DEBUG);
				 

				String collectionVersionDirectoryPath=collectionDirectoryPath+"/"+versions[i]+"/"; 	// the directory path
				String indexHtmlFileVerEN=collectionVersionDirectoryPath+"/"+"register.en.html"; 	// file (html)
				String indexHtmlFileVerIT=collectionVersionDirectoryPath+"/"+"register.it.html"; 
				String indexVarFileVer=collectionVersionDirectoryPath+"/"+"register.var";
				String indexRDFFileVer=collectionVersionDirectoryPath+"/"+"register.rdf"; 	// file (rdf)
				String indexTTLFileVer=collectionVersionDirectoryPath+"/"+"register.ttl"; 	// file (rdf)
				String indexJSONFileVer=collectionVersionDirectoryPath+"/"+"register.json"; 	// file (rdf)
				String registerQRImageFileVer=collectionVersionDirectoryPath+"/"+"register.gif";
				
				String indexHtmlLinkVerEN=collectionBaseURL+"/"+versions[i]+"/"+"register.en.html";
				String indexHtmlLinkVerIT=collectionBaseURL+"/"+versions[i]+"/"+"register.it.html";
				String indexRDFLinkVer=collectionBaseURL+"/"+versions[i]+"/"+"register.rdf";		// url (rdf)
				String indexTTLLinkVer=collectionBaseURL+"/"+versions[i]+"/"+"register.ttl";		// url (rdf)
				String indexJSONLinkVer=collectionBaseURL+"/"+versions[i]+"/"+"register.json";		// url (rdf)
				
				
				File vDirectory=myCheckedMkDir(collectionVersionDirectoryPath,overWriteFiles);
				/*
				if(vDirectory.isDirectory() && vDirectory.exists()) {
					if(!overWriteFiles) throw new WebWriterException("Cannot overwrite version directory "+collectionVersionDirectoryPath+" check the -ow option is this is the intended behaviour");
				}
				else {
					boolean success = vDirectory.mkdir();
					if (success) 
					  SSLogger.log("Created version directory " + collectionVersionDirectoryPath);
					else throw new WebWriterException("Unable to create version directory "+collectionVersionDirectoryPath);
				}
				*/
				
				FileWriter indexHtmlStreamVerEN = new FileWriter(indexHtmlFileVerEN);
				BufferedWriter iHtmlOutVerEN = new BufferedWriter(indexHtmlStreamVerEN);
				FileWriter indexHtmlStreamVerIT = new FileWriter(indexHtmlFileVerIT);
				BufferedWriter iHtmlOutVerIT = new BufferedWriter(indexHtmlStreamVerIT);
				FileWriter indexVarHtmlStreamVer = new FileWriter(indexVarFileVer);
				BufferedWriter iHtmlOutVarVer = new BufferedWriter(indexVarHtmlStreamVer);
				//TODO QR always points to the last version
				File registerQRImageFileFWVer= new File(registerQRImageFileVer);
				FileOutputStream registerQRImageFileBWVer = new FileOutputStream(registerQRImageFileFWVer);
				QRCode.from(collection.getURI()).to(ImageType.GIF).withSize(120,120).writeTo(registerQRImageFileBWVer);
				registerQRImageFileBWVer.close();
				
				iHtmlOutVarVer.write(registerVarBlock);
				iHtmlOutVarVer.close();
				
				

				

				SortedMap<String,String> stdMapV=new TreeMap<String,String>();
				stdMapV.put("RDF/XML",indexRDFLinkVer);
				stdMapV.put("Turtle",indexTTLLinkVer);
		
				SortedMap<String,String> langMapV=new TreeMap<String,String>();
				langMapV.put("en", indexHtmlLinkVerEN);
				langMapV.put("it", indexHtmlLinkVerIT);
				
				/*
				iHtmlOutVer.write(myRenderer.getVersionOpening(versions[i],true,"en"));
				iHtmlOutVer.write(myRenderer.getVersionHeader(stdMapV,versions[i]));
				iHtmlOutVer.write(myRenderer.getStatusVersionedBlock(versions[i], labelRepository));
				iHtmlOutVer.write(myRenderer.getMeta());
				iHtmlOutVer.write(myRenderer.getVersionBlock(versions[i],collectionBaseURL));
				iHtmlOutVer.write(myRenderer.getStatementsBlock(MetaLanguage.filterForWeb(collection.getStatements(versions[i]))));
				iHtmlOutVer.write(myRenderer.getNavigationPanel(versions[i], uri2Url));
				iHtmlOutVer.write(myRenderer.getClosing());
				iHtmlOutVer.close();
				
				*/
				
				iHtmlOutVerEN.write(myRenderer.getHtmlRepresentation(versions[i], true, "en",stdMapV,langMapV,baseURL));
				iHtmlOutVerEN.close();
				iHtmlOutVerIT.write(myRenderer.getHtmlRepresentation(versions[i], true, "it",stdMapV,langMapV,baseURL));
				iHtmlOutVerIT.close();
		

				Set<TerminologySet> childrenSetVer=collection.getCollections(versions[i]);
				Set<TerminologyIndividual> indSetVer=collection.getIndividuals(versions[i]);
				Model modelToWriteVer=ModelFactory.createDefaultModel();
				
				triplifyStatusVersion(collection,modelToWrite,versions[i]);
				triplifyMeta(collection,modelToWriteVer);
				triplifyVersion( collection, modelToWriteVer,versions[i]);
				triplifyLinks(collection,childrenSetVer,indSetVer,modelToWriteVer);
				modelToWriteVer.add(MetaLanguage.filterForData(collection.getStatements(versions[i])));

				writeModel(modelToWriteVer,indexRDFFileVer,indexTTLFileVer,indexJSONFileVer);
				
				
			}
				
			
		}			
		/** Terms and children are called only for the "current" version. 
		 * This behavior is consistent with the overall input process.
		 */
		
		//Set<TerminologyCollection> childrenSet=collection.getAllKnownContainedCollections();
		//Set<TerminologyIndividual> indSet=collection.getIndividuals();
		
	
		Iterator<TerminologySet> childrenEnum=totalChildrenSet.iterator();
		while(childrenEnum.hasNext()) {
			writeSetToWeb(childrenEnum.next());
		}
		

		Iterator<TerminologyIndividual> myIndIter=totalIndSet.iterator();
		while(myIndIter.hasNext()) {
			writeIndividualToWeb(myIndIter.next());
		}
	}

	








	private void writeModel(Model modelToWrite, String indexRDFFile,
		String indexTTLFile, String indexJSONFile) throws FileNotFoundException {
		FileOutputStream myOutRDF=	 new FileOutputStream(indexRDFFile);
		FileOutputStream myOutTTL=	 new FileOutputStream(indexTTLFile);
		//FileOutputStream myOutJSON=	 new FileOutputStream(indexJSONFile);
		if(prefixMap!=null) modelToWrite.setNsPrefixes(prefixMap);
		//System.out.println(prefixMap.toString());
		//// TODO DEBUG
		/*
		StmtIterator stats=modelToWrite.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			System.out.print("S: "+stat.getSubject());
			if(stat.getSubject().isURIResource()) System.out.println(" U");
			else if (stat.getSubject().isResource()) System.out.println(" R");
			else if (stat.getSubject().isLiteral()) System.out.println(" L");
			else if (stat.getSubject().isAnon()) System.out.println(" A");
			else System.out.println(" ?");
			System.out.print("P: "+stat.getPredicate());
			if(stat.getPredicate().isURIResource()) System.out.println(" U");
			else if (stat.getPredicate().isResource()) System.out.println(" R");
			else if (stat.getPredicate().isLiteral()) System.out.println(" L");
			else if (stat.getPredicate().isAnon()) System.out.println(" A");
			else System.out.println(" ?");
			System.out.print("O: "+stat.getObject());
			if(stat.getObject().isURIResource()) System.out.println(" U");
			else if (stat.getObject().isResource()) System.out.println(" R");
			else if (stat.getObject().isLiteral()) System.out.println(" L");
			else if (stat.getObject().isAnon()) System.out.println(" A");
			else System.out.println(" ?");
			System.out.println("-");
		}
		*/
		/////
		modelToWrite.write(myOutRDF);
		modelToWrite.write(myOutTTL,"TTL");
	
	}


	
	
	

	

	
	


	
	
	


	
	
	
	
	
	
	

	

	
	private void triplifyLinks(TerminologySet collection, Set<TerminologySet> childrenSet, Set<TerminologyIndividual> indSet,Model modelToWrite) {
		Resource collectionResource=ResourceFactory.createResource(collection.getURI());
		Iterator<TerminologySet> myCollIter=childrenSet.iterator();
		while(myCollIter.hasNext()) {
			TerminologySet myColl=myCollIter.next();
			modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.definesProperty,ResourceFactory.createResource(myColl.getURI())));
		}
	
		Iterator<TerminologyIndividual> myIndIter=indSet.iterator();
		while(myIndIter.hasNext()) {
			TerminologyIndividual myInd=myIndIter.next();
			
			modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.definesProperty,ResourceFactory.createResource(myInd.getURI())));
			
		}
		
	}
	private void triplifyTermLinks(TerminologyIndividual term,
			Model modelToWrite) {
		// TODO Nothing really to do here, it seems... but maybe later
		
	}

	private void triplifyVersion(TerminologyEntity collection,
			 Model modelToWrite, String version) {
		Resource collectionResource=ResourceFactory.createResource(collection.getURI());
		modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasVersionProperty,ResourceFactory.createPlainLiteral(version)));
		if(collection.hasPreviousVersion(version)) 
			modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasPreviousVersionProperty,ResourceFactory.createPlainLiteral(version)));

	
		
	}
	
	private void triplifyMeta(TerminologyEntity entity,
			Model modelToWrite) {
		// TODO Auto-generated method stub
		Resource collectionResource=ResourceFactory.createResource(entity.getURI());
		modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasManagerProperty,ResourceFactory.createResource(entity.getOwnerURI())));
		
	}
	
	private void triplifyStatus(TerminologyEntity entity, String version,
			Model modelToWrite) {
		Resource entityResource=ResourceFactory.createResource(entity.getURI());
		modelToWrite.add(ResourceFactory.createStatement(entityResource,MetaLanguage.hasStatusProperty,ResourceFactory.createResource(entity.getStateURI(version))));
		
	}
	
	private void triplifyStatusVersion(TerminologyEntity entity,
			Model modelToWrite, String version) {
		Resource entityResource=ResourceFactory.createResource(entity.getURI());
		modelToWrite.add(ResourceFactory.createStatement(entityResource,MetaLanguage.hasStatusProperty,ResourceFactory.createResource(entity.getStateURI(version))));
		
	}
	
	
	
	
	
	//TODO no need to propagate namespace anymore, and also directory could be pre-computed.
	private void writeIndividualToWeb(TerminologyIndividual term) throws IOException, WebWriterException, WebSystemException, ModelException {
		WebRendererIndividual myRenderer=new WebRendererIndividual(term,uri2Url.get(term.getURI()));
		myRenderer.registerUrlMap(uri2Url);
		String termDirectoryPath=uri2Path.get(term.getURI()); 	// the directory path
		String termURL=uri2Url.get(term.getURI());
		
		if(term.isVersioned()) SSLogger.log("Writing vers. individual "+term.getURI());
		else SSLogger.log("Writing unv. individual "+term.getURI());
		
		String termVarFile=termDirectoryPath+"/code.var";
		String termFileHtmlEN=termDirectoryPath+"/code.en.html";
		String termFileHtmlIT=termDirectoryPath+"/code.it.html";
		String termIndexRDF=termDirectoryPath+"/code.rdf";
		String termIndexTTL=termDirectoryPath+"/code.ttl";
		String termIndexJSON=termDirectoryPath+"/code.json";
		String individualQRImageFile=termDirectoryPath+"/"+"register.gif";	//QR image

		
		
		String termURIHtmlEN=termURL+"/code.en.html";
		String termURIHtmlIT=termURL+"/code.it.html";
		String termURIRDF=termURL+"/code.rdf";
		String termURITTL=termURL+"/code.ttl";
		String termURIJSON=termURL+"/code.json";
		
		String lastVersion=term.getLastVersion();
		System.out.println(">>Last version "+lastVersion);
	
		
		// TODO obsolete ?
		//Resource termResource=ResourceFactory.createResource(term.getURI());
		NodeIterator myIter=extraTriplesInInput.listObjectsOfProperty(term.getResource(),MetaLanguage.localIdProperty);
		String myID="";
		if(myIter.hasNext()) {
			RDFNode node=myIter.nextNode();
			if(node.isLiteral()) 
				myID=((Literal) node).getValue().toString();
		}
		if(myID=="") myID=term.getLastURIBit(); //TODO maybe we can factor these things somewhere.
		
		
		//String termURL=accumulatedNamespace+"/"+myID;			//URI! 
		
		
		File directory=myCheckedMkDir(termDirectoryPath,overWriteFiles);
		
		/*
		File directory=new File(termDirectoryPath);
		if(directory.isDirectory() && directory.exists()) {
			if(!overWriteFiles) throw new WebWriterException("Cannot overwrite directory "+termDirectoryPath+" check the -ow option is this is the intended behaviour");
		}
		else {
			boolean success = directory.mkdir();
			if (success) 
			  SSLogger.log("Created directory " + termDirectoryPath,SSLogger.DEBUG);
			else throw new WebWriterException("Unable to create directory "+termDirectoryPath);
		}
		 */
		
		FileWriter termHtmlStreamEN = new FileWriter(termFileHtmlEN);
		BufferedWriter iHtmlOutEN = new BufferedWriter(termHtmlStreamEN);
		FileWriter termHtmlStreamIT = new FileWriter(termFileHtmlIT);
		BufferedWriter iHtmlOutIT = new BufferedWriter(termHtmlStreamIT);
		FileWriter termHtmlStreamVar = new FileWriter(termVarFile);
		BufferedWriter iHtmlOutVar = new BufferedWriter(termHtmlStreamVar);
		
		//Set<String> standards=term.getStandardURIs();
		Model modelToWrite=ModelFactory.createDefaultModel();
		
		String termVarBlock="URI: code\n\n" +
				"URI: code.en.html\n" +
				"Content-type: text/html\n" +
				"Content-language: en\n\n" +
				"URI: code.it.html\n" +
				"Content-type: text/html\n" +
				"Content-language: it\n\n" +
				"URI: code.rdf\n" +
				"Content-type: application/rdf+xml\n\n" +
				"URI: code.ttl\n" +
				"Content-type: text/turtle\n";
		iHtmlOutVar.write(termVarBlock);
		iHtmlOutVar.close();		
				
		File individualQRImageFileFW= new File(individualQRImageFile);
		FileOutputStream individualQRImageFileBW = new FileOutputStream(individualQRImageFileFW);
		QRCode.from(term.getURI()).to(ImageType.GIF).withSize(120,120).writeTo(individualQRImageFileBW);
		individualQRImageFileBW.close();
		
		SortedMap<String,String> stdMap=new TreeMap<String,String>();
		stdMap.put("RDF/XML",termURIRDF);
		stdMap.put("Turtle",termURITTL);
		SortedMap<String,String> langMap=new TreeMap<String,String>();
		langMap.put("en", termURIHtmlEN);
		langMap.put("it", termURIHtmlIT);
		
		iHtmlOutEN.write(myRenderer.getHtmlRepresentation(lastVersion, false, "en",stdMap,langMap,baseURL));
		iHtmlOutEN.close();
		iHtmlOutIT.write(myRenderer.getHtmlRepresentation(lastVersion, false, "it",stdMap,langMap,baseURL));
		iHtmlOutIT.close();
		
		triplifyStatus(term,term.getLastVersion(),modelToWrite);
		triplifyMeta(term,modelToWrite);
		modelToWrite.add(MetaLanguage.filterForData(term.getStatements(term.getLastVersion())));	
		triplifyTermLinks(term,modelToWrite);

		if(term.isVersioned()) {
			triplifyVersion( term, modelToWrite,term.getLastVersion());
		}
		
		writeModel(modelToWrite,termIndexRDF,termIndexTTL,termIndexJSON);
		
		
		if(term.isVersioned()) {
			String[] versions =term.getVersions();
			for(int v=0;v<versions.length;v++) {
				SSLogger.log("Version "+versions[v],SSLogger.DEBUG);
				
				// TODO to check
				//String termDirectoryPathVer=termDirectoryPath+"/"+versions[v]; 	// the directory path
				//String termURLVer=termURL+"/"+myID+"/"+versions[v];			//URI! 
				
				String termDirectoryPathVer=termDirectoryPath+"/"+versions[v]; 	// the directory path
				String termURLVer=termURL+"/"+versions[v];			//URI! 
				
				File directoryVer=myCheckedMkDir(termDirectoryPathVer,overWriteFiles);

				/*
				File directoryVer=new File(termDirectoryPathVer);
				if(directoryVer.isDirectory() && directoryVer.exists()) {
					if(!overWriteFiles) throw new WebWriterException("Cannot overwrite version directory "+termDirectoryPath+" check the -ow option is this is the intended behaviour");
				}
				else {
					boolean success = directoryVer.mkdir();
					if (success) 
					  SSLogger.log("Created version directory " + termDirectoryPathVer,SSLogger.DEBUG);
					else throw new WebWriterException("Unable to create version directory "+termDirectoryPathVer);
				}
				*/
				
				String termFileHtmlVerEN=termDirectoryPathVer+"/code.en.html";
				String termFileHtmlVerIT=termDirectoryPathVer+"/code.it.html";
				String termVarFileVer=termDirectoryPathVer+"/code.var";
				String termIndexRDFVer=termDirectoryPathVer+"/code.rdf";
				String termIndexTTLVer=termDirectoryPathVer+"/code.ttl";
				String termIndexJSONVer=termDirectoryPathVer+"/code.json";
				String individualQRImageFileVer=termDirectoryPathVer+"/"+"register.gif";
				
				String termURIHtmlVerEN=termURLVer+"/code.en.html";
				String termURIHtmlVerIT=termURLVer+"/code.it.html";
				String termURIRDFVer=termURLVer+"/code.rdf";
				String termURITTLVer=termURLVer+"/code.ttl";
				String termURIJSONVer=termURLVer+"/code.json";
				
				FileWriter termHtmlStreamVerEN = new FileWriter(termFileHtmlVerEN);
				BufferedWriter iHtmlOutVerEN = new BufferedWriter(termHtmlStreamVerEN);
				FileWriter termHtmlStreamVerIT = new FileWriter(termFileHtmlVerIT);
				BufferedWriter iHtmlOutVerIT = new BufferedWriter(termHtmlStreamVerIT);
				FileWriter termVarStreamVer = new FileWriter(termVarFileVer);
				BufferedWriter iVarOutVer = new BufferedWriter(termVarStreamVer);
				
				//TODO QR Always points to the last version
				File individualQRImageFileFWVer= new File(individualQRImageFileVer);
				FileOutputStream individualQRImageFileBWVer = new FileOutputStream(individualQRImageFileFWVer);
				QRCode.from(term.getURI()).to(ImageType.GIF).withSize(120,120).writeTo(individualQRImageFileBWVer);
				individualQRImageFileBWVer.close();
				
				//Set<String> standardsVer=term.getStandardURIsForVersion(versions[v]);
				Model modelToWriteVer=ModelFactory.createDefaultModel();
				
				iVarOutVer.write(termVarBlock);
				iVarOutVer.close();
				
				SortedMap<String,String> stdMapV=new TreeMap<String,String>();
				stdMapV.put("RDF/XML",termURIRDFVer);
				stdMapV.put("Turtle",termURITTLVer);
				
				SortedMap<String,String> langMapV=new TreeMap<String,String>();
				langMapV.put("en", termURIHtmlVerEN);
				langMapV.put("it", termURIHtmlVerIT);
				
				/*
				iHtmlOutVer.write(myRenderer.getVersionOpening(versions[v],true,"en"));
				iHtmlOutVer.write(myRenderer.getVersionHeader(stdMapV, versions[v]));
				iHtmlOutVer.write(myRenderer.getMeta());
				iHtmlOutVer.write(myRenderer.getStatusVersionedBlock(versions[v], labelRepository));
				iHtmlOutVer.write(myRenderer.getVersionBlock(versions[v],termURL));
				iHtmlOutVer.write(myRenderer.getStatementsBlock(MetaLanguage.filterForWeb(term.getStatements(versions[v]))));
				iHtmlOutVer.write(myRenderer.getNavigationPanel(versions[v], uri2Url));
				iHtmlOutVer.write(myRenderer.getClosing());
				iHtmlOutVer.close();
				*/
				iHtmlOutVerEN.write(myRenderer.getHtmlRepresentation(versions[v], true, "en",stdMapV,langMapV,baseURL));
				iHtmlOutVerEN.close();
				iHtmlOutVerIT.write(myRenderer.getHtmlRepresentation(versions[v], true, "it",stdMapV,langMapV,baseURL));
				iHtmlOutVerIT.close();
				
		
				triplifyStatusVersion(term,modelToWrite,versions[v]);
				triplifyMeta(term,modelToWriteVer);
				triplifyVersion(term, modelToWriteVer,versions[v]);
				//System.out.println("SIZE: ++++++++++++"+term.getStatements().size());
				modelToWriteVer.add(MetaLanguage.filterForData(term.getStatements(versions[v])));	
				triplifyTermLinks(term,modelToWriteVer);
				writeModel(modelToWriteVer,termIndexRDFVer,termIndexTTLVer,termIndexJSONVer);
				
				
			}
		}
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

	
	
}

