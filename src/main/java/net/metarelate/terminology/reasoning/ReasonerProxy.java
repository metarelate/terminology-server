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

package net.metarelate.terminology.reasoning;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * A reasoner wrapper.
 * 
 * This class provides some reasoning services. The only public method {@link #customReason} accepts a set of RDF statements as a parameter and enriches this set with inferred statements.
 * This class is intended as a place holder for more complex reasoning procedures, that could be easily implemented by importing third parties libraries.
 * The primary use of this class in the Met Office Terminology Manager Prototype is to expand an RDF representation of a terminology set provided by the d2rq system.
 * 
 * @author andreasplendiani
 *
 */
public class ReasonerProxy {
	/**
	 * Accepts a set of statements and enriches it with inferred statements.
	 * Inference could follow standard axioms sets (e.g. RDFS, OWL) or it could be customize (e.g. rules).
	 * In the current implementation, this method will check for symmetric properties and "generative" properties (more info in the Met Office Terminology Manager Prototype documentation).
	 * The specification of which properties are symmetric or generative is expected to be part of the input RDF.
	 * 
	 * @param unionOfAllRDFToProcess The RDF input, including statements on which to apply inference and (eventually) statements that specify inference features.
	 */
	public static void customReason(Model unionOfAllRDFToProcess) {
		computeSymmetric(unionOfAllRDFToProcess);
		computeGenerates(unionOfAllRDFToProcess);
	}
	
	private static void computeSymmetric(Model rdfToInferOn) {
		StmtIterator myStats=rdfToInferOn.listStatements(null,ReasonerProxyConfig.symmetricProperty,(Resource)null);
		Hashtable<Property,Property> symProps=new Hashtable<Property,Property>();
		while(myStats.hasNext()) {
			Statement stat=myStats.next();
			Property prop1=ResourceFactory.createProperty(stat.getSubject().getURI());
			Property prop2=ResourceFactory.createProperty(stat.getObject().asResource().getURI());
			symProps.put(prop1, prop2);
			symProps.put(prop2, prop1);
			SSLogger.log("Symmetric properties: "+prop1.getURI()+" and "+prop2.getURI());
		}
		Model inferred=ModelFactory.createDefaultModel();
		// This could be done better, checking only the statements we know are symmetric
		Iterator<Property> toInfer=symProps.keySet().iterator();
		while(toInfer.hasNext()) {
			
			Property tempProp=toInfer.next();
			SSLogger.log("Checking statements for: "+tempProp.getURI());
			Iterator<Statement> tempStats=rdfToInferOn.listStatements(null,tempProp,(Resource)null);
			while(tempStats.hasNext()) {
				Statement tempStat=tempStats.next();
				inferred.add(ResourceFactory.createStatement(tempStat.getObject().asResource(), symProps.get(tempStat.getPredicate()), tempStat.getSubject()));
				SSLogger.log("Inferred reverse for "+tempStat.toString());
			}
			rdfToInferOn.add(inferred);
		}
	}
	
	private static void computeGenerates(Model rdfToInferOn) {
		SSLogger.log("Looking for generative properties",SSLogger.DEBUG);
		StmtIterator myStats=rdfToInferOn.listStatements(null,ReasonerProxyConfig.generatesPropertyProperty,(Resource)null);
		Hashtable<Property,Set<Property>> genProps=new Hashtable<Property,Set<Property>>();
		while(myStats.hasNext()) {
			Statement stat=myStats.next();
			Property prop1=ResourceFactory.createProperty(stat.getSubject().getURI());
			Property prop2=ResourceFactory.createProperty(stat.getObject().asResource().getURI());
			if(!genProps.containsKey(prop1)) {
				Set<Property> newSet=new HashSet<Property>();
				genProps.put(prop1,newSet);
				newSet.add(prop2);
			}
			else {
				genProps.get(prop1).add(prop2);
			}
			SSLogger.log("Generative properties: "+prop1.getURI()+" yields "+prop2.getURI());
		}
		Model inferred=ModelFactory.createDefaultModel();
		// This could be done better, checking only the statements we know are symmetric
		Iterator<Property> toInfer=genProps.keySet().iterator();
		while(toInfer.hasNext()) {	
			Property tempProp=toInfer.next();
			SSLogger.log("Checking statements for: "+tempProp.getURI());
			Iterator<Statement> tempStats=rdfToInferOn.listStatements(null,tempProp,(Resource)null);
			while(tempStats.hasNext()) {
				Statement tempStat=tempStats.next();
				Set<Property> genSet=genProps.get(tempProp);
				Iterator<Property> propsToAssert=genSet.iterator();
				while(propsToAssert.hasNext()) {
					Property propToAssert=propsToAssert.next();
					Statement newStatement=ResourceFactory.createStatement(tempStat.getSubject().asResource(), propToAssert, tempStat.getObject());
					inferred.add(newStatement);
					SSLogger.log("Inferred statement: "+newStatement.toString());
				}
				
			}
			
		}
		
		///
		
		StmtIterator myStats2=rdfToInferOn.listStatements(null,ReasonerProxyConfig.generatesTypeProperty,(Resource)null);
		Hashtable<Resource,Set<Resource>> genTypes=new Hashtable<Resource,Set<Resource>>();
		while(myStats2.hasNext()) {
			Statement stat=myStats2.next();
			Resource type1=ResourceFactory.createProperty(stat.getSubject().getURI());
			Resource type2=ResourceFactory.createProperty(stat.getObject().asResource().getURI());
			if(!genTypes.containsKey(type1)) {
				Set<Resource> newSet=new HashSet<Resource>();
				genTypes.put(type1,newSet);
				newSet.add(type2);
			}
			else {
				genTypes.get(type1).add(type2);
			}
			SSLogger.log("Generative types : "+type1.getURI()+" yields "+type2.getURI());
		}
		Model inferred2=ModelFactory.createDefaultModel();
		// This could be done better, checking only the statements we know are symmetric
		Iterator<Resource> toInfer2=genTypes.keySet().iterator();
		while(toInfer2.hasNext()) {	
			Resource tempType=toInfer2.next();
			SSLogger.log("Checking statements for: "+tempType.getURI());
			Iterator<Statement> tempStats=rdfToInferOn.listStatements(null,MetaLanguage.typeProperty,tempType);
			while(tempStats.hasNext()) {
				Statement tempStat=tempStats.next();
				Set<Resource> genSet=genTypes.get(tempType);
				Iterator<Resource> typesToAssert=genSet.iterator();
				while(typesToAssert.hasNext()) {
					Resource typeToAssert=typesToAssert.next();
					Statement newStatement=ResourceFactory.createStatement(tempStat.getSubject().asResource(), MetaLanguage.typeProperty, typeToAssert);
					inferred2.add(newStatement);
					SSLogger.log("Inferred statement: "+newStatement.toString());
				}
				
			}
			
		}
		rdfToInferOn.add(inferred);
		rdfToInferOn.add(inferred2);
		
	}
	
	
}
