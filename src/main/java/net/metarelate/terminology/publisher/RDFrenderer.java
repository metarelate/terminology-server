package net.metarelate.terminology.publisher;

import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class RDFrenderer {
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
			modelToWrite.add(ResourceFactory.createStatement(collectionResource,MetaLanguage.hasPreviousVersionProperty,ResourceFactory.createPlainLiteral(version)));

	
		
	}
	
	
}
