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

/*
 * TODO this should be re-designed with a wrapper patterm for 
 * multi-backend persistence. Same for all classes in this package!
 */
package net.metarelate.terminology.coreModel;

import net.metarelate.terminology.config.MetaLanguage;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

// TODO we should switch all of these to properties and resources (no strings)
// TODO this should all move to MetaLanguage or CoreConfig

/**
 * Contains static fields used throughout the package.
 * Fields are mostly defined in @see net.metarelate.terminology.config.MetaLanguage and @see net.metarelate.terminology.config.CoreConfig
 * For the most part, these fields are defined here only to allow a possibility for re-definition in principle.
 * @author andrea_splendiani
 *
 */
public class TDBModelsCoreConfig {
	
	static final Property	hasTypeProperty = MetaLanguage.typeProperty;
	static final RDFNode 	TerminologySetType = MetaLanguage.terminologySetType;
	static final Property 	hasSubRegister = MetaLanguage.definesProperty;
	static final Property 	hasRegisterItem = MetaLanguage.definesProperty;
	static final Property 	definedInRegister = MetaLanguage.definedInProperty;

	static final RDFNode 	TerminologyIndividualType = MetaLanguage.terminologyIndividualType;
	
	/**
	 * Link between an entity and a model containing a version snapshot (global graph)
	 */
	static final Property 	hasVersionURIProperty=ResourceFactory.createProperty("http://metarelate.net/core/structure/hasVersionStatements");
	//static final Property 	hasOwnerProperty=MetaLanguage.hasManagerProperty;
	static final Property 	hasStateURI = MetaLanguage.hasStatusProperty;
	static final Property 	hasActionURI = MetaLanguage.versionActionProperty;
	static final Property 	hasActionAuthorURI = MetaLanguage.versionActorProperty;
	static final Property 	hasActionDate = MetaLanguage.versionActionDateProperty;
	static final Property 	hasActionDescription = MetaLanguage.versionActionDescription;
	
	/**
	 * Indicates that for an entity is under the terminology version control
	 */
	static final Property 	isVersionedProperty = ResourceFactory.createProperty("http://metarelate.net/core/system/isVersioningActive");
	static final Property 	hasPreviousVersionProperty = MetaLanguage.hasPreviousVersionProperty;
	
	

}
