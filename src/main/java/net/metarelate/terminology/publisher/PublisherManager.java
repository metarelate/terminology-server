package net.metarelate.terminology.publisher;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

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
	
	
	
	private String templateLocation=null;
	
	public PublisherManager(Initializer myInitializer) {
		this.myInitializer = myInitializer;
	}
	
	public void setTemplateLocation(String template) {
		this.templateLocation=template;
		
	}
	
	public void publishWebFiles(String rootURI, Model extraInputGraph,boolean overwrite) throws ModelException, ConfigurationException, WebWriterException, UnknownURIException, IOException {
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
				baseURL=myInitializer.myCache.getValueFor(uberRoot.getURI(), PublisherConfig.uriHasUrl);
				baseDisk=myInitializer.myCache.getValueFor(uberRoot.getURI(), PublisherConfig.uriHasDisk);
				SSLogger.log("From cache: For "+rootURI+" found URL: "+baseURL+" , Disk: "+baseDisk,SSLogger.DEBUG);
				if(baseURL==null || baseDisk==null) throw new WebWriterException("You attempted to publish a register never published before, roots should be published first! ("+rootURI+")");
			}
		}
		else throw new WebWriterException("Only known registers can be published");
		SSLogger.log("Cleaning cache for "+rootURI);
		cleanCacheTree(rootURI);
		buildURLMapAndCache(baseURL,baseDisk,rootURI,extraInputGraph);
		//TODO should be hidden by design.
		myInitializer.myCache.synch();
		TemplateManager myTm=new TemplateManager(templateLocation);
		WebFilesVisitor vis=new WebFilesVisitor(myInitializer,myTm);
		vis.setOverwriteFiles(overwrite);
		vis.crawl(root);
		
		
		
		// build Visitor
		// cycle over passing visitor (visitor print files)
		//TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(rootURI);
		
	}
	
	public void publishDoc(String tag, String language, String fileName) throws ConfigurationException, IOException, ModelException, WebWriterException {
		SSLogger.log("Publishing the terminology as a document for tag "+tag+" in language "+language);
		TemplateManager myTm=new TemplateManager(templateLocation); //TODO just in case we need a different design
		DocumentVisitor vis=new DocumentVisitor(myInitializer,myTm);
		vis.bind(tag, language);
		vis.writeToFile(fileName);
		// Optional latex bits ?
		
	}
	
	public void publishOnline(Model extraTriplesGraph, int port) throws Exception {
		String baseURL=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(), PublisherConfig.baseURLProperty);
		SSLogger.log("From configuration files found base URL:  "+baseURL,SSLogger.DEBUG);
		cleanCache();
		//TODO should be hidden by design.
		myInitializer.myCache.synch();
		TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
		for (TerminologySet root:roots) {
			try {
				buildURLMapAndCacheSimple(baseURL,root.getURI(),extraTriplesGraph);
			} catch (UnknownURIException e) {
				e.printStackTrace();
				throw new ConfigurationException(root.getURI()+" is a root, but it's unknown!");
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//TODO should be hidden by design.
		myInitializer.myCache.synch();
		TemplateManager myTm=new TemplateManager(templateLocation);
		
		//TODO should be hidden by design.
		myInitializer.myCache.synch();
		
		Server server = new Server(port);
	    server.setHandler(new SimpleServer());
	    server.start();
	    server.join();
	    
		
		
			// 1) Do we know it already ? >> get/clean/compute
		// 2) Unknown: is it a root ? >> compute (must find params or error)
		// 3) Unknwon, not roor: error. Need to compute root first.
		
		// find baseURI/URL
		// resolve URI/URLs
		// initialize termplateManager
		// start server
	}
	// TODO, this should be better designed, properties cleaned should be implicit.
	private void cleanCacheTree(String rootURI) throws ModelException {
		myInitializer.myCache.cleanValueFor(rootURI,PublisherConfig.uriHasUrl);
		myInitializer.myCache.cleanValueFor(rootURI,PublisherConfig.uriHasDisk);
		SSLogger.log("Cleaning uriHasUrl and uriHasDisk chache for "+rootURI,SSLogger.DEBUG);
		if(myInitializer.myFactory.terminologySetExist(rootURI)) {
			Collection<TerminologySet> sets=myInitializer.myFactory.getUncheckedTerminologySet(rootURI).getCollections();
			for (TerminologySet set:sets ) {
				cleanCacheTree(set.getURI());
			}
			Collection<TerminologyIndividual> inds=myInitializer.myFactory.getUncheckedTerminologySet(rootURI).getAllKnownContainedInviduals();
			for (TerminologyIndividual ind:inds) {
				cleanCacheTree(ind.getURI());
			}
		}
		
	}
	
	private void buildURLMapAndCache(String baseURL,String baseDisk,String entityURI, Model extraParametersGraph) throws ModelException, UnknownURIException, ConfigurationException {
		TerminologySet currentSet=myInitializer.myFactory.getCheckedTerminologySet(entityURI);
		String myNSBit=currentSet.getLocalNamespace();	//TODO this needs a fix for trailing "/"
		
		// We still allow override of URLs
		Literal overrideBaseDisk=SimpleQueriesProcessor.getOptionalLiteral(currentSet.getResource(), PublisherConfig.overrideBasePathProperty, extraParametersGraph);
		Literal overrideBaseURL=SimpleQueriesProcessor.getOptionalLiteral(currentSet.getResource(), PublisherConfig.overrideBaseSiteProperty, extraParametersGraph);
		if(overrideBaseURL!=null) {
			if(overrideBaseURL.isLiteral()) {
				baseURL=overrideBaseURL.getValue().toString();
			}
			else throw new ConfigurationException("Override for "+entityURI+" does not have a valid literal :"+overrideBaseURL);
		}
		String collectionURL=baseURL+"/"+myNSBit;
		
		if(overrideBaseDisk!=null) {
			if(overrideBaseDisk.isLiteral()) {
				baseDisk=overrideBaseDisk.getValue().toString();
			}
			else throw new ConfigurationException("Override for "+entityURI+" does not have a valid literal :"+overrideBaseDisk);
		}
		String collectionDisk=baseDisk+"/"+myNSBit;
		
		myInitializer.myCache.recordValue(entityURI, PublisherConfig.uriHasUrl, collectionURL);
		myInitializer.myCache.recordValue(entityURI, PublisherConfig.uriHasDisk, collectionDisk);
		
		// Now going for individuals
		Iterator<TerminologyIndividual> myIndIter=currentSet.getAllKnownContainedInviduals().iterator();
		//System.out.println(">>>>For: "+collection.getURI()+" Total number of terms: "+collection.getAllKnownContainedInviduals().size());
		while(myIndIter.hasNext()) {
			TerminologyIndividual ind=myIndIter.next();
			String indNs=ind.getLocalNamespace();
			String indURL=collectionURL+"/"+indNs;
			String indPath=collectionDisk+"/"+indNs;
			myInitializer.myCache.recordValue(ind.getURI(),PublisherConfig.uriHasUrl,indURL);
			myInitializer.myCache.recordValue(ind.getURI(),PublisherConfig.uriHasDisk,indPath);
		}
				
		Iterator<TerminologySet> childrenEnum=currentSet.getAllKnownContainedCollections().iterator();
		while(childrenEnum.hasNext()) {
			buildURLMapAndCache(collectionURL,collectionDisk,childrenEnum.next().getURI(),extraParametersGraph);
		}
		
	}

	private void buildURLMapAndCacheSimple(String baseURL,String rootURI, Model extraParametersGraph) throws ModelException, UnknownURIException, ConfigurationException {
		TerminologySet currentSet=myInitializer.myFactory.getCheckedTerminologySet(rootURI);
		String myNSBit=currentSet.getLocalNamespace();	//TODO this needs a fix for trailing "/"
		
		// We still allow override of URLs
		Literal overrideBaseURL=SimpleQueriesProcessor.getOptionalLiteral(currentSet.getResource(), PublisherConfig.overrideBaseSiteProperty, extraParametersGraph);
		if(overrideBaseURL!=null) {
			if(overrideBaseURL.isLiteral()) {
				baseURL=overrideBaseURL.getValue().toString();
			}
			else throw new ConfigurationException("Override for "+rootURI+" does not have a valid literal :"+overrideBaseURL);
		}
		String collectionURL=baseURL+"/"+myNSBit;
		
		
		
		myInitializer.myCache.recordValue(rootURI, PublisherConfig.uriHasUrl, collectionURL);
		
		// Now going for individuals
		Iterator<TerminologyIndividual> myIndIter=currentSet.getAllKnownContainedInviduals().iterator();
		//System.out.println(">>>>For: "+collection.getURI()+" Total number of terms: "+collection.getAllKnownContainedInviduals().size());
		while(myIndIter.hasNext()) {
			TerminologyIndividual ind=myIndIter.next();
			String indNs=ind.getLocalNamespace();
			String indURL=collectionURL+"/"+indNs;
			myInitializer.myCache.recordValue(ind.getURI(),PublisherConfig.uriHasUrl,indURL);
		}
				
		Iterator<TerminologySet> childrenEnum=currentSet.getAllKnownContainedCollections().iterator();
		while(childrenEnum.hasNext()) {
			buildURLMapAndCacheSimple(collectionURL,childrenEnum.next().getURI(),extraParametersGraph);
		}
		
	}
	
	
	public void cleanCache() {
		myInitializer.myCache.forceCleanProp(PublisherConfig.uriHasUrl);	
		myInitializer.myCache.forceCleanProp(PublisherConfig.uriHasDisk);	
	}
	
	
	private class SimpleServer extends AbstractHandler
	{
	    public void handle(String target,
	                       Request baseRequest,
	                       HttpServletRequest request,
	                       HttpServletResponse response) 
	        throws IOException, ServletException
	    {
	        System.out.println("base was type: "+baseRequest.getContentType()+" URI: "+baseRequest.getRequestURI());
	    	System.out.println("request was type: "+request.getRequestURI());
	        //TODO here we need to do the proper thing!
	    	response.setContentType("text/html;charset=utf-8");
	        response.setStatus(HttpServletResponse.SC_OK);
	        baseRequest.setHandled(true);
	        response.getWriter().println("<h1>Hello World</h1>");
	    }
	}

}
