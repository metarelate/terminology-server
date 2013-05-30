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

package net.metarelate.terminology.config;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.metarelate.terminology.management.TerminologyManagerConfig;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;


public class CoreConfig {
	public static final String VERSION_INIT = "0";
	public static final String VERSION_DEFUALT = "0";
	
	public static String VERSION_NUMBER="0.9.0";
	public static String VERSION_CODENAME="de-WMOized";
	
	public static String UNDEFINED_LABEL="undefined";
	
	//public static String totalGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/totalGraph";
	//public static String labelsGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/labelsGraph";
	//public static String extraGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/extraGraph";

	public static String DEFAULT_FROM_RDF_DESCRIPTION="imported from table";

	
	public static final String globalModel="http://metarelate.net/graph/globalMetaGraph";
	public static final String labelModel="http://metarelate.net/graph/labels";
	public static final String extraModel="http://metarelate.net/graph/backgroundKnowledge";
	public static final String cacheGraph="http://metarelate.net/graph/cache"; // TODO update docs and perhaps make this all backend specific later on

	public static final String DEAFULT_FROM_RDF_IMPORT_ACTION = "http://metarelate.net/actions/batchImport";
	public static final String DEAFULT_FROM_RDF_REIMPORT_ACTION = "http://metarelate.net/actions/batchImportOverride";
	
	public static final String DEFAULT_IMPORT_STATUS = TerminologyManagerConfig.defaultStateURI;

	public static final String DEFAULT_LANGUAGE = "en";

	
	//public static Resource totalGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/totalGraph");
	//public static Resource labelsGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/labelsGraph");
	//public static Resource extraGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/extraGraph");

	public static final String rootDirString=".tserver";
	public static final String gitDirString="git";
	public static final String dbDirString="db";
	public static final String confDirString="conf";
	public static final String authDirString = "auth";

	public static final String seedFileString = "instanceSeed.ttl";
	public static String prefixFileString="prefixFile.ttl";

	public static String baseDiskDir="web";
	public static String cacheDirString="cache";
	
	public static final String hasInstanceIdentifierURI="http://metarelate.net/config/hasInstanceIdentifier";
	public static final String selfURI="http://metarelate.net/config/selfInstance";
	
	/*
	 * Here we have all generic properties that can be overloaded (state/transition systems has a different mechanism)
	 */
	public static void parseConfiguration(Model configuration) {
		StmtIterator statsIterator=configuration.listStatements(null,MetaLanguage.overridesProperty,(Resource)null);
		while(statsIterator.hasNext()) {
			Statement currentStat=statsIterator.nextStatement();
			Resource overrider=currentStat.getSubject();
			Resource overridden=currentStat.getObject().asResource();
			
			if(overridden!=null) {
				if(overridden.getURI().equals(MetaLanguage.labelProperty.getURI())) {
					MetaLanguage.labelProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.definedInProperty.getURI())) {
					MetaLanguage.definedInProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.definesProperty.getURI())) {
					MetaLanguage.definesProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.commentProperty.getURI())) {
					MetaLanguage.commentProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.hasStatusProperty.getURI())) {
					MetaLanguage.hasStatusProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.notationProperty.getURI())) {
					MetaLanguage.notationProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				
				if(overridden.getURI().equals(MetaLanguage.versionActionDateProperty.getURI())) {
					MetaLanguage.versionActionDateProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.versionActionProperty.getURI())) {
					MetaLanguage.versionActionProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.versionActorProperty.getURI())) {
					MetaLanguage.versionActorProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.versionActionDescription.getURI())) {
					MetaLanguage.versionActionDescription=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.hasPreviousVersionProperty.getURI())) {
					MetaLanguage.hasPreviousVersionProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				
				if(overridden.getURI().equals(MetaLanguage.hasManagerProperty.getURI())) {
					MetaLanguage.hasManagerProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				
				if(overridden.getURI().equals(MetaLanguage.hasVersionProperty.getURI())) {
					MetaLanguage.hasVersionProperty=ResourceFactory.createProperty(overrider.getURI());
				}
				
				if(overridden.getURI().equals(MetaLanguage.superseeds.getURI())) {
					MetaLanguage.superseeds=ResourceFactory.createProperty(overrider.getURI());
				}
				if(overridden.getURI().equals(MetaLanguage.superseededBy.getURI())) {
					MetaLanguage.superseededBy=ResourceFactory.createProperty(overrider.getURI());
				}
				
			}
		}
		
		
	}
	
	
}
