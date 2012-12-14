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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.NonConformantRDFException;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Builds a terminology model from an RDF description (meta information plus content)
 * @author andreasplendiani
 *
 */

public class TerminologyModelBuilderFromRDF extends TerminologyModelBuilder{
	
	
	
	public TerminologyModelBuilderFromRDF(TerminologyFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Model getLabels() {
		StmtIterator labelStats=globalConfigurationModel.listStatements(null,MetaLanguage.labelProperty,(RDFNode)null);
		Model labelModel=ModelFactory.createDefaultModel();
		labelModel.add(labelStats);
		System.out.println(">>>GetLabels found: "+labelModel.size());
		return labelModel;
	}
	
	@Override
	protected Model getPropertyMetadata() {
		StmtIterator stIter=globalConfigurationModel.listStatements((Resource)null,MetaLanguage.propertyHasFocus,(Resource)null);
		Model propMetaModel=ModelFactory.createDefaultModel();
		propMetaModel.add(stIter);
		System.out.println(">>>getPropertyMetadata found: "+propMetaModel.size());
		return propMetaModel;
		
		
	}
	
	public Map<String,String> getPrefixes() {
		return globalConfigurationModel.getNsPrefixMap();
	}



	
	/**
	 * TODO this methods directly adds its result in a class hashtable. Results and hash storage should be broken down, and this method should simply return an object, for better modularity.
	 * @param currentEntityCollection the resource corresponding to the current entity collection to be built.
	 * @throws NonConformantRDFException 
	 */
	
	@Override
	protected  void generateSet(Resource currentEntityCollection) throws NonConformantRDFException   {
		TerminologySet myCollection;
		SSLogger.log("Going to generate model for Set Resource :"+currentEntityCollection.getURI(),SSLogger.DEBUG);
		String version="";
		if(SimpleQueriesProcessor.hasOptionalLiteral(currentEntityCollection,MetaLanguage.hasVersionProperty,globalConfigurationModel) ) {
			version=SimpleQueriesProcessor.getSingleMandatoryLiteral(currentEntityCollection,MetaLanguage.hasVersionProperty,globalConfigurationModel).getValue().toString();
			SSLogger.log("version: "+version,SSLogger.DEBUG);
			myCollection=myFactory.getOrCreateTerminologySet(currentEntityCollection.getURI(),version);		
		}
		else {
			SSLogger.log("non-versioned",SSLogger.DEBUG);
			myCollection=myFactory.getOrCreateTerminologySet(currentEntityCollection.getURI());
		}
		//Common bits
		
		Literal myNs=SimpleQueriesProcessor.getOptionalLiteral(currentEntityCollection, MetaLanguage.nameSpaceProperty, globalConfigurationModel);
		if(myNs!=null) myCollection.setLocalNamespace(myNs.getValue().toString());

		
		
		Resource statusURIRes= SimpleQueriesProcessor.getOptionalResourceObject(currentEntityCollection, MetaLanguage.hasStatusProperty, globalConfigurationModel);
		if(statusURIRes!=null) myCollection.setStateURI(statusURIRes.getURI(),myCollection.getDefaultVersion());
		else myCollection.setStateURI(CoreConfig.DEFAULT_IMPORT_STATUS,myCollection.getDefaultVersion());
		Resource ownerURIRes= SimpleQueriesProcessor.getOptionalResourceObject(currentEntityCollection, MetaLanguage.hasManagerProperty, globalConfigurationModel);
		if(ownerURIRes!=null) myCollection.setOwnerURI(ownerURIRes.getURI());
		else if (globalOwnerURI!=null)  myCollection.setOwnerURI(globalOwnerURI);
		//SSLogger.log("Owner check: "+myCollection.getOwnerURI(),SSLogger.DEBUG);//TODO debug
		//SSLogger.log("Collections has #versions: "+myCollection.getVersions().length,SSLogger.DEBUG);//TODO debug
		
		if(autoDefaults) {
			myCollection.setActionURI(CoreConfig.DEAFULT_FROM_RDF_IMPORT_ACTION,myCollection.getDefaultVersion());
			myCollection.setActionAuthorURI(globalOwnerURI,myCollection.getDefaultVersion());
			myCollection.setActionDescription(CoreConfig.DEFAULT_FROM_RDF_DESCRIPTION,myCollection.getDefaultVersion());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			myCollection.setActionDate(dateFormat.format(date),myCollection.getDefaultVersion());
		}
		
		//Additional statements
		Model myStats=myCollection.getStatements(myCollection.getDefaultVersion());	
		myStats.add(myStats.createStatement(myCollection.getResource(), MetaLanguage.typeProperty, MetaLanguage.isoRegistryType));
	}
	
	/**
	 * TODO this methods directly adds its result in a class hashtable. Results and hash storage should be broken down, and this method should simply return an object, for better modularity.
	 * @param currentEntityCollection the resource corresponding to the current entity collection to be built.
	 * @throws NonConformantRDFException 
	 */
	@Override
	protected  void generateIndividual(Resource currentIndividual) throws NonConformantRDFException  {
		TerminologyIndividual myIndividual;
		SSLogger.log("generate individual for "+currentIndividual.getURI(),SSLogger.DEBUG);
		String version="";
		if(SimpleQueriesProcessor.hasOptionalLiteral(currentIndividual,MetaLanguage.hasVersionProperty,globalConfigurationModel) ) {
			SSLogger.log("versioned");
			version=SimpleQueriesProcessor.getSingleMandatoryLiteral(currentIndividual,MetaLanguage.hasVersionProperty,globalConfigurationModel).getValue().toString();
			myIndividual=myFactory.getOrCreateTerminologyIndividual(currentIndividual.getURI(),version);
		}
		else {
			SSLogger.log("un-versioned",SSLogger.DEBUG);
			myIndividual=myFactory.getOrCreateTerminologyIndividual(currentIndividual.getURI());

		}
		
		Literal myNs=SimpleQueriesProcessor.getOptionalLiteral(currentIndividual, MetaLanguage.nameSpaceProperty, globalConfigurationModel);
		if(myNs!=null) myIndividual.setLocalNamespace(myNs.getValue().toString());
		
		String statusURI= SimpleQueriesProcessor.getOptionalLiteralValueAsString(currentIndividual, MetaLanguage.hasStatusProperty, globalConfigurationModel);
		if(statusURI!=null) myIndividual.setStateURI(statusURI,myIndividual.getDefaultVersion());
		else myIndividual.setStateURI(CoreConfig.DEFAULT_IMPORT_STATUS,myIndividual.getDefaultVersion());
		
		String ownerURI= SimpleQueriesProcessor.getOptionalLiteralValueAsString(currentIndividual, MetaLanguage.hasManagerProperty, globalConfigurationModel);
		if(ownerURI!=null) myIndividual.setOwnerURI(ownerURI);
		else if(globalOwnerURI!=null)  myIndividual.setOwnerURI(globalOwnerURI);
		
		if(autoDefaults) {
			myIndividual.setActionURI(CoreConfig.DEAFULT_FROM_RDF_IMPORT_ACTION,myIndividual.getDefaultVersion());
			myIndividual.setActionAuthorURI(globalOwnerURI,myIndividual.getDefaultVersion());
			myIndividual.setActionDescription(CoreConfig.DEFAULT_FROM_RDF_DESCRIPTION,myIndividual.getDefaultVersion());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			myIndividual.setActionDate(dateFormat.format(date),myIndividual.getDefaultVersion());
		}
		
		//Additional statements
		Model myStats=myIndividual.getStatements(myIndividual.getDefaultVersion());	
		myStats.add(myStats.createStatement(myIndividual.getResource(), MetaLanguage.typeProperty, MetaLanguage.isoRegistryItemType));
			
	}
	
	@Override
	protected void fillEntity(TerminologyEntity entity, String version) {
		Resource myRes=ResourceFactory.createResource(entity.getURI());
		Model resModel=MetaLanguage.filterForModel(collectStatementsForResource(myRes));
		entity.addStatements(resModel,version);
		SSLogger.log("Added "+resModel.size()+" statements to "+entity.getURI());
		// TODO look for standards as well.
	}
	
	/**
	 * Here we control what is to be associated to each code. This is a critical bit.
	 * For the time being, we collect statements that have the "code" as a subject (more naturally represented in tables).
	 * These will include the label of the code.
	 * Should we include other labels ?
	 * We can think that a label for a code/concept is part of the "payload" of a code/concept description.
	 * Not necessarily so for all labels used to characterize a code/concept.
	 * We delegate all these to a different "label list"
	 * Should we include other statements ?
	 * Unlikely for tables. But this would be the case if we import ontologies (which we could do even now).
	 * TODO (the above)
	 * @param res
	 * @param model
	 * @return
	 */
	private  Model collectStatementsForResource(Resource res) {
		StmtIterator stIter= globalConfigurationModel.listStatements(new SimpleSelector(res,null,(Object)null));
		Model tempModel=ModelFactory.createDefaultModel();
		tempModel.add(stIter);
		
		
		//We now collect labels as well (redundant, isn't it?)
		//StmtIterator stIter2=model.listStatements(res,MetaLanguage.labelProperty,(Literal)null);
		//tempModel.add(stIter2);
		
		return tempModel;
		// Follow properties?
	}

	
	
	@Override
	protected void buildContainmentStructure() {
		// Processing collections first.
		SSLogger.log("Looking for containment among collections ",SSLogger.DEBUG);
		Iterator<TerminologySet> collections=myFactory.getAllSets().iterator();
		while(collections.hasNext()) {
			TerminologySet myCollection=collections.next();
			//SSLogger.log("Processing "+myCollection.getURI(),SSLogger.DEBUG);
			Resource myModelInFile=ResourceFactory.createResource(myCollection.getURI());
			NodeIterator containers=globalConfigurationModel.listObjectsOfProperty(myModelInFile, MetaLanguage.containedInProperty);
			while(containers.hasNext()) {
				//System.out.println("In");
				RDFNode potentialContainer=containers.nextNode();
				//Note that we require this to be a URI Resource!
				if(potentialContainer.isURIResource()) {
					//System.out.println("URI");
					if(myFactory.terminologySetExist(((Resource)potentialContainer).getURI())) {
						myFactory.getOrCreateTerminologySet(((Resource)potentialContainer).getURI()).registerContainedCollection(myCollection);
						//wipeList.add(ResourceFactory.createStatement(ResourceFactory.createResource(myCollection.getURI()),MetaLanguage.containedInProperty,potentialContainer));
						SSLogger.log("registered "+myCollection.getURI()+" in "+((Resource)potentialContainer).getURI(),SSLogger.DEBUG);
					}
				}
			}
		}
		SSLogger.log("Looking for individuals containment ",SSLogger.DEBUG);
		Iterator<TerminologyIndividual> individuals= myFactory.getAllIndividuals().iterator();
		while(individuals.hasNext()) {
			TerminologyIndividual myIndividual=individuals.next();
			//SSLogger.log("Processing "+myIndividual.getURI(),SSLogger.DEBUG);
			Resource myModelInFile=ResourceFactory.createResource(myIndividual.getURI());
			NodeIterator containers=globalConfigurationModel.listObjectsOfProperty(myModelInFile, MetaLanguage.containedInProperty);
			while(containers.hasNext()) {
				//System.out.println("In");
				RDFNode potentialContainer=containers.nextNode();
				//Note that we require this to be a URI Resource!
				if(potentialContainer.isURIResource()) {
					//System.out.println("URI");
					if(myFactory.terminologySetExist(((Resource)potentialContainer).getURI())) {
						//System.out.println("Type OK");
						myFactory.getOrCreateTerminologySet(((Resource)potentialContainer).getURI()).registerContainedIndividual(myIndividual);
						//wipeList.add(ResourceFactory.createStatement(ResourceFactory.createResource(myIndividual.getURI()),MetaLanguage.containedInProperty,potentialContainer));
						SSLogger.log("registered ind. "+myIndividual.getURI()+" in "+((Resource)potentialContainer).getURI(),SSLogger.DEBUG);
					}
				}
			}
		}
	myFactory.synchRootCollections();
		
	}

	@Override
	protected void processPragma() {
		// We check pragma for all collections
		SSLogger.log("*************************************",SSLogger.DEBUG);
		SSLogger.log("Checking pragmas",SSLogger.DEBUG);
		SSLogger.log("*************************************",SSLogger.DEBUG);
		StmtIterator pragmaStatements=globalConfigurationModel.listStatements(null, MetaLanguage.pragmaProperty, (Resource)null);
		while (pragmaStatements.hasNext()) {
			Statement pragmaStatement=pragmaStatements.next();
			Resource pragmaNode=pragmaStatement.getObject().asResource();
			//System.out.println(pragmaStatement.toString());
			StmtIterator pragmaTypes=globalConfigurationModel.listStatements(pragmaNode,MetaLanguage.typeProperty,(Resource)null);
			while(pragmaTypes.hasNext()) {
				// TODO add try/catch malformed model
				Resource pragmaType=pragmaTypes.next().getObject().asResource();
				SSLogger.log("Found pragma of type: "+pragmaType.getURI(),SSLogger.DEBUG);
				if(pragmaType.equals(MetaLanguage.pragmaExpandDashAndSuppress)) {
					//Here we collect this pragma parameters
					int maxLimit=-1;
					int pad=-1;
					boolean hardLimitCut=false;
					boolean toSuppress=false;
					if(globalConfigurationModel.contains(pragmaNode, MetaLanguage.pragmaPropProperty, MetaLanguage.pragmaSuppress)) toSuppress=true;
					if(globalConfigurationModel.contains(pragmaNode, MetaLanguage.pragmaPropProperty, MetaLanguage.pragmaHardLimitCut)) hardLimitCut=true;
					Literal maxValue=SimpleQueriesProcessor.getOptionalLiteral(pragmaNode, MetaLanguage.pragmaHardLimit, globalConfigurationModel);
					if(maxValue!=null) {
						try {
							maxLimit=maxValue.getInt();
						} catch (Exception e) {}
					}
					Literal padValue=SimpleQueriesProcessor.getOptionalLiteral(pragmaNode, MetaLanguage.pragmaPad, globalConfigurationModel);
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
					NodeIterator ovvIter=globalConfigurationModel.listObjectsOfProperty(pragmaNode, MetaLanguage.pragmaOverrideProp);
					while(ovvIter.hasNext()) {
						//System.out.println("Type: .-.");
						RDFNode node=ovvIter.next();
						if(node.isResource()) overrideProps.add(ResourceFactory.createProperty(node.asResource().getURI()));
					}
					
									
					String collectionURI=pragmaStatement.getSubject().getURI();
					if(myFactory.terminologySetExist(collectionURI)) {
						PragmaProcessor myProc=new PragmaComputeExpandDashAndSuppress(myFactory,myFactory.getOrCreateTerminologySet(collectionURI),toSuppress,overrideProps,maxLimit,hardLimitCut,pad);
						myProc.run();
					}
				}
				else if(pragmaType.equals(MetaLanguage.pragmaExpandTree)) {
	
					Resource treeCollectionResource=SimpleQueriesProcessor.getOptionalResourceObject(pragmaNode, MetaLanguage.pragmaTreeCollection, globalConfigurationModel);
					Resource schemeResource=SimpleQueriesProcessor.getOptionalResourceObject(pragmaNode, MetaLanguage.pragmaSchemaProperty, globalConfigurationModel);
					TerminologySet rootSet=null;
					if(treeCollectionResource!=null) rootSet=myFactory.getOrCreateTerminologySet(treeCollectionResource.getURI());
					//if(leafsCollectionResource!=null) leafsSet=allCollections.get(leafsCollectionResource.getURI());
					if(rootSet!=null) {
						PragmaProcessor myProc=new ComputeTreePragmaProcessor(myFactory,globalConfigurationModel,rootSet,schemeResource);
						myProc.run();
						
					}
					
				}
				else {
					System.out.println("Unknown type: "+pragmaType.getURI());
				}
			}
		}
		
	}


	
}
