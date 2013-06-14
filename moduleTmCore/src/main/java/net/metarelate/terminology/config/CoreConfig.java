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

// TODO some import from table related properties may not be in use or should be renamed
package net.metarelate.terminology.config;

import net.metarelate.terminology.management.TerminologyManagerConfig;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Provide static strings used within the system (they are not meant to be used in the RDF representation of terminologies)
 * Also allows the redfinition of some system properties.
 * @author andrea_splendiani
 *
 */
public class CoreConfig {
	/**
	 * The initial version for a new entity
	 */
	public static final String VERSION_INIT = "0";
	
	/**
	 * A default version
	 */
	public static final String VERSION_DEFUALT = "0"; // TODO may be obsoleted
	
	/**
	 * Version number for the terminology server
	 */
	public static String VERSION_NUMBER="0.11.0";
	
	/**
	 * Label for the terminology server
	 */
	public static String VERSION_CODENAME="GenericRelease";
	
	/**
	 * Default label for something undefined
	 */
	public static String UNDEFINED_LABEL="undefined"; // TODO should be dealt with by the label manager
	
	/**
	 * Default action for the import from table action
	 */
	public static final String DEAFULT_FROM_RDF_IMPORT_ACTION = "http://metarelate.net/actions/batchImport";
	
	/**
	 * Default action for the re-import from table action
	 */
	public static final String DEAFULT_FROM_RDF_REIMPORT_ACTION = "http://metarelate.net/actions/batchImportOverride"; //TODO is this still in use ?
	
	/**
	 * Default state for the re-import from table action
	 */
	public static final String DEFAULT_IMPORT_STATUS = TerminologyManagerConfig.defaultStateURI;
	
	/**
	 * Default description for the import from table action
	 */
	public static String DEFAULT_FROM_RDF_DESCRIPTION="imported from table"; // TODO may be osboleted

	/**
	 * The URI for the global graph (holds relations between registers, URIs and their version graph)
	 */
	public static final String globalModel="http://metarelate.net/graph/globalMetaGraph";
	
	/**
	 * The URI for the labels graph
	 */
	public static final String labelModel="http://metarelate.net/graph/labels";
	
	/**
	 * The URI for the background knowledge graph
	 */
	public static final String extraModel="http://metarelate.net/graph/backgroundKnowledge";
	
	/**
	 * The URI for the cache graph
	 */
	public static final String cacheGraph="http://metarelate.net/graph/cache"; // TODO update docs and perhaps make this all backend specific later on

	/**
	 * The default language
	 */
	public static final String DEFAULT_LANGUAGE = "en";

	/**
	 * Default system dir
	 */
	public static final String systemRootDirString=".tserver";
	
	/**
	 * default git sub-dir
	 */
	public static final String gitDirString="git";	//TODO this and lot of the following are unused... took over by Installer, but unconnected for packaging simplification
	
	/**
	 * default db sub-dir
	 */
	public static final String dbDirString="db";
	
	/**
	 * default config sub-dir
	 */
	public static final String confDirString="conf";
	
	/**
	 * default auth sub-dir
	 */
	public static final String authDirString = "auth";

	/**
	 * default web sub-dir (admin web-app)
	 */
	public static String baseDiskDir="web";
	
	/**
	 * default cache sub-dir
	 */
	public static String cacheDirString="cache";
	
	/**
	 * default seed file
	 */
	public static final String seedFileString = "instanceSeed.ttl";
	
	/**
	 * default prefix file
	 */
	public static String prefixFileString="prefixFile.ttl";

	/**
	 * Default port for the admin web app
	 */
	public static int DEFAULT_ADMIN_PORT=8082;
	
	/**
	 * Default port for online publishing
	 */
	public static int DEFAULT_PUBLISH_PORT=8080;
	
	/**
	 * URI identifying "this instance" of the system 
	 */
	public static final String selfURI="http://metarelate.net/config/selfInstance";
	
	/**
	 * Property relating a terminology server instance to its ID
	 */
	public static final String hasInstanceIdentifierURI="http://metarelate.net/config/hasInstanceIdentifier";
	
	/**
	 * Property relating a terminology server instance to the URI of its default user
	 */
	public static final String hasDefaultUserURIProperty = "http://metarelate.net/config/hasDefaultUserID";
	/**
	 * Property specifying the DB location for the terminology server
	 */
	public static final String hasDBDirProperty="http://metarelate.net/config/hasDBDir";
	
	/**
	 * Property specifying the Cache location for the terminology server
	 */
	public static final String hasCacheDirProperty="http://metarelate.net/config/hasCacheDir";

	/**
	 * Property specifying the Auth location for the terminology server
	 */
	public static final String hasAuthDirProperty="http://metarelate.net/config/hasAuthDir";
	
	/**
	 * Property specifying the Git location for the terminology server
	 */
	public static final String hasGitDirProperty="http://metarelate.net/config/hasGitDir";
	
	/**
	 * Property specifying the Templates location for the terminology server
	 */
	public static final String hasTemplatesDirProperty="http://metarelate.net/config/hasTemplatesDir";
	
	/**
	 * Property specifying the Web (Admin war) location for the terminology server
	 */
	public static final String hasWebDirProperty="http://metarelate.net/config/hasWebPackageDir";
	
	/**
	 * Property specifying the Prefix file location for the terminology server
	 */
	public static final String hasPrefixFileProperty="http://metarelate.net/config/hasPrefixFile";
	
	/**
	 * Property specifying the Seed file location for the terminology server
	 */
	public static final String hasSeedFileProperty="http://metarelate.net/config/hasSeedFile";
	
	
	/**
	 * Read a configuration file and re-defines internal properties there redefined (overridden)
	 * @param configuration an RDF graph including ovverrides configuration directives
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
