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

package net.metarelate.terminology.modelBuilders;

import java.util.Iterator;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.NonConformantRDFException;
import net.metarelate.terminology.reasoning.ReasonerProxy;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Builds a terminology model.
 * The separation of concerns between this abstract class and its main implementation
 * {@link TerminologyModelBuilderFromRDF} is not well defined. 
 * In practice, this abstract class provides the higher-order logic of the building process (which steps, in which sequences),
 * while the implementing class provides detailed steps implementation.
 * However, the execution of steps is not independent from specific steps implementation.
 * This design choices is a residual from a previous design. At the moment, there is not a clear use case for
 * different "Builders". Should this happen later in the project, the distinction between abstract builder and implementation
 * could be cleaned. In alternative, this distinction can be dropped.
 *  
 * @author andreasplendiani
 *
 */

public abstract class TerminologyModelBuilder {
	protected TerminologyFactory myFactory=null;
	protected Model globalConfigurationModel=null;
	protected String globalOwnerURI=null;
	protected String actionMessage=CoreConfig.DEFAULT_FROM_RDF_DESCRIPTION;
	
	public TerminologyModelBuilder (TerminologyFactory factory) {
		this.myFactory=factory; 
	}
	
	/**
	 * Generates a terminology model from a collection of RDF resources which describe the terminology resources
	 * and that provides instruction for the model builder (e.g.: reasoning type, pragma operations, ...)
	 * @param unifiedConfigurationModel
	 * @throws NonConformantRDFException
	 */
	public void generateModel(Model unifiedConfigurationModel) throws NonConformantRDFException  {
		this.globalConfigurationModel=unifiedConfigurationModel;
		myFactory.getBackgroundKnowledgeManager().getModel().add(getPropertyMetadata());
		generateSets();
		generateIndividuals(); 
		buildContainmentStructure(); 
		ReasonerProxy.customReason(globalConfigurationModel);
		fillEntities();
		processPragma();
		myFactory.getLabelManager().registerLabels(getLabels());
	}
	
	
	
	
	protected abstract void generateSet(Resource setRes) throws NonConformantRDFException;
	protected abstract void generateIndividual(Resource individualRes) throws NonConformantRDFException ;
	
	protected  void fillEntities() {
		Iterator<TerminologySet> collectionsEnum=myFactory.getAllSets().iterator(); 
		Iterator<TerminologyIndividual> individualsEnum=myFactory.getAllIndividuals().iterator();
		while(collectionsEnum.hasNext()) {
			TerminologySet tempCollection=collectionsEnum.next();
			fillEntity(tempCollection,tempCollection.getDefaultVersion());
		}
		while(individualsEnum.hasNext()) {
			TerminologyIndividual myInd=individualsEnum.next();
			fillEntity(myInd,myInd.getDefaultVersion());
		}
		
	}
	protected abstract void fillEntity(TerminologyEntity entity,String version);
	protected abstract Model getLabels();
	protected abstract Model getPropertyMetadata();
	
	protected abstract void processPragma();

	protected void generateSets() throws NonConformantRDFException  {
		SSLogger.log("Generating Code Sets",SSLogger.DEBUG);
		ResIterator terminologySetIterator=globalConfigurationModel.listResourcesWithProperty(MetaLanguage.typeProperty,MetaLanguage.terminologySetType);		
		while(terminologySetIterator.hasNext()) {
			Resource currentEntitySet=terminologySetIterator.nextResource();
			generateSet(currentEntitySet);
		}
		
		
	}
	
	protected void generateIndividuals() throws NonConformantRDFException  {
		SSLogger.log("Generating Code Individual",SSLogger.DEBUG);
		ResIterator entityIndividualIter=globalConfigurationModel.listResourcesWithProperty(MetaLanguage.typeProperty,MetaLanguage.terminologyIndividualType);
		SSLogger.log("Generating Entity Individuals",SSLogger.DEBUG);
		while(entityIndividualIter.hasNext()) {
			Resource currentEntityIndividual=entityIndividualIter.nextResource();
			generateIndividual( currentEntityIndividual);
		}
	}	
	
	
	protected abstract void buildContainmentStructure();



	
	public void setGlobalOwnerURI(String ownerURI) {
		this.globalOwnerURI=ownerURI;
		
	}
	
	public void setActionMessage(String message) {
		this.actionMessage=message;
		
	}

	public void setGlobalConfigurationModel(Model globalInput) {
		globalConfigurationModel=globalInput;
		
	}
	
}
