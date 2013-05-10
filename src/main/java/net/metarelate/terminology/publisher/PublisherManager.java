package net.metarelate.terminology.publisher;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

public class PublisherManager {
	Initializer myInitializer=null;
	public static final int WEB_FILES = 0;
	public static final int DOC_FILE=1;
	public static final int ONLINE=2;
	
	public static final String uriHasUrl="http://metarelate.net/internal/cache/uriHasUrl"; // TODO maybe we should move this somewhere else
	public static final String uriHasDisk="http://metarelate.net/internal/cache/uriHasDisk"; // TODO maybe we should move this somewhere else
	
	private String templateLocation=null;
	
	public PublisherManager(Initializer myInitializer) {
		this.myInitializer = myInitializer;
	}
	
	public void setTemplateLocation(String template) {
		this.templateLocation=template;
		
	}
	
	/*
	public void publish(String rootURI,int publishingType) throws ModelException {
		switch (publishingType) {
			case WEB_FILES: webFileRender(rootURI);
			break;
			case DOC_FILE: docRender(rootURI);
			break;
			case ONLINE: onlineRender(rootURI);
			break;
		}
	}
	*/
	private void publishWebFiles(String rootURI, Model extraInputGraph) throws ModelException, ConfigurationException, WebWriterException, UnknownURIException, IOException {
		SSLogger.log("Publishing the terminology under "+rootURI+" as a set of web-files");
		String baseURL=null;
		String baseDisk=null;
		TerminologySet root=null;
		if(myInitializer.myFactory.terminologySetExist(rootURI)) {
			root=myInitializer.myFactory.getUncheckedTerminologySet(rootURI);
			if(root.isRoot()) {
				baseURL=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(), PublisherConfig.baseURLProperty);
				baseDisk=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(),PublisherConfig.diskPrefixProperty);
				SSLogger.log("From configuration files:  For "+rootURI+" found URL: "+baseURL+" , Disk: "+baseDisk,SSLogger.DEBUG);
			}
			else {
				Set<TerminologySet> uberRoots=root.getContainers(root.getLastVersion());
				TerminologySet uberRoot=null;
				if(uberRoots.size()==0) throw new WebWriterException(rootURI+" is not active in its last version. Try to publish an super-register");
				else if(uberRoots.size()>1) throw new ModelException(rootURI+ " in multiple containers");
				else uberRoot=uberRoots.iterator().next();
				baseURL=myInitializer.myCache.getValueFor(uberRoot.getURI(), uriHasUrl);
				baseDisk=myInitializer.myCache.getValueFor(uberRoot.getURI(), uriHasDisk);
				SSLogger.log("From cache: For "+rootURI+" found URL: "+baseURL+" , Disk: "+baseDisk,SSLogger.DEBUG);
				if(baseURL==null || baseDisk==null) throw new WebWriterException("You attempted to publish a register never published before, roots should be published first! ("+rootURI+")");
			}
		}
		else throw new WebWriterException("Only known registers can be published");
		SSLogger.log("Cleaning cache for "+rootURI);
		cleanCacheTree(rootURI);
		buildURLMapAndCache(baseURL,baseDisk,rootURI,extraInputGraph);
		TemplateManager myTm=new TemplateManager(templateLocation);
		PublisherVisitor vis=new WebFileVisitor(myTm);
		vis.crawl(root);
		// build Visitor
		// cycle over passing visitor (visitor print files)
		//TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(rootURI);
		
	}
	
	private void publishDoc(String rootURI) {
		//TODO we allow only upper level printing. Check needed!

		// initialize TemplateManager
		// build Visitor
		// cycle over passing visitor 
		// visitor.print
		
	}
	
	private void publishOnline(String rootURI) throws ModelException {
		// 1) Do we know it already ? >> get/clean/compute
		// 2) Unknown: is it a root ? >> compute (must find params or error)
		// 3) Unknwon, not roor: error. Need to compute root first.
		cleanCacheTree(rootURI);
		// find baseURI/URL
		// resolve URI/URLs
		// initialize termplateManager
		// start server
	}
	
	private void cleanCacheTree(String rootURI) throws ModelException {
		myInitializer.myCache.cleanValueFor(rootURI,uriHasUrl);
		SSLogger.log("Cleaning uriHasUrl chache for "+rootURI,SSLogger.DEBUG);
		if(myInitializer.myFactory.terminologyEntityExist(rootURI)) {
			Collection<TerminologySet> sets=myInitializer.myFactory.getAllSets();
			for (TerminologySet set:sets ) {
				cleanCacheTree(set.getURI());
			}
			Collection<TerminologyIndividual> inds=myInitializer.myFactory.getAllIndividuals();
			for (TerminologyIndividual ind:inds) {
				cleanCacheTree(ind.getURI());
			}
		}
		
	}
	
	private void buildURLMapAndCache(String baseURL,String baseDisk,String rootURI, Model extraParametersGraph) throws ModelException, UnknownURIException, ConfigurationException {
		TerminologySet currentSet=myInitializer.myFactory.getCheckedTerminologySet(rootURI);
		String myNSBit=currentSet.getLocalNamespace();	//TODO this needs a fix for trailing "/"
		
		// We still allow override of URLs
		Literal overrideBaseDisk=SimpleQueriesProcessor.getOptionalLiteral(currentSet.getResource(), PublisherConfig.overrideBasePathProperty, extraParametersGraph);
		Literal overrideBaseURL=SimpleQueriesProcessor.getOptionalLiteral(currentSet.getResource(), PublisherConfig.overrideBaseSiteProperty, extraParametersGraph);
		if(overrideBaseURL!=null) {
			if(overrideBaseURL.isLiteral()) {
				baseURL=overrideBaseURL.getValue().toString();
			}
			else throw new ConfigurationException("Override for "+rootURI+" does not have a valid literal :"+overrideBaseURL);
		}
		String collectionURL=baseURL+"/"+myNSBit;
		
		if(overrideBaseDisk!=null) {
			if(overrideBaseDisk.isLiteral()) {
				baseDisk=overrideBaseDisk.getValue().toString();
			}
			else throw new ConfigurationException("Override for "+rootURI+" does not have a valid literal :"+overrideBaseDisk);
		}
		String collectionDisk=baseDisk+"/"+myNSBit;
		
		myInitializer.myCache.recordValue(rootURI, uriHasUrl, collectionURL);
		myInitializer.myCache.recordValue(rootURI, uriHasDisk, collectionDisk);
		
		// Now going for individuals
		Iterator<TerminologyIndividual> myIndIter=currentSet.getAllKnownContainedInviduals().iterator();
		//System.out.println(">>>>For: "+collection.getURI()+" Total number of terms: "+collection.getAllKnownContainedInviduals().size());
		while(myIndIter.hasNext()) {
			TerminologyIndividual ind=myIndIter.next();
			String indNs=ind.getLocalNamespace();
			String indURL=collectionURL+"/"+indNs;
			String indPath=collectionDisk+"/"+indNs;
			myInitializer.myCache.recordValue(ind.getURI(),uriHasUrl,indURL);
			myInitializer.myCache.recordValue(ind.getURI(),uriHasDisk,indPath);
		}
				
		Iterator<TerminologySet> childrenEnum=currentSet.getAllKnownContainedCollections().iterator();
		while(childrenEnum.hasNext()) {
			buildURLMapAndCache(collectionURL,collectionDisk,childrenEnum.next().getURI(),extraParametersGraph);
		}
		
	}

}
