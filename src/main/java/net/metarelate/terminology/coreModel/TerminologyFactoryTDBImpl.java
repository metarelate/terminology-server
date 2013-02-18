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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

public class TerminologyFactoryTDBImpl implements TerminologyFactory {
	private Dataset myDataset=null;
	private Model globalGraph=null;
	private Model labelGraph=null;
	private Model extraGraph=null;
	
	private LabelManager myLabelManager=null;
	private BackgroundKnowledgeManager myBackgroundKnowledgeManager=null;
	
	private Hashtable<String,TerminologyIndividual> alreadyCreatedIndividuals;
	private Hashtable<String,TerminologySet> alreadyCreatedSets;
	
	public TerminologyFactoryTDBImpl(String tdbLocation) {
		super();
		alreadyCreatedIndividuals=new Hashtable<String,TerminologyIndividual>();
		alreadyCreatedSets=new Hashtable<String,TerminologySet>();
		myDataset=TDBFactory.createDataset(tdbLocation);
		globalGraph=myDataset.getNamedModel(TDBModelsCoreConfig.globalModel);
		labelGraph=myDataset.getNamedModel(TDBModelsCoreConfig.labelModel);
		extraGraph=myDataset.getNamedModel(TDBModelsCoreConfig.extraModel);
		myLabelManager=new LabelManagerTDBImpl(labelGraph);
		myBackgroundKnowledgeManager=new BackgroundKnowledgeManagerTDBImpl(extraGraph);
	}
	
	Dataset getDataset() {
		return myDataset;
	}

	public boolean terminologySetExist(String uri) {
		return globalGraph.contains(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		
	}

	public TerminologySet getOrCreateTerminologySet(String uri) {
		//My new test!
		if(alreadyCreatedSets.containsKey(uri)) return alreadyCreatedSets.get(uri);
		TerminologySetTDBImpl result=null;
		result= new TerminologySetTDBImpl(uri, this);
		if(result.getNumberOfVersions()==0) result.registerVersion(CoreConfig.INIT_VERSION);
		result.setDefaultVersion(result.getLastVersion());
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		alreadyCreatedSets.put(uri, result);
		return result;

	}

	public TerminologySet getOrCreateTerminologySet(String uri, String version) {
		TerminologySet result=null;
		if(alreadyCreatedSets.containsKey(uri)) {
			result= alreadyCreatedSets.get(uri);
			result.registerVersion(version);
			result.setDefaultVersion(version);
			return result;
		}
		result= new TerminologySetTDBImpl(uri, this);
		result.registerVersion(version);
		result.setDefaultVersion(version);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologySetType));
		alreadyCreatedSets.put(uri, result);
		result.setIsVersioned(true);
		return result;
	}

	public boolean terminologyIndividualExist(String uri) {
		return globalGraph.contains(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
	}
	
	public TerminologyIndividual getOrCreateTerminologyIndividual(String uri) {
		if(alreadyCreatedIndividuals.containsKey(uri)) return alreadyCreatedIndividuals.get(uri);
		TerminologyIndividualTDBImpl result=null;
		result= new TerminologyIndividualTDBImpl(uri, this);
		if(result.getNumberOfVersions()==0) result.registerVersion(CoreConfig.INIT_VERSION);
		result.setDefaultVersion(result.getLastVersion());
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
		alreadyCreatedIndividuals.put(uri, result);
		return result;
	
	}

	public TerminologyIndividual getOrCreateTerminologyIndividual(String uri,
			String version) {
		TerminologyIndividual result=null;;
		if(alreadyCreatedIndividuals.containsKey(uri)) {
			result= alreadyCreatedIndividuals.get(uri);
			result.registerVersion(version);
			result.setDefaultVersion(version);
			return result;
		}
		result= new TerminologyIndividualTDBImpl(uri, this);
		result.registerVersion(version);
		result.setDefaultVersion(version);
		globalGraph.add(ResourceFactory.createStatement(ResourceFactory.createResource(uri), TDBModelsCoreConfig.hasTypeProperty, TDBModelsCoreConfig.TerminologyIndividualType));
		alreadyCreatedIndividuals.put(uri, result);
		result.setIsVersioned(true);
		return result;

	}

	public Collection<TerminologySet> getAllSets() {
		ResIterator setNodes=globalGraph.listSubjectsWithProperty(TDBModelsCoreConfig.hasTypeProperty,TDBModelsCoreConfig.TerminologySetType);
		while(setNodes.hasNext()) {
			Resource currSetRes=setNodes.nextResource();
			if(!alreadyCreatedSets.containsKey(currSetRes.getURI())) {
				TerminologySetTDBImpl myTDBSet=new TerminologySetTDBImpl(currSetRes.getURI(),this);
				myTDBSet.setDefaultVersion(myTDBSet.getLastVersion());
				
				alreadyCreatedSets.put(currSetRes.getURI(),myTDBSet);	
			}
		}
		return alreadyCreatedSets.values();
		
	}

	public Collection<TerminologyIndividual> getAllIndividuals() {
		ResIterator setIndividuals=globalGraph.listSubjectsWithProperty(TDBModelsCoreConfig.hasTypeProperty,TDBModelsCoreConfig.TerminologyIndividualType);
		while(setIndividuals.hasNext()) {
			Resource currIndRes=setIndividuals.nextResource();
			if(!alreadyCreatedIndividuals.containsKey(currIndRes.getURI())) {
				TerminologyIndividualTDBImpl myTDBInd=new TerminologyIndividualTDBImpl(currIndRes.getURI(),this);
				myTDBInd.setDefaultVersion(myTDBInd.getLastVersion());
				alreadyCreatedIndividuals.put(currIndRes.getURI(),myTDBInd);	
			}
		}
		return alreadyCreatedIndividuals.values();

	}

	public TerminologySet[] getRootCollections() {
		ArrayList<TerminologySet> myRoots=new ArrayList<TerminologySet>();
		Collection<TerminologySet> mySets= getAllSets();
		Iterator<TerminologySet> mySetsIter=mySets.iterator();
		while(mySetsIter.hasNext()) {
			TerminologySet mySet=mySetsIter.next();
			if(mySet.isRoot()) myRoots.add(mySet);
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
    			"graph <"+TDBModelsCoreConfig.globalModel+">{ ?s a <"+type+">}\n"+
    			"graph ?g {?s ?p ?l .\n"+ 
    			"filter regex(?l,\""+constraint+"\")}\n"+
    			"}";
    	System.out.println("Query:");
    	System.out.println(queryString);
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
}
