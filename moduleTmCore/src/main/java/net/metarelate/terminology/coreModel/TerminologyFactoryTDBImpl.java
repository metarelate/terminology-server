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
	
package net.metarelate.terminology.coreModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
/**
 * A jena TDB based implementation of @see TerminologyFactory . Refer to the interface for help.
 * @author andrea_splendiani
 *
 */
public class TerminologyFactoryTDBImpl implements TerminologyFactory {
	private Dataset myDataset=null;
	private Model globalGraph=null;
	private Model labelGraph=null;
	private Model extraGraph=null;
	
	private LabelManager myLabelManager=null;
	private BackgroundKnowledgeManager myBackgroundKnowledgeManager=null;
	
	//private Hashtable<String,TerminologyIndividual> alreadyCreatedIndividuals;
	//private Hashtable<String,TerminologySet> alreadyCreatedSets;
	
	public TerminologyFactoryTDBImpl(String tdbLocation) {
		super();
		//alreadyCreatedIndividuals=new Hashtable<String,TerminologyIndividual>();
		//alreadyCreatedSets=new Hashtable<String,TerminologySet>();
		myDataset=TDBFactory.createDataset(tdbLocation);
		globalGraph=myDataset.getNamedModel(CoreConfig.globalModel);
		labelGraph=myDataset.getNamedModel(CoreConfig.labelModel);
		extraGraph=myDataset.getNamedModel(CoreConfig.extraModel);
		myLabelManager=new LabelManagerTDBImpl(labelGraph);
		myBackgroundKnowledgeManager=new BackgroundKnowledgeManagerTDBImpl(extraGraph);
	}
	
	Dataset getDataset() {
		return myDataset;
	}

	/**
	 * Creates a new TerminologySet (versioned), initialized at init-version. 
	 * Throws exception if the TerminologySet already exists.
	 * @throws ImporterException 
	 */
	public TerminologySet createNewVersionedTerminologySet(String uri) throws ImporterException {
		if(terminologyEntityExist(uri)) throw new ImporterException("Attempt to create a new Set when an entity with the same URI exists: "+uri);
		TerminologySetTDBImpl result= new TerminologySetTDBImpl(uri, this);
		result.setIsVersioned(true);
		result.registerVersion(CoreConfig.VERSION_INIT);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		TDB.sync(globalGraph);
		return result;
	}

	/**
	 * Creates a new TerminologySet (un-versioned), initialized at default-version. 
	 * Throws exception if the TerminologySet already exists.
	 * @throws ImporterException 
	 */
	public TerminologySet createNewUnversionedTerminologySet(String uri) throws ImporterException {
		if(terminologyEntityExist(uri)) throw new ImporterException("Attempt to create a new Set when an entity with the same URI exists: "+uri);
		TerminologySetTDBImpl result= new TerminologySetTDBImpl(uri, this);
		result.setIsVersioned(false);
		result.registerVersion(CoreConfig.VERSION_DEFUALT);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		TDB.sync(globalGraph);
		return result;
	}
	
	
	/**
	 * Returns a TerminologySet already known for a given URI. 
	 * Throw exception if not such TerminologySet has been defined.
	 */
	public TerminologySet getCheckedTerminologySet(String uri) throws UnknownURIException {
		//My new test!
		if(!terminologySetExist(uri)) throw new UnknownURIException(uri);
		TerminologySetTDBImpl result=new TerminologySetTDBImpl(uri, this);
		return result;

	}
	
	public TerminologySet getUncheckedTerminologySet(String uri)  {
		//My new test!
		if(!terminologySetExist(uri)) return null;
		else return new TerminologySetTDBImpl(uri, this);
		

	}

	

	
	
	
	/**
	 * Creates a new TerminologySet (versioned), initialized at init-version. 
	 * Throws exception if the TerminologySet already exists.
	 * @throws ImporterException 
	 */
	public TerminologyIndividual createNewVersionedTerminologyIndividual(String uri) throws ImporterException {
		if(terminologyEntityExist(uri)) throw new ImporterException("Attempt to create a new Individual when an entity with the same URI exists: "+uri);
		TerminologyIndividualTDBImpl result= new TerminologyIndividualTDBImpl(uri, this);
		result.setIsVersioned(true);
		result.registerVersion(CoreConfig.VERSION_INIT);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
		TDB.sync(globalGraph);
		return result;
	}

	/**
	 * Creates a new TerminologySet (un-versioned), initialized at default-version. 
	 * Throws exception if the TerminologySet already exists.
	 * @throws ImporterException 
	 */
	public TerminologyIndividual createNewUnversionedTerminologyIndividual(String uri) throws ImporterException {
		if(terminologyEntityExist(uri)) throw new ImporterException("Attempt to create a new Individual when an entity with the same URI exists: "+uri);
		TerminologyIndividualTDBImpl result= new TerminologyIndividualTDBImpl(uri, this);
		result.setIsVersioned(false);
		result.registerVersion(CoreConfig.VERSION_DEFUALT);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
		TDB.sync(globalGraph);
		return result;
	}
	
	
	/**
	 * Returns a TerminologySet already known for a given URI. 
	 * Throw exception if not such TerminologySet has been defined.
	 */
	public TerminologyIndividual getCheckedTerminologyIndividual(String uri) throws UnknownURIException {
		//My new test!
		if(!terminologyIndividualExist(uri)) throw new UnknownURIException(uri);
		TerminologyIndividualTDBImpl result=new TerminologyIndividualTDBImpl(uri, this);
		return result;

	}
	
	public TerminologyIndividual getUncheckedTerminologyIndividual(String uri) {
		//My new test!
		if(!terminologyIndividualExist(uri)) return null;
		else return new TerminologyIndividualTDBImpl(uri, this);

	}
	
	

	public Collection<TerminologySet> getAllSets() {
		ResIterator setNodes=globalGraph.listSubjectsWithProperty(TDBModelsCoreConfig.hasTypeProperty,TDBModelsCoreConfig.TerminologySetType);
		ArrayList<TerminologySet> result=new ArrayList<TerminologySet>();
		while(setNodes.hasNext()) {
			Resource currSetRes=setNodes.nextResource();
			result.add(getUncheckedTerminologySet(currSetRes.getURI()));
		}
		return result;
		
	}

	public Collection<TerminologyIndividual> getAllIndividuals() throws ModelException {
		ResIterator setIndividuals=globalGraph.listSubjectsWithProperty(TDBModelsCoreConfig.hasTypeProperty,TDBModelsCoreConfig.TerminologyIndividualType);
		ArrayList<TerminologyIndividual> result=new ArrayList<TerminologyIndividual>();
		while(setIndividuals.hasNext()) {
			Resource currIndRes=setIndividuals.nextResource();
			result.add(getUncheckedTerminologyIndividual(currIndRes.getURI()));
		}
		return result;

	}

	public TerminologySet[] getRootCollections() throws ModelException {
		Loggers.coreLogger.trace("Finding root collections");
		ArrayList<TerminologySet> myRoots=new ArrayList<TerminologySet>();
		Collection<TerminologySet> mySets= getAllSets();
		Iterator<TerminologySet> mySetsIter=mySets.iterator();
		while(mySetsIter.hasNext()) {
			TerminologySet mySet=mySetsIter.next();
			if(mySet.isRoot()) {
				myRoots.add(mySet);
			}
		}
		return myRoots.toArray(new TerminologySet[myRoots.size()]);
		
	}

	public void synchRootCollections() {
		// TODO Nothing here!!!. Could prefactor the call to all roots here for performances
		
	}
	
	public LabelManager getLabelManager() {
		return myLabelManager;
	}
	public BackgroundKnowledgeManager getBackgroundKnowledgeManager() {
		return myBackgroundKnowledgeManager;
	}

	public Set<String> extractIndividualsWithMarchingValue(
			String textQueryString) {
		return extractTypedResourcesMatchingLiteratConstraint(MetaLanguage.terminologyIndividualType.getURI(),textQueryString);
	}

	public Set<String> extractSetsWithMarchingValue(String textQueryString) {
		return extractTypedResourcesMatchingLiteratConstraint(MetaLanguage.terminologySetType.getURI(),textQueryString);
	}
	
	private Set<String> extractTypedResourcesMatchingLiteratConstraint(String type, String constraint){
		
    	String queryString="select distinct ?s where {\n"+ 
    			"graph <"+CoreConfig.globalModel+">{ ?s a <"+type+">}\n"+
    			"graph ?g {?s ?p ?l .\n"+ 
    			"filter regex(?l,\""+constraint+"\")}\n"+
    			"}";
    Loggers.coreLogger.debug("Query: "+queryString);
    	QueryExecution queryExec=QueryExecutionFactory.create(queryString,myDataset);
    	ResultSet results = queryExec.execSelect();
    	Set<String> uriResult=new HashSet<String>();
    	while(results.hasNext()) {
    		QuerySolution currentRes=results.next();
    		if(currentRes.get("?s").isURIResource()) {
    			uriResult.add(((Resource)currentRes.get("?s")).getURI());
    		}
    	}
    	return uriResult;
    	
    			
	}

	public void synch() {
		Iterator<String> datasets=myDataset.listNames();
		ArrayList<String>datasetNames =new ArrayList<String>();
		while(datasets.hasNext()) datasetNames.add(datasets.next());
		for(String datasetName: datasetNames) {
			TDB.sync(myDataset.getNamedModel(datasetName));
		}
		
	}

	public Set<TerminologySet> getRootsForURI(String uri) throws ModelException, UnknownURIException  {
		TerminologyEntity myEntity=getCheckedTerminologyEntity(uri);
		return myEntity.getContainers(myEntity.getLastVersion());
	}

	public TerminologyEntity getCheckedTerminologyEntity(String uri) throws UnknownURIException {
		if(terminologyIndividualExist(uri)) return getCheckedTerminologyIndividual(uri);
		else if(terminologySetExist(uri)) return getCheckedTerminologySet(uri);
		else return null;
	}

	public TerminologyEntity getUncheckedTerminologyEntity(String uri) {
		if(terminologyIndividualExist(uri)) return getUncheckedTerminologyIndividual(uri);
		else if(terminologySetExist(uri)) return getUncheckedTerminologySet(uri);
		else return null;
	}

	public boolean terminologySetExist(String uri) {
		return globalGraph.contains(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		
	}
	
	public boolean terminologyIndividualExist(String uri) {
		return globalGraph.contains(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
	}
	
	public boolean terminologyEntityExist(String uri) {
		return terminologySetExist(uri) || terminologyIndividualExist(uri);
	}
	
	
}
