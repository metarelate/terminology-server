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
	
package net.metarelate.terminology.coreModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.publisher.PublisherVisitor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
/**
 * A jena-TDB based implementation of @see TerminologySet . Refer to the extended abstract classes and implemented interfaces for help
 * @author andrea_splendiani
 *
 */
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
		containerModel.add(myRes, MetaLanguage.definesProperty, myCollection.getResource());
		containedModel.add(myCollection.getResource(), MetaLanguage.definedInProperty, myRes);
		
	}

	public boolean isRoot() {
		String[] versions=getVersions();
		for(int v=0;v<versions.length;v++) {
			StmtIterator containerItem=getStatements(versions[v]).listStatements(myRes, MetaLanguage.definedInProperty , (Resource)null);
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
		containerModel.add(myRes, MetaLanguage.definesProperty, myIndividual.getResource());
		containedModel.add(myIndividual.getResource(),MetaLanguage.definedInProperty,myRes);
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
		containerModel.remove(myRes,MetaLanguage.definesProperty,myIndividual.getResource());
		
		containedModel.remove(myIndividual.getResource(),MetaLanguage.definedInProperty,myRes);
		
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
		NodeIterator regItemIter=getStatements(version).listObjectsOfProperty(myRes, MetaLanguage.definesProperty);
		while(regItemIter.hasNext()) {
			RDFNode currRegItem=regItemIter.nextNode();
			if(currRegItem.isResource())
				if(myFactory.terminologyIndividualExist(currRegItem.asResource().getURI()))
					answer.add(myFactory.getUncheckedTerminologyIndividual(currRegItem.asResource().getURI()));
				
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
		NodeIterator subRegIter=getStatements(version).listObjectsOfProperty(myRes, MetaLanguage.definesProperty);
		while(subRegIter.hasNext()) {
			RDFNode currSubReg=subRegIter.nextNode();
			if(currSubReg.isResource())
				if(myFactory.terminologySetExist(currSubReg.asResource().getURI()))
					answer.add(myFactory.getUncheckedTerminologySet(currSubReg.asResource().getURI()));
				
		}
		return answer;
	}

	
	public Set<TerminologySet> getAllKnownContainedCollections() throws ModelException {
		Map<String,TerminologySet>answer=new HashMap<String,TerminologySet>();
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator subRegIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, MetaLanguage.definesProperty);
			while(subRegIter.hasNext()) {
				RDFNode currSubReg=subRegIter.nextNode();
				if(currSubReg.isResource())
					if(myFactory.terminologySetExist(currSubReg.asResource().getURI()))
						answer.put(currSubReg.asResource().getURI(),myFactory.getUncheckedTerminologySet(currSubReg.asResource().getURI()));
					
			}
		}
		Set<TerminologySet> answer2=new HashSet<TerminologySet>();
		for(TerminologySet set:answer.values()) answer2.add(set);
		return answer2;
	}

	public Set<TerminologyIndividual> getAllKnownContainedInviduals() throws ModelException {
		Map<String,TerminologyIndividual>answer=new HashMap<String,TerminologyIndividual>();
		String[] myVersions=getVersions();
		for(int i=0;i<myVersions.length;i++) {
			NodeIterator regItemIter=getStatements(myVersions[i]).listObjectsOfProperty(myRes, MetaLanguage.definesProperty);
			while(regItemIter.hasNext()) {
				RDFNode currRegItem=regItemIter.nextNode();
				if(currRegItem.isResource())
					if(myFactory.terminologyIndividualExist(currRegItem.asResource().getURI()))
						answer.put(currRegItem.asResource().getURI(),myFactory.getUncheckedTerminologyIndividual(currRegItem.asResource().getURI()));
					
			}
			//for(TerminologyIndividual ans:answer) System.out.println(ans.getURI()); 
		}
		Set<TerminologyIndividual> answer2=new HashSet<TerminologyIndividual>();
		for(TerminologyIndividual ind:answer.values()) answer2.add(ind);
		return answer2;
	}

	public boolean containsEntity(TerminologyEntity myTerm) {
		return containsEntity(myTerm,getLastVersion());
	
	}

	private boolean containsEntity(TerminologyEntity myTerm, String version) {
		
		return getStatements(version).contains(myRes, MetaLanguage.definesProperty,myTerm.getResource());
		
		
	}

	public void accept(PublisherVisitor v) throws WebWriterException, IOException, ConfigurationException, ModelException {
		v.visit(this);
		
	}

	public boolean isSet() {
		return true;
	}

	public boolean isIndividual() {
		return false;
	}

}
