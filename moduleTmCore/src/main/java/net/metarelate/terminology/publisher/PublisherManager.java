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
import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;
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
		Loggers.publishLogger.info("Publishing the terminology under "+rootURI+" as a set of web-files");
		String baseURL=null;
		String baseDisk=null;
		TerminologySet root=null;
		if(myInitializer.myFactory.terminologySetExist(rootURI)) {
			root=myInitializer.myFactory.getUncheckedTerminologySet(rootURI);
			if(root.isRoot()) {
				baseURL=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(), PublisherConfig.baseURLProperty);
				baseDisk=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(),PublisherConfig.diskPrefixProperty);
				Loggers.publishLogger.debug("From configuration files:  For "+rootURI+" found URL: "+baseURL+" , Disk: "+baseDisk);
			}
			else {
				Set<TerminologySet> uberRoots=root.getContainers(root.getLastVersion());
				TerminologySet uberRoot=null;
				if(uberRoots.size()==0) throw new WebWriterException(rootURI+" is not active in its last version. Try to publish an super-register");
				else if(uberRoots.size()>1) throw new ModelException(rootURI+ " in multiple containers");
				else uberRoot=uberRoots.iterator().next();
				baseURL=myInitializer.myCache.getValueFor(uberRoot.getURI(), PublisherConfig.uriHasUrl);
				baseDisk=myInitializer.myCache.getValueFor(uberRoot.getURI(), PublisherConfig.uriHasDisk);
				Loggers.publishLogger.debug("From cache: For "+rootURI+" found URL: "+baseURL+" , Disk: "+baseDisk);
				if(baseURL==null || baseDisk==null) throw new WebWriterException("You attempted to publish a register never published before, roots should be published first! ("+rootURI+")");
			}
		}
		else throw new WebWriterException("Only known registers can be published");
		Loggers.publishLogger.info("Cleaning cache for tree starting at: "+rootURI);
		cleanCacheTree(rootURI);
		Loggers.publishLogger.trace("Rebuilding URI/URL cache for tree starting at: "+rootURI);
		buildURLMapAndCache(baseURL,baseDisk,rootURI,extraInputGraph);
		//TODO should be hidden by design.
		myInitializer.myCache.synch();
		TemplateManager myTm=new TemplateManager(templateLocation);
		WebFilesVisitor vis=new WebFilesVisitor(myInitializer,myTm,baseURL);
		vis.setOverwriteFiles(overwrite);
		vis.crawl(root);	
	}
	
	public void publishDoc(String tag, String language, String fileName) throws ConfigurationException, IOException, ModelException, WebWriterException {
		Loggers.publishLogger.info("Publishing the terminology as a document for tag "+tag+" in language "+language);
		TemplateManager myTm=new TemplateManager(templateLocation); //TODO just in case we need a different design
		DocumentVisitor vis=new DocumentVisitor(myInitializer,myTm);
		vis.bind(tag, language);
		vis.writeToFile(fileName);
		// Optional latex bits ?
		
	}
	
	public void publishOnline(Model extraTriplesGraph, int port) throws Exception {
		Loggers.publishLogger.info("Publishing starting online server for the terminology at port: "+port);
		String baseURL=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(myInitializer.getConfigurationGraph(), PublisherConfig.baseURLProperty);
		Loggers.publishLogger.debug("From configuration files found base URL:  "+baseURL);
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
	    server.setHandler(new SimpleServer(myTm,baseURL));
	    server.start();
	    server.join();
	    

	}
	// TODO, this should be better designed, properties cleaned should be implicit.
	private void cleanCacheTree(String rootURI) throws ModelException {
		myInitializer.myCache.cleanValueFor(rootURI,PublisherConfig.uriHasUrl);
		myInitializer.myCache.cleanValueFor(rootURI,PublisherConfig.uriHasDisk);
		Loggers.publishLogger.trace("Cleaning uriHasUrl and uriHasDisk chache for (sub)tree staring at: "+rootURI);
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
	
	
	private class SimpleServer extends AbstractHandler {
		private static final int RDFXMLOUT=1;
		private static final int TTLOUT=2;
		private static final int HTMLOUT=3;
		private String langOut=CoreConfig.DEFAULT_LANGUAGE;
		private int modeOut=HTMLOUT;
		private TemplateManager tm;
		private String baseURL="";
	    public SimpleServer(TemplateManager myTm, String baseURL) {
	    		super();
			tm=myTm;
			this.baseURL=baseURL;
		}
		public void handle(String target,
	                       Request baseRequest,
	                       HttpServletRequest request,
	                       HttpServletResponse response) 
	        throws IOException, ServletException {
	    		//TODO finding the requested url should be cleaner!
	    		String portBit="";
	    		int port=request.getServerPort();
	    		if(port!=80) portBit=new Integer(port).toString();
	    		portBit=":"+portBit;
	    		String urlRequested="http://"+request.getServerName()+portBit+baseRequest.getRequestURI();
	    		String uri="";
	    		String version=null;
	    		String language=CoreConfig.DEFAULT_LANGUAGE;
	    		Loggers.publishLogger.debug("Serving request for: "+urlRequested);
	    		if(urlRequested.endsWith(".rdf")) {
	    			urlRequested=urlRequested.replace(".rdf", "");
	    			if(urlRequested.endsWith(PublisherConfig.individualStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.individualStemString, "");
	    			if(urlRequested.endsWith(PublisherConfig.setStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.setStemString, "");
	    			String[] res=getURIFromURL(urlRequested);
	    			uri=res[0];
	    			version=res[1];
	    			if(uri==null) {
	    				unknownURIError(baseRequest,response,urlRequested);
	    			}
	    			else {
	    				modeOut=RDFXMLOUT;
	    			}
	    		}
	    		else if(urlRequested.endsWith(".ttl")) {
	    			urlRequested=urlRequested.replace(".ttl", "");
	    			if(urlRequested.endsWith(PublisherConfig.individualStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.individualStemString, "");
	    			if(urlRequested.endsWith(PublisherConfig.setStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.setStemString, "");
	    			String[] res=getURIFromURL(urlRequested);
	    			uri=res[0];
	    			version=res[1];
	    			if(uri==null) {
	    				unknownURIError(baseRequest,response,urlRequested);
	    			}
	    			else {
	    				modeOut=TTLOUT;
	    			}
	    		}
	    		else if(urlRequested.endsWith(".html")) {
	    			urlRequested=urlRequested.replace(".html", "");
	    			int lastIndexOfDot=urlRequested.lastIndexOf(".");
	    			if(lastIndexOfDot>0) {
	    				language=urlRequested.substring(lastIndexOfDot+1);
	    				urlRequested=urlRequested.substring(0,lastIndexOfDot);
	    			} 
	    			if(urlRequested.endsWith(PublisherConfig.individualStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.individualStemString, "");
	    			if(urlRequested.endsWith(PublisherConfig.setStemString))
	    				urlRequested=urlRequested.replace(PublisherConfig.setStemString, "");
	    			String[] res=getURIFromURL(urlRequested);
	    			uri=res[0];
	    			version=res[1];
	    			if(uri==null) {
	    				unknownURIError(baseRequest,response,urlRequested);
	    			}	
	    			else {
	    				modeOut=HTMLOUT;
	    			}
	    		}	
	    		else {
	    			String[] res=getURIFromURL(urlRequested);
	    			uri=res[0];
	    			version=res[1];
	    			if(uri==null) {
	    				unknownURIError(baseRequest,response,urlRequested);
	    			}
	    			else {
	    				//TODO perhaps we could implement a better content negotation!
	    				modeOut=HTMLOUT;
	    				String acceptHeader=baseRequest.getHeader("Accept");
	    				if(acceptHeader.contains("application/rdf+xml")) modeOut=RDFXMLOUT;
	    				if(acceptHeader.contains("text/turtle")) modeOut=TTLOUT;
	    				if(acceptHeader.contains("text/html")) modeOut=HTMLOUT;
	    			}
	    		}
	    	
	    		//TODO here we need to do the proper thing!
	    		Loggers.publishLogger.debug("Page served has URI: "+uri
	    				+" , Version: "+version+
	    				" , Language: "+language+
	    				" , content-type (internal code): "+langOut);
	    		if(modeOut==HTMLOUT) {
	    			response.setContentType("text/html;charset=utf-8");
	    			response.setStatus(HttpServletResponse.SC_OK);
	    			baseRequest.setHandled(true);
	    			String result="";
	    			try {
	    				if(myInitializer.myFactory.terminologyIndividualExist(uri)) {
	    					TerminologyIndividual ind=myInitializer.myFactory.getUncheckedTerminologyIndividual(uri);
	    					if(version==null) version=ind.getLastVersion();
	    					result=tm.getPageForLang(language, ind, version, 0, myInitializer.myCache.getValueFor(uri, PublisherConfig.uriHasUrl), myInitializer.myCache,  myInitializer.myFactory.getLabelManager(), myInitializer.myFactory.getBackgroundKnowledgeManager(), baseURL);

	    				}
	    				else if (myInitializer.myFactory.terminologySetExist(uri)) {
	    					TerminologySet set=myInitializer.myFactory.getUncheckedTerminologySet(uri);
	    					if(version==null) version=set.getLastVersion();
	    					result=tm.getPageForLang(language, set, version, 0,  myInitializer.myCache.getValueFor(uri, PublisherConfig.uriHasUrl), myInitializer.myCache, myInitializer.myFactory.getLabelManager(), myInitializer.myFactory.getBackgroundKnowledgeManager(), baseURL);

	    				}
	    				else {
	    					result="Sorry, I'm terribly confused!";
	    					// This shouldn't happen! TODO classes and typing are to be re-designed: too many types checks!
	    				}
	    			} catch (Exception e){
	    				e.printStackTrace();
	    			}; //TODO we should add something here.
	    			
	    			response.getWriter().println(result);
	    		}
	    		else if(modeOut==RDFXMLOUT) {
	    			response.setContentType("application/rdfxml;charset=utf-8");
	    			response.setStatus(HttpServletResponse.SC_OK);
	    			baseRequest.setHandled(true);
	    			TerminologyEntity entity=myInitializer.myFactory.getUncheckedTerminologyEntity(uri);
	    			if(version==null) version=entity.getLastVersion();
	    			Model modelToWrite=RDFrenderer.prepareModel(entity, version);
	    			modelToWrite.setNsPrefixes(myInitializer.getPrefixMap());
	    			modelToWrite.write(response.getWriter(),"RDF/XML-ABBREV");
	    			
	    		}
	    		else if(modeOut==TTLOUT) {
	    			response.setContentType("text/turtle;charset=utf-8");
	    			response.setStatus(HttpServletResponse.SC_OK);
	    			baseRequest.setHandled(true);
	    			TerminologyEntity entity=myInitializer.myFactory.getUncheckedTerminologyEntity(uri);
	    			if(version==null) version=entity.getLastVersion();
	    			Model modelToWrite=RDFrenderer.prepareModel(entity, version);
	    			modelToWrite.setNsPrefixes(myInitializer.getPrefixMap());
	    			modelToWrite.write(response.getWriter(),"TURTLE");
	    		}
		}
		private String[] getURIFromURL(String urlRequested) {
			String uri=null;
			String[] result=new String[2];
			Loggers.publishLogger.trace("Looking for URI for URL: "+urlRequested);
			uri=myInitializer.myCache.getSubjectForValue(urlRequested, PublisherConfig.uriHasUrl);
			if(uri!=null) {
				Loggers.publishLogger.trace("Found URI: "+uri);
				result[0]=uri;
				result[1]=null;
				return result;
			}
			//URI not found, does the URL ends with "/" ? We can check without
			if(urlRequested.endsWith("/")) urlRequested=urlRequested.substring(0,urlRequested.length()-1);
			uri=myInitializer.myCache.getSubjectForValue(urlRequested, PublisherConfig.uriHasUrl);
			if(uri!=null) {
				Loggers.publishLogger.trace("Found URI: "+uri);
				result[0]=uri;
				result[1]=null;
				return result;
			}
			//Maybe the URI has a trailing version number (we already removed the last "/")
			int indexOfSlash=urlRequested.lastIndexOf("/");
			String putativeURL=urlRequested.substring(0,indexOfSlash);
			String putativeURL2=putativeURL+"/";
			String putativeVersion=urlRequested.substring(indexOfSlash+1);
			uri=myInitializer.myCache.getSubjectForValue(putativeURL, PublisherConfig.uriHasUrl);
			if(uri==null) uri=myInitializer.myCache.getSubjectForValue(putativeURL2, PublisherConfig.uriHasUrl);
			if(uri!=null) {
				// TODO in theory we should check if this is valid version...
				String[] versions=myInitializer.myFactory.getUncheckedTerminologyEntity(uri).getVersions();
				boolean found=false;
				for(String version:versions)
					if(version.equals(putativeVersion))
						found=true;
				if(found) {
					result[0]=uri;
					result[1]=putativeVersion;
					return result;
				}
			}
			result[0]=null;
			result[1]=null;
			return result;
			
		}
	}
	
	private void unknownURIError(Request baseRequest,HttpServletResponse response, String url) throws IOException {
		response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Sorry!</h1>");
        response.getWriter().println("I cannot recover any enity from URL : "+url);
    }
	
	private void unknownVersionError(Request baseRequest,HttpServletResponse response, String url,String version) throws IOException {
		response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<h1>Sorry!</h1>");
        response.getWriter().println("Cannot find anything for URL: "+url+" at version "+version);
    }

}
