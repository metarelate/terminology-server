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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;

public class TerminologyEntityTDBImpl implements TerminologyEntity{
	protected TerminologyFactory myFactory=null;	//The factory responsible for the construction of this entity
	private String uri=null; 							//This is the uri of the entity. 
	protected Resource myRes=null; 						//A resource representation of the entity.
	
	//private String defaultVersion=null;
	//private String localNamespace=null;					//This is in memory only! see get/set for an explanation.
	
	private Versioner versioner=null;
	
	private Dataset myDataset=null;
	private Model globalGraph=null;
	
	
	
	/**
	 * Constructor for this entity.
	 * This constructor should be called indirectly by the children classes,
	 * that should be created only by the factory. Hence the visibility of this constructor is limited to the package.
	 * @param uri the URI of the entity.
	 * @param dataset the TDB dataset where the terminology is defined
	 * @param factory the factory responsible for the creation of this entity
	 */
	TerminologyEntityTDBImpl(String uri, TerminologyFactoryTDBImpl factory) {
		this.uri=uri;
		myFactory=factory;
		myRes=ResourceFactory.createResource(uri);
		versioner=new Versioner(this);
		myDataset=factory.getDataset();
		globalGraph=myDataset.getNamedModel(CoreConfig.globalModel);		
	}
	
	public String getURI() {
		return uri;
	}
		
	public Resource getResource() {
		return myRes;
	}
		
	public TerminologyFactory getFactory() {
		return myFactory;
	}
	
	public String getLabel(String version) {
		return getGenericVersionSpecificStringValueObject(MetaLanguage.labelProperty, version);
	}

	public String getLabel(String version, String language) {
		String result= getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.labelProperty, version,language);
		if(result==null) result=getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.labelProperty, version,CoreConfig.DEFAULT_LANGUAGE);
		if(result==null) result=getLabel(version);
		return result;
	}
	
	/**
	 * Note: the information held is held in memory.
	 * @see {@link TerminologyEntityTDBImpl#getLocalNamespace}
	 * TODO it would be better if the namespace was persisted as an endurant information. 
	 */
	public void setLocalNamespace(String lns) {
		StmtIterator toRemove=globalGraph.listStatements(myRes,MetaLanguage.nameSpaceProperty,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(myRes,MetaLanguage.nameSpaceProperty,ResourceFactory.createPlainLiteral(lns)));
		TDB.sync(globalGraph);
	}
	
	/**
	 * This implementation is asymmetric in its behavior between get and set.
	 * returns the namespace as it was set via the set method, if this information is still in memory,
	 * the last fragment of the URL after the last "/" otherwise.
	 * This asymmetry works only for the current usage of these functions:
	 * A constructor generates all resources, then (with in-memory only information still present) build the terminology structure. During this second step,
	 * the method getNamespace is called and the information returned is used to construct the URL/URI of the entity.
	 * getNamespace is also called by the renderer to build the directory structure. In this case it relies on the assumption that the namespace can be
	 * found as the string after the last "/" of the entity URI.
	 * TODO it would be better if the namespace was persisted as an endurant information. 
	 * @see {@link TerminologyEntityTDBImpl#setLocalNamespace}
	 */
	public String getLocalNamespace() {
		//TODO note: we always only consider the last version for the namespace, perhaps this shouldn't be in the version graph!
		String localNamespace=SimpleQueriesProcessor.getOptionalLiteralValueAsString(myRes, MetaLanguage.nameSpaceProperty, globalGraph);
		if(localNamespace==null) {
			String uri=myRes.getURI();
			localNamespace=uri.substring(uri.lastIndexOf('/')+1);
		}
		// TODO debug
		System.out.println("for "+getURI()+" namespace was "+localNamespace);
		return localNamespace;
	}
	
	/**
	 * basically equivalent to getLocalNamespace, unless the local namespace is set otherwise, and this information is still in memory.
	 * Possibly a redundant method.
	 * TODO should be made redundant and harmonized respect to {@link TerminologyEntityTDBImpl#getLocalNamespace} and  {@link TerminologyEntityTDBImpl#setLocalNamespace}
	 * TODO why public ?
	 * @return the last bit of the URI or the whole URI if no "/" is found in it.
	 */
	public String getLastURIBit() {
		return uri.substring(uri.lastIndexOf('/')+1);
	}
	
	public String getNotation(String version) {
		return getGenericVersionSpecificStringValueObject(MetaLanguage.notationProperty, version);

	}
	
	public Set<TerminologySet> getContainers(String version) throws ModelException  {
		Set<TerminologySet>answer=new HashSet<TerminologySet>();
		NodeIterator superRegIter=getStatements(version).listObjectsOfProperty(myRes,TDBModelsCoreConfig.definedInRegister);
		while(superRegIter.hasNext()) {
			RDFNode currSupReg=superRegIter.nextNode();
			if(currSupReg.isResource())
				try {
					answer.add(((TerminologyFactoryTDBImpl)myFactory).getCheckedTerminologySet(currSupReg.asResource().getURI()));
				} catch (UnknownURIException e) {
					e.printStackTrace();
					throw new ModelException("Inconsistent container: "+currSupReg.asResource().getURI()+" for "+getURI());
				}
		}
		return answer;
	}
	
	public void setStateURI(String uri, String version) {
		// note: there's only one!
		Model myGraph=myDataset.getNamedModel(getVersionURI(version));
		StmtIterator toRemove=myGraph.listStatements(myRes,TDBModelsCoreConfig.hasStateURI,(Resource)null);
		myGraph.remove(toRemove);
		myGraph.add(myGraph.createStatement(myRes,TDBModelsCoreConfig.hasStateURI,globalGraph.createResource(uri)));
	}

	// Note: new
	public String getStateURI(String version) {
		return getGenericVersionSpecificURIObject(TDBModelsCoreConfig.hasStateURI,version);
	}

	public void setOwnerURI(String uri) {
		StmtIterator toRemove=globalGraph.listStatements(myRes,MetaLanguage.hasManagerProperty,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(myRes, MetaLanguage.hasManagerProperty, globalGraph.createResource(uri)));
	}
	
	public String getOwnerURI() {
		//Note there's only one!
		NodeIterator ownerIter=globalGraph.listObjectsOfProperty(myRes, MetaLanguage.hasManagerProperty);
		if(ownerIter.hasNext())
			return ownerIter.next().asResource().getURI();
		return null;
	}
		
	public void setActionURI(String actionURI, String version) {
		StmtIterator toRemove=globalGraph.listStatements(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionURI,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionURI,globalGraph.createResource(actionURI)));
	}

	public String getActionURI(String version) {
		// note: there's only one!
		String actionRes=null;
		NodeIterator ansIter=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)), TDBModelsCoreConfig.hasActionURI);
		if(ansIter.hasNext()) {
			RDFNode ans=ansIter.next();
			if(ans.isURIResource()) actionRes=ans.asResource().getURI();
		}
		return actionRes;
		

	}

	public void setActionAuthorURI(String actionAuthorURI, String version) {
		StmtIterator toRemove=globalGraph.listStatements(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionAuthorURI,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionAuthorURI,globalGraph.createResource(actionAuthorURI)));

	}

	
	public String getActionAuthorURI(String version) {
		// note: there's only one!
		String authorRes=null;
		NodeIterator ansIter=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)), TDBModelsCoreConfig.hasActionAuthorURI);
		if(ansIter.hasNext()) {
			RDFNode ans=ansIter.next();
			if(ans.isURIResource()) authorRes=ans.asResource().getURI();
		}
		return authorRes;
	}
	
	public void setActionDate(String actionDate, String version) {
		//Model myGraph=myDataset.getNamedModel(getVersionURI(version));
		StmtIterator toRemove=globalGraph.listStatements(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionDate,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionDate,globalGraph.createLiteral(actionDate)));

	}

	public String getActionDate(String version) {
		// note: there's only one!
		String dateRes=null;
		NodeIterator ansIter=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)), TDBModelsCoreConfig.hasActionDate);
		if(ansIter.hasNext()) {
			RDFNode ans=ansIter.next();
			if(ans.isLiteral()) dateRes=ans.asLiteral().getValue().toString();
		}
		return dateRes;
	}
	
	public void setActionDescription(String actionDescription, String version) {
		StmtIterator toRemove=globalGraph.listStatements(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionDescription,(Resource)null);
		globalGraph.remove(toRemove);
		globalGraph.add(globalGraph.createStatement(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasActionDescription,globalGraph.createLiteral(actionDescription)));
	}

	public String getActionDescription(String version) {
		// note: there's only one!
		String descRes=null;
		NodeIterator ansIter=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)), TDBModelsCoreConfig.hasActionDescription);
		if(ansIter.hasNext()) {
			RDFNode ans=ansIter.next();
			if(ans.isLiteral()) descRes=ans.asLiteral().getValue().toString();
		}
		return descRes;
	}

	//public void setDefaultVersion(String version) {
	//	this.defaultVersion=version;
	//}
	
	//public String getDefaultVersion() {
	//	return this.defaultVersion;
	//}

	public void setIsVersioned(boolean isVersioned) {
		StmtIterator toRemove=globalGraph.listStatements(myRes,TDBModelsCoreConfig.isVersionedProperty,(Resource)null);
		globalGraph.remove(toRemove);
		if(isVersioned) globalGraph.add(globalGraph.createStatement(myRes, TDBModelsCoreConfig.isVersionedProperty, globalGraph.createLiteral("TRUE")));

	}

	public boolean isVersioned() {
		// note: there's only one!
		String versionedString=getGenericEndurantStringValueObject(TDBModelsCoreConfig.isVersionedProperty);
		if(versionedString==null) return false;
		else{
			if(versionedString.equals("TRUE")) return true;
			else return false;
		}
	}
	
	public String[] getVersions() {
		Set<String> tempVersions=new HashSet<String>();
		NodeIterator versModelIter=globalGraph.listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasVersionURIProperty);
		while(versModelIter.hasNext()) {
			RDFNode tempValue=versModelIter.nextNode();
			if(tempValue.isResource()) {
				NodeIterator versionLabelIter=globalGraph.listObjectsOfProperty(tempValue.asResource(), MetaLanguage.hasVersionProperty);
				while(versionLabelIter.hasNext()) {
					RDFNode tempValue2=versionLabelIter.nextNode();
					if(tempValue2.isLiteral()) tempVersions.add(tempValue2.asLiteral().getValue().toString());

				}
			}
		}
		return tempVersions.toArray(new String[0]);
	}
	
	public  String getVersionURI(String version) {
		return uri+"/"+version;
	}
	
	public void tagVersion(String version,String tag) {
		globalGraph.add(globalGraph.createResource(getVersionURI(version)),MetaLanguage.hasTag,globalGraph.createLiteral(tag));

	}

	public String[] getVersionsForTag(String tag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String[] getTagsForVersion(String version) {
		Set<String> versions=new HashSet<String>();
		NodeIterator objIter=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)),MetaLanguage.hasTag);
		while(objIter.hasNext()) {
			RDFNode obj=objIter.nextNode();
			if(obj.isLiteral()) versions.add(obj.asLiteral().getValue().toString());
		}
		return versions.toArray(new String[0]);
	}

	public int getNumberOfVersions() {
		NodeIterator versIter=globalGraph.listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasVersionURIProperty);
		return versIter.toSet().size();
	}

	public void registerVersion(String version) {
		globalGraph.add(globalGraph.createStatement(myRes, TDBModelsCoreConfig.hasVersionURIProperty, globalGraph.createResource(getVersionURI(version))));
		globalGraph.add(globalGraph.createStatement(globalGraph.createResource(getVersionURI(version)), MetaLanguage.hasVersionProperty, globalGraph.createLiteral(version)));

	}
	
	public boolean isLastVersion(String version) {
		return versioner.isLastVersion(version);
	}
	
	/**
	 * This method is used by the Versioner object
	 */
	public String getPreviousVersion(String version) {
		NodeIterator preVersions=globalGraph.listObjectsOfProperty(globalGraph.createResource(getVersionURI(version)),TDBModelsCoreConfig.hasPreviousVersionProperty);
		while(preVersions.hasNext()) {
			RDFNode ver=preVersions.nextNode();
			if(ver.isResource()) {
				NodeIterator verValue=globalGraph.listObjectsOfProperty(ver.asResource(),MetaLanguage.hasVersionProperty);
				if(verValue.hasNext()) {
					RDFNode name=verValue.nextNode();
					if(name.isLiteral()) return name.asLiteral().getValue().toString();
				}
			}
		}
		return null;
	}
	
	/**
	 * This method is used by the Versioner object
	 */
	public String getNextVersion(String version) {
		ResIterator nextVersions=globalGraph.listSubjectsWithProperty(TDBModelsCoreConfig.hasPreviousVersionProperty,globalGraph.createResource(getVersionURI(version)));
		while(nextVersions.hasNext()) {
			Resource ver=nextVersions.nextResource();
			NodeIterator verValue=globalGraph.listObjectsOfProperty(ver.asResource(),MetaLanguage.hasVersionProperty);
			if(verValue.hasNext()) {
				RDFNode name=verValue.nextNode();
					if(name.isLiteral()) return name.asLiteral().getValue().toString();
			}
			
		}
		return null;
	}
	
	public boolean hasPreviousVersion(String version) {
		return versioner.hasPreviousVersion(version);
	}
	
	public String getLastVersion() {
		return versioner.getNewestVersion();
	}
	
	public String getGenerationDate() {
		return versioner.getGenerationDate();
		
	}
	public String getLastUpdateDate() {
		return versioner.getLastUpdateDate();
	}
	
	public void linkVersions(String lastVersion, String newVersion) {
		globalGraph.add(globalGraph.createStatement(
				globalGraph.createResource(getVersionURI(newVersion)),
				TDBModelsCoreConfig.hasPreviousVersionProperty,
				globalGraph.createResource(getVersionURI(lastVersion))
				));
		
	}
	
	public void replaceStatements(Model statementsAsModel, String version) {
		myDataset.getNamedModel(getVersionURI(version)).removeAll();
		myDataset.getNamedModel(getVersionURI(version)).add(statementsAsModel);
	}

	public void addStatements(Model statementsAsModel, String version) {
		myDataset.getNamedModel(getVersionURI(version)).add(statementsAsModel);

	}

	public Model getStatements(String version) {
		return myDataset.getNamedModel(getVersionURI(version));
	}
	
	//TODO why is this forced to be public if in the interface it is public within the package ?
	public String getGenericVersionSpecificStringValueObjectByLanguage(
			Property property, String version, String language) {
		String result=null;
		NodeIterator myResults= myDataset.getNamedModel(getVersionURI(version)).listObjectsOfProperty(getResource(), property);
		while(myResults.hasNext()) {
			RDFNode litResult=myResults.nextNode();
			if(litResult.isLiteral()) {
				if(litResult.asLiteral().getLanguage().toString().equals(language)) {
					result=litResult.asLiteral().getValue().toString();
				}
			}
				
				
		}
		return result;
	}
	
	//TODO why is this forced to be public if in the interface it is public within the package ?
	public String getGenericVersionSpecificStringValueObject(Property property,
			String version) {
		String result=null;
		NodeIterator myResults= myDataset.getNamedModel(getVersionURI(version)).listObjectsOfProperty(getResource(), property);
		if(myResults.hasNext()) {
			RDFNode myRes=myResults.nextNode();
			if(myRes.isLiteral()) result=((Literal)myRes).getValue().toString();
		}
		return result;
	}
	
	//TODO why is this forced to be public if in the interface it is public within the package ?
	public String getGenericVersionSpecificURIObject(Property property,
			String version) {
		String result=null;
		NodeIterator myResults= myDataset.getNamedModel(getVersionURI(version)).listObjectsOfProperty(getResource(), property);
		if(myResults.hasNext()) {
			RDFNode myRes=myResults.nextNode();
			if(myRes.isURIResource()) result=myRes.asResource().getURI();
		}
		return result;
	}
	
	//TODO why is this forced to be public if in the interface it is public within the package ?
	public String getGenericEndurantStringValueObject(Property property) {
		String result=null;
		NodeIterator myResults= globalGraph.listObjectsOfProperty(getResource(), property);
		if(myResults.hasNext()) {
			RDFNode myRes=myResults.nextNode();
			if(myRes.isLiteral()) result=((Literal)myRes).getValue().toString();
		}
		return result;
	}

	//TODO why is this forced to be public if in the interface it is public within the package ?
	public String getGenericEndurantURIObject(Property property) {
		String result=null;
		NodeIterator myResults= globalGraph.listObjectsOfProperty(getResource(), property);
		if(myResults.hasNext()) {
			RDFNode myRes=myResults.nextNode();
			if(myRes.isURIResource()) result=myRes.asResource().getURI();
		}
		return result;
	}

	public void synch() {
		TDB.sync(myDataset);
		TDB.sync(globalGraph);
		// TODO calls to synch should be double checked (arguably, it's a TerminologyManager issue)
		
	}

	public String[] getVersionsChainFor(String version) {
		ArrayList<String> versions=new ArrayList<String>();
		String currentVersion=getLastVersion();
		versions.add(currentVersion);
		while(getPreviousVersion(currentVersion)!=null) {
			currentVersion=getPreviousVersion(currentVersion);
			versions.add(currentVersion);
		}
		return versions.toArray(new String[0]);
	}


	
	
}
