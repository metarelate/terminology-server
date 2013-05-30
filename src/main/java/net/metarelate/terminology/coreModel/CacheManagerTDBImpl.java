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

package net.metarelate.terminology.coreModel;

import java.util.List;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.instanceManager.Initializer;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;

public class CacheManagerTDBImpl implements CacheManager {
	private Initializer myInitializer=null;
	private Dataset auxDataset=null;
	private Model cacheGraph=null;

	public CacheManagerTDBImpl(Initializer initializer, String cacheLocation) { //TODO we could only only pass the initializer with a multi-staged init sequence
		this.myInitializer=initializer;
		auxDataset = TDBFactory.createDataset(cacheLocation);
		cacheGraph=auxDataset.getNamedModel(CoreConfig.cacheGraph);
	}
	/* (non-Javadoc)
	 * @see net.metarelate.terminology.instanceManager.CacheManager#getValueFor(java.lang.String, java.lang.String)
	 */
	public String getValueFor(String resource, String property) {
		Resource subjectRes=ResourceFactory.createResource(resource);
		Property proRes=ResourceFactory.createProperty(property);
		NodeIterator currentValues=cacheGraph.listObjectsOfProperty(subjectRes, proRes);
		if(currentValues.hasNext()) {
			RDFNode currentValue=currentValues.nextNode();
			if(currentValue.isLiteral())
				return currentValue.asLiteral().getValue().toString();	
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see net.metarelate.terminology.instanceManager.CacheManager#cleanValueFor(java.lang.String, java.lang.String)
	 */
	public boolean cleanValueFor(String resource, String property) {
		Resource subjectRes=ResourceFactory.createResource(resource);
		Property proRes=ResourceFactory.createProperty(property);
		NodeIterator currentValues=cacheGraph.listObjectsOfProperty(subjectRes, proRes);
		if(!currentValues.hasNext()) return false;
		List<RDFNode> toRemove=currentValues.toList();
		for(RDFNode val:toRemove) {
			cacheGraph.remove(subjectRes,proRes,val);
		}
		return true;	
		
	}
	/* (non-Javadoc)
	 * @see net.metarelate.terminology.instanceManager.CacheManager#recordValue(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void recordValue(String resource, String property, String value) {
		cacheGraph.add(cacheGraph.createStatement(
				ResourceFactory.createResource(resource), 
				ResourceFactory.createProperty(property), 
				ResourceFactory.createPlainLiteral(value))	);
	}
	/* (non-Javadoc)
	 * @see net.metarelate.terminology.instanceManager.CacheManager#changeValue(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void changeValue(String resource, String property, String value) {
		cleanValueFor(resource,property);
		recordValue(resource,property,value);
	}
	public void forceCleanProp(String propertyURI) {
		Model toRemove=ModelFactory.createDefaultModel();
		StmtIterator toRemItemIter=cacheGraph.listStatements(null,ResourceFactory.createProperty(propertyURI),(RDFNode)null);
		while(toRemItemIter.hasNext()) toRemove.add(toRemItemIter.nextStatement());
		toRemItemIter=cacheGraph.listStatements(null,ResourceFactory.createProperty(propertyURI),(Literal)null);
		while(toRemItemIter.hasNext()) toRemove.add(toRemItemIter.nextStatement());
		cacheGraph.remove(toRemove);
		TDB.sync(cacheGraph);
	}
	public void synch() {
		TDB.sync(cacheGraph);
		
	}
	//TODO note that we assume there's only one URI for a url...
	public String getSubjectForValue(String urlRequested, String property) {
		Literal objectRes=ResourceFactory.createPlainLiteral(urlRequested);
		Property proRes=ResourceFactory.createProperty(property);
		ResIterator currentValues=cacheGraph.listSubjectsWithProperty(proRes,objectRes);
		if(currentValues.hasNext()) {
			return currentValues.nextResource().getURI().toString();	
		}
		return null;
		
		
	}

	
}
