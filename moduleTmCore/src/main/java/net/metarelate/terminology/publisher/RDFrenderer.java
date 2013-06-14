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

package net.metarelate.terminology.publisher;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
/**
 * Provides an RDF representation of a code or register information.
 * This basically combines information from the global and local graph.
 * @author andreasplendiani
 *
 */
public class RDFrenderer {
	/**
	 * returns a set of statements for the specific version of the entity, which is enriched with information from the global graph
	 * @param entity
	 * @param version
	 * @return
	 */
	public static Model prepareModel(TerminologyEntity entity, String version) {
		Model modelToWrite=ModelFactory.createDefaultModel();
		modelToWrite.add(MetaLanguage.filterForData(entity.getStatements(version)));
		
		modelToWrite.add(ResourceFactory.createStatement(entity.getResource(),MetaLanguage.hasStatusProperty,ResourceFactory.createResource(entity.getStateURI(version))));
		modelToWrite.add(ResourceFactory.createStatement(entity.getResource(),MetaLanguage.hasManagerProperty,ResourceFactory.createResource(entity.getOwnerURI())));
		
		if(entity.isVersioned()) triplifyVersion(modelToWrite,entity,version);
	
		return modelToWrite;
	}

	private static void triplifyVersion(Model modelToWrite,TerminologyEntity collection,
			  String version) {
		Resource collectionResource=ResourceFactory.createResource(collection.getURI());
		modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasVersionProperty,ResourceFactory.createPlainLiteral(version)));
		if(collection.hasPreviousVersion(version)) 
			modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasPreviousVersionProperty,ResourceFactory.createResource(collection.getVersionURI(collection.getPreviousVersion(version)))));

	
		
	}
	
	
}
