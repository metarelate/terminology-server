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

import net.metarelate.terminology.config.MetaLanguage;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

// TODO we should switch all of these to properties and resources (no strings)
public class TDBModelsCoreConfig {
	static final String 	globalModel="http://metoffice.gov.uk/terminology/impl/model/globalModel";
	static final String 	labelModel="http://metoffice.gov.uk/terminology/impl/model/labelModel";
	static final String 	extraModel = "http://metoffice.gov.uk/terminology/impl/model/backgroundKnowledgeModel";
	static final Property	hasTypeProperty = MetaLanguage.typeProperty;
	static final RDFNode 	TerminologySetType = MetaLanguage.terminologySetType;
	static final Property 	hasSubRegister = MetaLanguage.hasSubRegisterProperty;
	static final Property 	hasRegisterItem = MetaLanguage.hasRegisterItemProperty;
	static final Property 	definedInRegister = MetaLanguage.definedInRegister;

	static final RDFNode 	TerminologyIndividualType = MetaLanguage.terminologyIndividualType;
	static final Property 	hasVersionURIProperty=ResourceFactory.createProperty("http://metoffice.gov.uk/terminology/impl/model/hasVersionModel");
	static final Property 	hasOwnerProperty=MetaLanguage.hasManagerProperty;
	static final Property 	hasStateURI = MetaLanguage.hasStatusProperty;
	static final Property 	hasActionURI = MetaLanguage.versionActionProperty;
	static final Property 	hasActionAuthorURI = MetaLanguage.versionActorProperty;
	static final Property 	hasActionDate = MetaLanguage.versionActionDateProperty;
	static final Property 	hasActionDescription = MetaLanguage.versionActionDescription;
	static final Property 	isVersionedProperty = ResourceFactory.createProperty("http://metoffice.gov.uk/terminology/impl/model/isVersioningActive");
	static final Property 	hasVersionName = MetaLanguage.hasVersionProperty;
	static final Property 	hasPreviousVersionProperty = MetaLanguage.hasPreviousVersionProperty;
	
	

}
