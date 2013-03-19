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

import java.util.HashSet;
import java.util.Set;

import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TerminologySetTDBImpl extends
		TerminologyEntityTDBImpl implements TerminologySet {

	TerminologySetTDBImpl(String uri,
			TerminologyFactoryTDBImpl factory) {
		super(uri, factory);
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public void registerContainedCollection(TerminologySet myCollection) {
		registerContainedCollection(myCollection,getLastVersion(),myCollection.getLastVersion());
		
	}

	public void registerContainedCollection(TerminologySet myCollection,
			String myVersion, String containedVersion) {
		Model containerModel=getStatements(myVersion);
		Model containedModel=myCollection.getStatements(containedVersion);
		containerModel.add(myRes, TDBModelsCoreConfig.hasSubRegister, myCollection.getResource());
		containedModel.add(myCollection.getResource(), TDBModelsCoreConfig.definedInRegister, myRes);
		
	}

	public boolean isRoot() {
		String[] versions=getVersions();
		for(int v=0;v<versions.length;v++) {
			StmtIterator containerItem=getStatements(versions[v]).listStatements(myRes, TDBModelsCoreConfig.definedInRegister , (Resource)null);
			if(containerItem.hasNext()) return false;
		}
		return true;
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public void registerContainedIndividual(TerminologyIndividual myIndividual) {
		registerContainedIndividual(myIndividual,getLastVersion(),myIndividual.getLastVersion());		
	}

	public void registerContainedIndividual(TerminologyIndividual myIndividual,
			String myVersion, String containedVersion) {
		Model containerModel=getStatements(myVersion);
		Model containedModel=myIndividual.getStatements(containedVersion);
		containerModel.add(myRes, TDBModelsCoreConfig.hasRegisterItem, myIndividual.getResource());
		containedModel.add(myIndividual.getResource(),TDBModelsCoreConfig.definedInRegister,myRes);
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public void unregisterContainedEntity(TerminologyEntity toRemove) {
		unregisterContainedEntity(toRemove,getLastVersion(),toRemove.getLastVersion());
	}

	
	public void unregisterContainedEntity(
			TerminologyEntity myIndividual, String containerVersion,
			String containedVersion) {
		// TODO fix here! (unclear what this comment was referring to!)
		Model containerModel=getStatements(containerVersion);
		Model containedModel=myIndividual.getStatements(containedVersion);
		
		//TODO it's either one or the other down here... perhaps we need to  move this higher in the abstraction chain
		containerModel.remove(myRes,TDBModelsCoreConfig.hasRegisterItem,myIndividual.getResource());
		containerModel.remove(myRes,TDBModelsCoreConfig.hasSubRegister,myIndividual.getResource());
		//System.out.println("REMOVING: "+myRes+" --- "+TDBModelsCoreConfig.hasRegisterItem+
		//		" ---"+myIndividual.getResource()+" FROM container version "+containerVersion);
		containedModel.remove(myIndividual.getResource(),TDBModelsCoreConfig.definedInRegister,myRes);
		/*
		Resource versionModel=globalGraph.createResource(myIndividual.getVersionURI(myVersion));
		globalGraph.remove(globalGraph.createStatement(versionModel, TDBModelsCoreConfig.hasRegisterItem, myIndividual.getResource()));
		*/
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 * @throws ModelException 
	 */
	public Set<TerminologyIndividual> getIndividuals() throws ModelException {
		return getIndividuals(getLastVersion());
	}

	
	public Set<TerminologyIndividual> getIndividuals(String version) throws ModelException {
		Set<TerminologyIndividual>answer=new HashSet<TerminologyIndividual>();
		NodeIterator regItemIter=getStatements(version).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasRegisterItem);
		while(regItemIter.hasNext()) {
			RDFNode currRegItem=regItemIter.nextNode();
			if(currRegItem.isResource())
				try {
					answer.add(myFactory.getCheckedTerminologyIndividual(currRegItem.asResource().getURI()));
				} catch (UnknownURIException e) {
					e.printStackTrace();
					throw new ModelException("Inconsisent individual "+currRegItem.asResource().getURI()+" for version "+version+" of "+getURI());
				}
		}
		return answer;
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 * @throws ModelException 
	 */
	public Set<TerminologySet> getCollections() throws ModelException {
		return getCollections(getLastVersion());
	}

	
	public Set<TerminologySet> getCollections(String version) throws ModelException {
		Set<TerminologySet>answer=new HashSet<TerminologySet>();
		NodeIterator subRegIter=getStatements(version).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasSubRegister);
		while(subRegIter.hasNext()) {
			RDFNode currSubReg=subRegIter.nextNode();
			if(currSubReg.isResource())
				try {
					answer.add(((TerminologyFactoryTDBImpl)myFactory).getCheckedTerminologySet(currSubReg.asResource().getURI()));
				} catch (UnknownURIException e) {
					e.printStackTrace();
					throw new ModelException("Inconsistent collection "+currSubReg.asResource().getURI()+" for version "+version+" of "+getURI());
				}
		}
		return answer;
	}

	
	public Set<TerminologySet> getAllKnownContainedCollections() throws ModelException {
		Set<TerminologySet>answer=new HashSet<TerminologySet>();
		//System.out.println(">>All collections");
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator subRegIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasSubRegister);
			while(subRegIter.hasNext()) {
				RDFNode currSubReg=subRegIter.nextNode();
				if(currSubReg.isResource())
					try {
						answer.add(((TerminologyFactoryTDBImpl)myFactory).getCheckedTerminologySet(currSubReg.asResource().getURI()));
					} catch (UnknownURIException e) {
						e.printStackTrace();
						throw new ModelException("Inconsistent collection "+currSubReg.asResource().getURI()+" for "+getURI());
					}
			}
		}
		
		return answer;
	}

	public Set<TerminologyIndividual> getAllKnownContainedInviduals() throws ModelException {
		Set<TerminologyIndividual>answer=new HashSet<TerminologyIndividual>();
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator regItemIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasRegisterItem);
			while(regItemIter.hasNext()) {
				RDFNode currRegItem=regItemIter.nextNode();
				if(currRegItem.isResource())
					try {
						answer.add(((TerminologyFactoryTDBImpl)myFactory).getCheckedTerminologyIndividual(currRegItem.asResource().getURI()));
					} catch (UnknownURIException e) {
						e.printStackTrace();
						throw new ModelException("Inconsistent individual "+currRegItem.asResource().getURI()+" for "+getURI());
					}
			}
		}
		return answer;
	}

}
