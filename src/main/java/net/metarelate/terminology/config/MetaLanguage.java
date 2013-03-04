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

package net.metarelate.terminology.config;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.lang.reflect.*;

public class MetaLanguage {
	private static Set<Resource> indexRModel=null;
	private static Set<Property> indexPModel=null;
	
	private static Set<Resource> indexRWeb=null;
	private static Set<Property> indexPWeb=null;
	
	private static Set<Resource> indexRData=null;
	private static Set<Property> indexPData=null;
	
	
	/**
	 * Configuration and instance management
	 */
	public static final String authConfigURI="http://metarelate.net/config/hasAuthConfigType";
	public static final Property authConfigProperty=ResourceFactory.createProperty(authConfigURI);
	
	/**
	 * General usage properties
	 * these properties are used in the mapping and appear in the data, but not in the web representation of data
	 */
	public static final Property labelProperty=ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label");
	public static final Property typeProperty=ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	public static final Property commentProperty = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#comment");
	public static final Property notationProperty=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#notation");
	public static final Property skosTopConceptProperty=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#hasTopConcept");
	public static final Property skosNarrowerProperty=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#narrower");
	public static final Property skosBroaderProperty=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#broader");

	public static final Resource skosConceptType=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#Concept");
	public static final Resource skosCollectionType=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#Collection");
	public static final Resource skosSchemeType=ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#Scheme");

	public static final Resource isoRegistryType =ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#Register");
	public static final Resource isoRegistryItemType =ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#RegisterItem");
	
	//states!
	//public static final Resource statusWMOValidationResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Validation");
	//public static final Resource statusWMOValidResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Operational");
	//public static final Resource statusWMOSupersededResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Superseded");
	//public static final Resource statusWMORetiredResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Retired");
	
	//public static final Resource statusWMOValidationResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Validation");
	//public static final Resource statusWMOOperationalResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/wmo#Operational");

	//public static final Resource statusISONotValidResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#NotValid");
	//public static final Resource statusISOValidResource=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#Valid");

	//public static final Resource statusOutOfRegistry = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#outOfRegistryStatus");
	//public static final Resource statusObsolete = ResourceFactory.createResource("http://meoffice.gov.uk/terminology/extra/obsoleteStatus");
	//public static final Resource statusIllegalResource = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/model#outOfRegistryStatus");
	//public static final Resource statusNullResource = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/model#outOfRegistryStatus");

	
	//Actions!
	//public static final Resource terminologyUpdateAction=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#updateAction");
	//public static final Resource terminologyAddItemAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#addItemAction");
	//public static final Resource terminologyDelItemAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#delItemAction");
	//public static final Resource terminologyDelFromRegAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#obseleteTerm");
	//public static final Resource terminologySupersedAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#supersedTerm");

	//public static final Resource terminologyValidateAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#validateTerm");
	//public static final Resource terminologyInvalidateAction = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#invalidateTerm");

	
	
	public static final Resource tagAction=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/def/met#tag");
	
	
	
	public static final Property hasTag=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#hasTag");
	
	
	/**
	 * Web serialization support (needed to serialize via web)
	 * these properties don't enter the data model
	 * TODO namespace property is currently stored in the data model (it doesn't appear in view, but it should regarded as homogenuos to the other metadata)
	 */
	// The root of the website
	public static final Property sitePrefixProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#sitePrefix");
	// The root of the disk location
	public static final Property diskPrefixProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#diskPrefix");
	// The address of the css file
	public static final Property cssAddressProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#cssAddress");
	// For every entity a namespace to be used to compose a URL 
	public static final Property nameSpaceProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#nameSpace");	
	// Override the base path for a specific collection (or entity?) // TODO decide
	public static final Property overrideBasePathProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#overridesDiskPrefix");	
	// Override the base namespace for a specific collection (or entity?) // TODO decide

	public static final Property baseURLProperty = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#baseURL");	
	
	public static final Property overrideBaseSiteProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#overrideSitePrefix");	
	
	public static final Property localIdProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#localID");	

	
	/**
	 * TDB serialization support
	 * these properties don't enter the data model
	 */
	// TDB location
	public static final Property tdbPrefixProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#tdbPrefix");
	
	
	
	/**
	 * These structural relations are only used in the RDF representation used to build a model.
	 * There are filtered in the model statements.
	 */
	public static final Property containedInProperty =ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#containedIn");
	public static final Property containsProperty   =ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#contains");
	
	
	
	/**
	 * Relations among entities and collections used on the web.
	 * They are not present in the model by design, and they are filtered in the html (but not data)
	 * TODO we may have different properties for registers, collections and the like.
	 */
	// Links a code to a set of standards that this code can have information for. On tdb-named, this is a relation among named graphs. In RDF, this is a relation among URIs.
	public static final Property hasStandardProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#standardSpecific"); // TODO standards may be osboleted soon
	// Collection contains collection
	public static final Property hasSubRegisterProperty =ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#subRegister");
	// Collection contains terms
	public static final Property hasRegisterItemProperty =ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#registerItem");
	// Collection to versions
	
	public static final Property definedInRegister = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#definedInRegister");

	
	
	/**
	 * Model support
	 * TODO models are largely to be reworked. In their current incarnation, they have to be considered as "style-sheets" that specify the  
	 */
	//public static final Resource entityCollectionType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#entityCollection");
	//public static final Resource conceptCollectionType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#conceptCollection");
	//public static final Resource registryType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#codeCollection");

	//public static final Resource entityIndividualType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#entityIndividual");
	//public static final Resource conceptIndividualType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#conceptIndividual");
	//public static final Resource regsiterItemType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#codeIndividual");
	
	public static final Resource standardType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#standard");
	
	public static final Resource terminologySetType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#TerminologySet");
	public static final Resource terminologyIndividualType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#TerminologyIndividual");
	
	
	//New!!!
	
	
	/**
	 * Registry support
	 */
	public static final Property hasManagerProperty =ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#manager");
	public static final Property hasStatusProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/wmo#status");
	
	public static final Property superseeds=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#superseeds");
	public static final Property superseededBy=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/iso19135#superseededBy");

	
	//public static final Resource validStatus=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#validStatus");
	//public static final Resource toBeValidatedStatus=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#toBeValidatedStatus");
	
	
	
	/**
	 * Versioning support
	 * TODO these are not in the model
	 * an equivalent set of properties should be used to publish metadata in RDF.
	 */
	public static final Property hasVersionProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasVersion");
	public static final Property hasPreviousVersionProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasPreviousVersion");
	public static final Property versionActionDateProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasActionDate");
	public static final Property versionActionProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasAction");
	public static final Property versionActorProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasActor");
	public static final Property versionActionDescription=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/def/met#hasActionDescription");
	
	public static final Property hasPreviousVersionURIProperty=ResourceFactory.createProperty("http://purl.org/dc/terms/replaces");
	public static final Property hasNewVersionURIProperty=ResourceFactory.createProperty("http://purl.org/dc/terms/isReplacedBy");
	public static final Property hasVersionURIProperty=ResourceFactory.createProperty("http://purl.org/dc/terms/hasVersion");

	/**
	 * TDB Support
	 * (not in model)
	 */
	public static final Resource rootCollectionType=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/model#rootCollection");
	public static final Property hasNonVersionModel = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#hasNonVersionModel");
	public static final Property containsCollection = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#containsCollection");
	public static final Property containsIndividual = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#containsIndividual");
	public static final Property hasVersionModel =  ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#hasVersionModel");
	
	
	/**
	 * Reasoning support
	 * (not in model)
	 */
	public static final Property symmetricProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#symmetric");
	public static final Property generatesProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#generates");
	
	
	public static final Property propertyHasFocus = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/model#propertyFocus");
	public static final Resource propertyCodeFocus = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/model#codeRelatedProperty");
	public static final Resource propertyConceptFocus = ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/model#conceptRelatedProperty");

	
	
	/**
	 * Pragma support
	 * (not in model)
	 */
	public static final Property pragmaProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragma");
	public static final Resource pragmaExpandDashAndSuppress=ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#expandDashAndSuppress");
	public static final Resource pragmaExpandTree =ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#ExpandTreeProcedure");
	public static final Resource pragmaSuppress =ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragmaSuppressSource");
	public static final Resource pragmaHardLimitCut =ResourceFactory.createResource("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#HardLimitCut");

	public static final Property pragmaTreeCollection = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragmaTreeCollection");
	public static final Property pragmaSchemaProperty = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragmaSchema");

	public static final Property pragmaPropProperty = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragmaProp");
	public static final Property pragmaOverrideProp = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#overrideProp");
	public static final Property pragmaHardLimit = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#hardLimit");
	public static final Property pragmaPad = ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#pragmaPad");
	

	
	
	public static Model filterForWeb(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(indexPWeb==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!indexPWeb.contains(stat.getPredicate()) && !indexRWeb.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	public static Model filterForData(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(indexPData==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!indexPData.contains(stat.getPredicate()) && !indexRData.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	public static Model filterForModel(Model ModelIn) {
		Model modelOut=ModelFactory.createDefaultModel();
		if(indexPModel==null) buildFilters();
		StmtIterator stats=ModelIn.listStatements();
		while(stats.hasNext()) {
			Statement stat=stats.next();
			if(!indexPModel.contains(stat.getPredicate()) && !indexRModel.contains(stat.getObject())) {
				modelOut.add(stat);
			}
		}
		return modelOut;
		
	}
	
	private static void buildFilters() {
		indexRWeb=new HashSet<Resource>();
		indexPWeb=new HashSet<Property>();
		//Filters for web
		
		//indexPWeb.add(sitePrefixProperty);
		//indexPWeb.add(diskPrefixProperty);
		indexPWeb.add(nameSpaceProperty);			// TODO should not be in model either!
		//indexPWeb.add(cssAddressProperty);
		//indexPWeb.add(tdbPrefixProperty);
		indexPWeb.add(hasStandardProperty);
		indexPWeb.add(hasManagerProperty);
		indexPWeb.add(hasSubRegisterProperty);
		indexPWeb.add(hasRegisterItemProperty);
		
		indexPWeb.add(hasStatusProperty);
		indexPWeb.add(hasVersionProperty);
		indexPWeb.add(hasPreviousVersionProperty);
		indexPWeb.add(versionActionDateProperty);
		indexPWeb.add(versionActionProperty);
		indexPWeb.add(versionActorProperty);
		indexPWeb.add(versionActionDescription);
		//indexPWeb.add(containedInProperty);
		//indexPWeb.add(containsProperty);
		indexPWeb.add(hasNonVersionModel);
		indexPWeb.add(definedInRegister);
		indexPWeb.add(notationProperty);			// std. prop
		indexPWeb.add(labelProperty);				// std. prop
		indexPWeb.add(typeProperty);				// std. prop
		indexPWeb.add(commentProperty);				// std. prop
		// Labels
		// 
		
		//indexR.add(validStatus);
		//indexR.add(toBeValidatedStatus);
		indexRWeb.add(terminologySetType);
		//indexRWeb.add(conceptCollectionType);
		//indexRWeb.add(registryType);
		//indexRWeb.add(entityIndividualType);
		//indexRWeb.add(conceptIndividualType);
		//indexRWeb.add(regsiterItemType);
		
		
		indexRWeb.add(standardType);
		
		// Filters for Data
		indexRData=new HashSet<Resource>();
		indexPData=new HashSet<Property>();
		
		//indexPData.add(sitePrefixProperty);
		//indexPData.add(diskPrefixProperty);
		indexPData.add(nameSpaceProperty);	// TODO should not be in model either!
		//indexPData.add(cssAddressProperty);
		//indexPData.add(tdbPrefixProperty);
		//indexPData.add(hasStandardProperty); TODO standards are to be reworked, but in case they should appear in some form in models
		indexPData.add(hasManagerProperty);
		indexPData.add(hasManagerProperty);
		indexPData.add(hasStatusProperty);
		indexPData.add(hasVersionProperty);
		indexPData.add(hasPreviousVersionProperty);
		indexPData.add(versionActionDateProperty);
		indexPData.add(versionActionProperty);
		indexPData.add(versionActorProperty);
		indexPData.add(versionActionDescription);
		//indexPData.add(containedInProperty);
		//indexPData.add(containsProperty);
		indexPData.add(hasNonVersionModel);
		
		//indexR.add(validStatus);
		//indexR.add(toBeValidatedStatus);
		indexRData.add(terminologySetType);
		//indexRData.add(conceptCollectionType);
		//indexRData.add(registryType);
		//indexRData.add(entityIndividualType);
		//indexRData.add(conceptIndividualType);
		//indexRData.add(regsiterItemType);
		indexRData.add(standardType);
		
		
		// Filters for Model
		indexRModel=new HashSet<Resource>();
		indexPModel=new HashSet<Property>();
		
		indexPModel.add(sitePrefixProperty);		// web publ.
		indexPModel.add(diskPrefixProperty);		// web publ.
		indexPModel.add(cssAddressProperty);		// web publ.
		indexPModel.add(tdbPrefixProperty);			// tdb publ.
		indexPModel.add(pragmaProperty);
		indexPModel.add(pragmaPropProperty);
		indexPModel.add(pragmaOverrideProp);
		indexPModel.add(containedInProperty);		// model constr.
		indexPModel.add(containsProperty);			// model constr.
		
		indexRModel.add(hasVersionProperty);		// version (name) is managed by data structures, rather than "term statements"

		indexPModel.add(hasNonVersionModel);		// (tdb) should never be found by design
		indexPModel.add(containsCollection);		// (tdb) should never be found by design
		indexPModel.add(containsIndividual);		// (tdb) should never be found by design
		indexPModel.add(hasVersionModel);			// (tdb) should never be found by design
		indexPModel.add(symmetricProperty);			
		indexPModel.add(generatesProperty);		
		
		indexPModel.add(overrideBaseSiteProperty);
		indexPModel.add(overrideBasePathProperty);
		
		//indexR.add(validStatus);
		//indexR.add(toBeValidatedStatus);
		indexRModel.add(terminologySetType);
		//indexRModel.add(conceptCollectionType);
		//indexRModel.add(registryType);
		//indexRModel.add(entityIndividualType);
		//indexRModel.add(conceptIndividualType);
		//indexRModel.add(regsiterItemType);
		indexRModel.add(standardType);
		indexRModel.add(pragmaExpandDashAndSuppress);
		indexRModel.add(pragmaSuppress);
		indexRModel.add(rootCollectionType); //TODO not used and should never be found by design	
		indexRModel.add(terminologyIndividualType);
		indexRModel.add(terminologySetType);
		
	}
	
	
		  
		       
		   


}
