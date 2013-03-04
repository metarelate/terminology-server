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
		registerContainedCollection(myCollection,getDefaultVersion(),myCollection.getDefaultVersion());
		
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
		registerContainedIndividual(myIndividual,getDefaultVersion(),myIndividual.getDefaultVersion());		
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
		unregisterContainedEntity(toRemove,getDefaultVersion(),toRemove.getDefaultVersion());
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
	 */
	public Set<TerminologyIndividual> getIndividuals() {
		return getIndividuals(getDefaultVersion());
	}

	
	public Set<TerminologyIndividual> getIndividuals(String version) {
		Set<TerminologyIndividual>answer=new HashSet<TerminologyIndividual>();
		NodeIterator regItemIter=getStatements(version).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasRegisterItem);
		while(regItemIter.hasNext()) {
			RDFNode currRegItem=regItemIter.nextNode();
			if(currRegItem.isResource()) answer.add(myFactory.getOrCreateTerminologyIndividual(currRegItem.asResource().getURI()));
		}
		return answer;
	}

	/**
	 * This method is independent of this individual being backed by TDB.
	 * It could be abstracted, if different implementations are designed.
	 */
	public Set<TerminologySet> getCollections() {
		return getCollections(getDefaultVersion());
	}

	
	public Set<TerminologySet> getCollections(String version) {
		Set<TerminologySet>answer=new HashSet<TerminologySet>();
		NodeIterator subRegIter=getStatements(version).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasSubRegister);
		while(subRegIter.hasNext()) {
			RDFNode currSubReg=subRegIter.nextNode();
			if(currSubReg.isResource()) answer.add(((TerminologyFactoryTDBImpl)myFactory).getOrCreateTerminologySet(currSubReg.asResource().getURI()));
		}
		return answer;
	}

	
	public Set<TerminologySet> getAllKnownContainedCollections() {
		Set<TerminologySet>answer=new HashSet<TerminologySet>();
		//System.out.println(">>All collections");
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator subRegIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasSubRegister);
			while(subRegIter.hasNext()) {
				RDFNode currSubReg=subRegIter.nextNode();
				if(currSubReg.isResource()) answer.add(((TerminologyFactoryTDBImpl)myFactory).getOrCreateTerminologySet(currSubReg.asResource().getURI()));
			}
		}
		
		return answer;
	}

	public Set<TerminologyIndividual> getAllKnownContainedInviduals() {
		Set<TerminologyIndividual>answer=new HashSet<TerminologyIndividual>();
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator regItemIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, TDBModelsCoreConfig.hasRegisterItem);
			while(regItemIter.hasNext()) {
				RDFNode currRegItem=regItemIter.nextNode();
				if(currRegItem.isResource()) answer.add(((TerminologyFactoryTDBImpl)myFactory).getOrCreateTerminologyIndividual(currRegItem.asResource().getURI()));
			}
		}
		return answer;
	}

}
