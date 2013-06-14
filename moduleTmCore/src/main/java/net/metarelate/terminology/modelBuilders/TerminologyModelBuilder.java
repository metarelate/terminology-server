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

package net.metarelate.terminology.modelBuilders;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.NonConformantRDFException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.management.TerminologyManager;
import net.metarelate.terminology.reasoning.ReasonerProxy;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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

public class TerminologyModelBuilder {
	protected Initializer myInitializer=null;
	protected Model inputGraph=null;
	protected String actionMessage=CoreConfig.DEFAULT_FROM_RDF_DESCRIPTION;
	protected boolean updateMode=false;
	Hashtable<String, String> entityToVersion=null; //Maps entity to be imported to their "operative" version.
	
	public TerminologyModelBuilder (Initializer initializer) {
		myInitializer=initializer;
	}
	
	/**
	 * Generates a terminology model from a collection of RDF resources which describe the terminology resources
	 * and that provides instruction for the model builder (e.g.: reasoning type, pragma operations, ...)
	 * @param globalInput
	 * @throws NonConformantRDFException
	 * @throws ConfigurationException 
	 * @throws ImporterException 
	 * @throws ModelException 
	 * @throws UnknownURIException 
	 */
	public void generateModel(Model globalInput,boolean updateMode, String message) throws ConfigurationException, ImporterException, UnknownURIException, ModelException  {
		this.inputGraph=globalInput;
		this.updateMode=updateMode;
		if(message!=null) this.actionMessage=message;
		entityToVersion=new Hashtable<String,String>();
		//inputGraph=myInitializer.getConfigurationGraph();
		Loggers.importLogger.info("Starting import, update mode="+updateMode);
		Loggers.importLogger.debug("Number of known statements in input: "+inputGraph.size());
		ReasonerProxy.customReason(inputGraph);
		Loggers.importLogger.debug("Number of statements after reasoning: "+inputGraph.size());
		fillBackwardContainment(inputGraph);
		Loggers.importLogger.debug("Number of statements after containment completion: "+inputGraph.size());
		generateIndividuals();
		generateSets();
		//buildContainmentStructure(); TODO verify this is obsolete now 
		//fillEntities(); //TODO this should be done online...
		processPragma();
		
		
		myInitializer.myFactory.getBackgroundKnowledgeManager().getModel().add(getPropertyMetadata());
		myInitializer.myFactory.getLabelManager().registerLabels(getLabels());
		
	}
	
	
	
	
	private void fillBackwardContainment(Model model) {
		Model newStats=ModelFactory.createDefaultModel();
		StmtIterator cin=model.listStatements(null,MetaLanguage.definedInProperty,(Resource)null);
		while(cin.hasNext()) {
			Loggers.importLogger.trace("contained");
			Statement stat=cin.nextStatement();
			if(stat.getObject().isResource()) {
				newStats.add(stat.getObject().asResource(),MetaLanguage.definesProperty,stat.getSubject());
			}
		}
		StmtIterator cis=model.listStatements(null,MetaLanguage.definesProperty,(Resource)null);
		while(cis.hasNext()) {
			Loggers.importLogger.trace("contains");
			Statement stat=cis.nextStatement();
			if(stat.getObject().isResource()) {
				newStats.add(stat.getObject().asResource(),MetaLanguage.definedInProperty,stat.getSubject());
			}
		}
		model.add(newStats);
		//return model.add(newStats);
		
	}

	

	protected void generateSets() throws ConfigurationException, ImporterException  {
		Loggers.importLogger.trace("Generating Code Sets");
		ResIterator terminologySetIterator=inputGraph.listResourcesWithProperty(MetaLanguage.typeProperty,MetaLanguage.terminologySetType);		
		while(terminologySetIterator.hasNext()) {
			Resource currentEntitySet=terminologySetIterator.nextResource();
			generateEntity(currentEntitySet,true);
		}
		
		
	}
	
	protected void generateIndividuals() throws ConfigurationException, ImporterException  {
		Loggers.importLogger.trace("Generating Code Individual");
		ResIterator entityIndividualIter=inputGraph.listResourcesWithProperty(MetaLanguage.typeProperty,MetaLanguage.terminologyIndividualType);
		while(entityIndividualIter.hasNext()) {
			Resource currentEntityIndividual=entityIndividualIter.nextResource();
			generateEntity(currentEntityIndividual,false);
		}
	}	
	
	
	//protected abstract void buildContainmentStructure();

	/**
	 * Sets the description for the import action
	 * @param message
	 */
	public void setActionMessage(String message) {
		this.actionMessage=message;
		
	}

	/**
	 * TODO this methods directly adds its result in a class hashtable. Results and hash storage should be broken down, and this method should simply return an object, for better modularity.
	 * @param currentEntityCollection the resource corresponding to the current entity collection to be built.
	 * @throws ImporterException 
	 * @throws NonConformantRDFException 
	 */
	
	private  void generateEntity(Resource entityResource,boolean isSet) throws ConfigurationException, ImporterException  {
		if(isSet) Loggers.importLogger.debug("Processing set "+entityResource.getURI());
		else Loggers.importLogger.debug("Processing individual "+entityResource.getURI());
		TerminologyEntity myEntity;
		String version=null;
		boolean isVersioned=true;
		if(SimpleQueriesProcessor.hasOptionalLiteral(entityResource,MetaLanguage.hasVersionProperty,inputGraph) ) {
			version=SimpleQueriesProcessor.getOptionalLiteral(entityResource,MetaLanguage.hasVersionProperty,inputGraph).getValue().toString();
		}
		int versionNumber=0;
		if(version!=null) {
			Loggers.importLogger.trace("Found version: "+version);
			try {
				versionNumber=Integer.parseInt(version);
			} catch (NumberFormatException e) {
				throw new ConfigurationException("Unparsable version number for "+entityResource.getURI()+" ("+version+")");
			}
		}
		else Loggers.importLogger.trace("No version specified");
		//Entity is unversioned, we just act on it idempotently.
		if(versionNumber<0) {
			isVersioned=false;
			Loggers.importLogger.trace("Un-versioned");
		}
		else Loggers.importLogger.trace("Versioned");
		//First we check if the individual already existed, if it is new, we just create it.
		if(!(myInitializer.myFactory.terminologyEntityExist(entityResource.getURI()))) {
			Loggers.importLogger.debug("This individual was never declared before");
			if(isVersioned) {
				if(isSet) myEntity=myInitializer.myFactory.createNewVersionedTerminologySet(entityResource.getURI());
				else myEntity=myInitializer.myFactory.createNewVersionedTerminologyIndividual(entityResource.getURI());
				Loggers.importLogger.trace("New versioned");
			}
			else {
				if(isSet) myEntity=myInitializer.myFactory.createNewUnversionedTerminologySet(entityResource.getURI()); 
				else myEntity=myInitializer.myFactory.createNewUnversionedTerminologyIndividual(entityResource.getURI()); 
				Loggers.importLogger.trace("New un-versioned");
			}
			entityToVersion.put(myEntity.getURI(), myEntity.getLastVersion());
			fillEntity(myEntity,entityToVersion.get(myEntity.getURI()));
			fillMetadataForNewEntity(myEntity,entityToVersion.get(myEntity.getURI()));
			fillDefaultStateIfNone(myEntity,entityToVersion.get(myEntity.getURI()));
		}
		else { //TODO check here whether it changed or not
			Loggers.importLogger.debug("An entity with this URI was declared before (we don't check for types!)");
			if(isSet) myEntity=myInitializer.myFactory.getUncheckedTerminologySet(entityResource.getURI());
			else myEntity=myInitializer.myFactory.getUncheckedTerminologyIndividual(entityResource.getURI());
			//Did it change ?
			String lastVersion=myEntity.getLastVersion();
			Model oldStatements=myEntity.getStatements(lastVersion);
			Model newStatement=collectFilteredMetadataForEntity(myEntity);
			complementState(myEntity.getResource(), newStatement);
			Model newOnly=newStatement.difference(oldStatements);
			Model oldOnly=oldStatements.difference(newStatement);
			if(newOnly.size()==0 && oldOnly.size()==0) {
				Loggers.importLogger.debug("Nothing changed, doing nothing");
			}
			else {
				Loggers.importLogger.debug("Something changed from last time: ");
				StmtIterator oldOnlyIter=oldOnly.listStatements();
				while(oldOnlyIter.hasNext()) {
					Loggers.importLogger.trace("Missing: "+oldOnlyIter.nextStatement().toString());
				}
				StmtIterator newOnlyIter=newOnly.listStatements();
				while(newOnlyIter.hasNext()) {
					Loggers.importLogger.trace("Added: "+newOnlyIter.nextStatement().toString());
				}
				if(!isVersioned) {
					Loggers.importLogger.trace("Non versioned: overriding statements");
					//myIndividual=myInitializer.myFactory.getUncheckedTerminologyIndividual(currentIndividual.getURI());
					entityToVersion.put(myEntity.getURI(), myEntity.getLastVersion());
					fillEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillMetadataForReimportEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillDefaultStateIfNone(myEntity,entityToVersion.get(myEntity.getURI()));
				}
				else if(version!=null) {
					Loggers.importLogger.trace("Versioned: overriding statements for specified version: "+version);
					//myIndividual=myInitializer.myFactory.getUncheckedTerminologyIndividual(currentIndividual.getURI());
					myEntity.registerVersion(version);	//In case we didn't know this... shouldn't happen in correct usage
					entityToVersion.put(myEntity.getURI(), version);
					fillEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillMetadataForReimportEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillDefaultStateIfNone(myEntity,entityToVersion.get(myEntity.getURI()));
				}
				else if(updateMode==false) {
					//myIndividual=myInitializer.myFactory.getUncheckedTerminologyIndividual(currentIndividual.getURI());
					entityToVersion.put(myEntity.getURI(), myEntity.getLastVersion());
					Loggers.importLogger.trace("Versioned, no version specified. Overriding statements for last version: "+entityToVersion.get(myEntity.getURI()));
					fillEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillMetadataForReimportEntity(myEntity,entityToVersion.get(myEntity.getURI()));
					fillDefaultStateIfNone(myEntity,entityToVersion.get(myEntity.getURI()));
				}
				else if(updateMode) {
					try {
						myInitializer.myTerminologyManager.amendEntityInformation(myEntity.getURI(), newStatement, myInitializer.getDefaultUserURI(), actionMessage, TerminologyManager.MODE_REPLACE);
					} catch (AuthException e) {
						Loggers.importLogger.error("Please check you have permission to import data. All auth on all is a safe assumption for a fresh new system");
						e.printStackTrace();
						return;
					} catch (InvalidProcessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (RegistryAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					} catch (ModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
					
			}
		}
		
		
		Literal myNs=SimpleQueriesProcessor.getOptionalLiteral(entityResource, MetaLanguage.nameSpaceProperty, inputGraph);
		if(myNs!=null) myEntity.setLocalNamespace(myNs.getValue().toString());
			
	}
	
	private void fillEntity(TerminologyEntity e, String version) {
		e.getStatements(version).removeAll().add(collectFilteredMetadataForEntity(e));
		
	}

	private Model collectFilteredMetadataForEntity(TerminologyEntity e) {
		Model result=ModelFactory.createDefaultModel();
		StmtIterator iter=inputGraph.listStatements(e.getResource(),null,(RDFNode)null);
		while(iter.hasNext()) {
			Statement stat=iter.next();
			boolean okstat=true;
			if(stat.getPredicate().equals(MetaLanguage.nameSpaceProperty)) okstat=false;
			if(stat.getPredicate().equals(MetaLanguage.hasVersionProperty)) okstat=false;
			if(stat.getObject().isResource()) {
				if(stat.getObject().asResource().equals(MetaLanguage.terminologySetType) || stat.getObject().asResource().equals(MetaLanguage.terminologyIndividualType)) okstat=false;
			}
			if(okstat) result.add(stat);
		}
		return result;
	}

	private void fillDefaultStateIfNone(TerminologyEntity entity, String version) {
		String statusURI= SimpleQueriesProcessor.getOptionalLiteralValueAsString(entity.getResource(), MetaLanguage.hasStatusProperty, inputGraph);
		if(statusURI==null) entity.setStateURI(CoreConfig.DEFAULT_IMPORT_STATUS,version); //TODO move default somewhere better
		
	}
	
	private void complementState(Resource subject, Model model) {
		String statusURI= SimpleQueriesProcessor.getOptionalLiteralValueAsString(subject, MetaLanguage.hasStatusProperty, model);
		if(statusURI==null) model.add(ResourceFactory.createStatement(subject, MetaLanguage.hasStatusProperty, ResourceFactory.createResource(CoreConfig.DEFAULT_IMPORT_STATUS))); //TODO move default somewhere better
		
	}

	//After refactoring
	private void fillMetadataForNewEntity (TerminologyEntity e,String version) {
		e.setActionURI(CoreConfig.DEAFULT_FROM_RDF_IMPORT_ACTION,version);
		e.setActionAuthorURI(myInitializer.getDefaultUserURI(),version);
		e.setActionDescription(actionMessage,version);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		e.setActionDate(dateFormat.format(date),version);
		e.setOwnerURI(myInitializer.getDefaultUserURI()); // TODO this should be changed if we have to move to a more complex login system
	}
	
	private void fillMetadataForReimportEntity (TerminologyEntity e,String version) {
		e.setActionURI(CoreConfig.DEAFULT_FROM_RDF_REIMPORT_ACTION,version);
		e.setActionAuthorURI(myInitializer.getDefaultUserURI(),version);
		e.setActionDescription(actionMessage,version);
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		e.setActionDate(dateFormat.format(date),version);
	}
	
	/**
	 * Returns all labels in the input to the import process
	 * @return
	 */
	public Model getLabels() {
		StmtIterator labelStats=inputGraph.listStatements(null,MetaLanguage.labelProperty,(RDFNode)null);
		Model labelModel=ModelFactory.createDefaultModel();
		labelModel.add(labelStats);
		Loggers.importLogger.debug("Labels found: "+labelModel.size());
		return labelModel;
	}
	
	/**
	 * Returns property metadata (focus) found in the input to the import process
	 * @return
	 */
	public Model getPropertyMetadata() {
		StmtIterator stIter=inputGraph.listStatements((Resource)null,MetaLanguage.propertyHasFocus,(Resource)null);
		Model propMetaModel=ModelFactory.createDefaultModel();
		propMetaModel.add(stIter);
		Loggers.importLogger.debug("PropertyMetadata found: "+propMetaModel.size());
		return propMetaModel;
		
		
	}
	
	private void processPragma() throws UnknownURIException, ImporterException, ModelException {
		// We check pragma for all collections
		Loggers.importLogger.info("Checking pragmas");
		StmtIterator pragmaStatements=inputGraph.listStatements(null, MetaLanguage.pragmaProperty, (Resource)null);
		while (pragmaStatements.hasNext()) {
			Statement pragmaStatement=pragmaStatements.next();
			Resource pragmaNode=pragmaStatement.getObject().asResource();
			//System.out.println(pragmaStatement.toString());
			StmtIterator pragmaTypes=inputGraph.listStatements(pragmaNode,MetaLanguage.typeProperty,(Resource)null);
			while(pragmaTypes.hasNext()) {
				// TODO add try/catch malformed model
				Resource pragmaType=pragmaTypes.next().getObject().asResource();
				Loggers.importLogger.debug("Found pragma of type: "+pragmaType.getURI());
				if(pragmaType.equals(TerminologyModelBuilderConfig.pragmaExpandDashAndSuppress)) {
					//Here we collect this pragma parameters
					int maxLimit=-1;
					int pad=-1;
					boolean hardLimitCut=false;
					boolean toSuppress=false;
					if(inputGraph.contains(pragmaNode, TerminologyModelBuilderConfig.pragmaPropProperty, TerminologyModelBuilderConfig.pragmaSuppress)) toSuppress=true;
					if(inputGraph.contains(pragmaNode, TerminologyModelBuilderConfig.pragmaPropProperty, TerminologyModelBuilderConfig.pragmaHardLimitCut)) hardLimitCut=true;
					Literal maxValue=SimpleQueriesProcessor.getOptionalLiteral(pragmaNode, TerminologyModelBuilderConfig.pragmaHardLimit, inputGraph);
					if(maxValue!=null) {
						try {
							maxLimit=maxValue.getInt();
						} catch (Exception e) {}
					}
					Literal padValue=SimpleQueriesProcessor.getOptionalLiteral(pragmaNode, TerminologyModelBuilderConfig.pragmaPad, inputGraph);
					if(padValue!=null) {
						try {
							pad=padValue.getInt();
						} catch (Exception e) {}
					}
					//looking for override
					//Resource overridePropRes=SimpleQueriesProcessor.getOptionalResourceObject(pragmaStatement.getSubject(), MetaLanguage.pragmaOverrideProp, myModel);
					//System.out.println(">>>>>>>"+overridePropRes);
					ArrayList<Property> overrideProps=new ArrayList<Property>();
					//Property overrideProp=null;
					NodeIterator ovvIter=inputGraph.listObjectsOfProperty(pragmaNode, TerminologyModelBuilderConfig.pragmaOverrideProp);
					while(ovvIter.hasNext()) {
						//System.out.println("Type: .-.");
						RDFNode node=ovvIter.next();
						if(node.isResource()) overrideProps.add(ResourceFactory.createProperty(node.asResource().getURI()));
					}
					
									
					String collectionURI=pragmaStatement.getSubject().getURI();
					if(myInitializer.myFactory.terminologySetExist(collectionURI)) {
						PragmaProcessor myProc=new PragmaComputeExpandDashAndSuppress(myInitializer.myFactory,myInitializer.myFactory.getUncheckedTerminologySet(collectionURI),toSuppress,overrideProps,maxLimit,hardLimitCut,pad);
						myProc.run();
					}
				}
				else if(pragmaType.equals(TerminologyModelBuilderConfig.pragmaExpandTree)) {
	
					Resource treeCollectionResource=SimpleQueriesProcessor.getOptionalResourceObject(pragmaNode, TerminologyModelBuilderConfig.pragmaTreeCollection, inputGraph);
					Resource schemeResource=SimpleQueriesProcessor.getOptionalResourceObject(pragmaNode, TerminologyModelBuilderConfig.pragmaSchemaProperty, inputGraph);
					TerminologySet rootSet=null;
					if(treeCollectionResource!=null) rootSet=myInitializer.myFactory.getCheckedTerminologySet(treeCollectionResource.getURI());
					//if(leafsCollectionResource!=null) leafsSet=allCollections.get(leafsCollectionResource.getURI());
					if(rootSet!=null) {
						PragmaProcessor myProc=new ComputeTreePragmaProcessor(myInitializer.myFactory,inputGraph,rootSet,schemeResource);
						myProc.run();
						
					}
					
				}
				else {
					Loggers.importLogger.warn("Unknown type: "+pragmaType.getURI());
				}
			}
		}
		
	}

	/**
	 * sets the input to the import process
	 * @param globalInput
	 */
	public void registerInput(Model globalInput) {
		this.inputGraph=globalInput;
		
	}

	
}
