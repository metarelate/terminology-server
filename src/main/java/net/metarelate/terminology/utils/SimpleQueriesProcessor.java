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

package net.metarelate.terminology.utils;

import java.util.ArrayList;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.NonConformantRDFException;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A collection of static methods to query an RDF graph (just a convenience)
 */
public class SimpleQueriesProcessor {
	/**
	 * Gets a label for a URI, if it exists, or returns its URI.
	 * @author andreasplendiani
	 *
	 */
	public static String getLabelorURIForURI(String URI,Model myModel) {
		String result="";
		result=getGenericValue(URI,MetaLanguage.labelProperty, myModel);
		if(result.equals("")) result=URI;
		return result;
	}
	
	public static String getLabelorUndefinedForURI(String URI,Model myModel) {
		String result="";
		result=getGenericValue(URI,MetaLanguage.labelProperty, myModel);
		if(result.equals("")) return CoreConfig.UNDEFINED_LABEL;
		return result;
	}
	
	
	private static String getGenericValue(String uri,Property property, Model myModel) {
		String result="";
		NodeIterator myResults= myModel.listObjectsOfProperty(ResourceFactory.createResource(uri), property);
		if(myResults.hasNext()) {
			RDFNode myRes=myResults.nextNode();
			if(myRes.isLiteral()) result=((Literal)myRes).getValue().toString();
		}
		return result;
	}
	
	
	public static boolean hasOptionalLiteral(Resource resource, Property property, Model model)  {
		NodeIterator literalIter=model.listObjectsOfProperty(resource,property);
		if(literalIter.hasNext()) return true;
		else return false;
	}

	/**
	 * Returns an optional literal of null if the optional lietaral is not found.
	 * Literals are converted to Strings.
	 * 
	 * @param resource
	 * @param property
	 * @param model
	 * @return
	 */
	public static String getOptionalLiteralValueAsString(Resource resource,
			Property property, Model model) {
		String value=null;
		NodeIterator myIter=model.listObjectsOfProperty(resource,property);
		if(myIter.hasNext()) {
			RDFNode node=myIter.nextNode();
			if(node.isLiteral()) 
				value=((Literal) node).getValue().toString();
		}
		return value;
	}
	
	public static Literal getOptionalLiteral(Resource resource,
			Property property, Model model) {
		Literal value=null;
		NodeIterator myIter=model.listObjectsOfProperty(resource,property);
		if(myIter.hasNext()) {
			RDFNode node=myIter.nextNode();
			if(node.isLiteral()) 
				value=node.asLiteral();
		}
		return value;
	}


	public static Literal getSingleMandatoryLiteral(Resource resource, Property property, Model model) throws NonConformantRDFException {
		NodeIterator literalIter=model.listObjectsOfProperty(resource,property);
		Literal result=null;
		if(!literalIter.hasNext()) throw new NonConformantRDFException("No literal found as object of "+resource+" , "+property);
		try {
			result=literalIter.nextNode().asLiteral();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NonConformantRDFException("Problems in finding literal for "+resource+" , "+property);
		}
		if(literalIter.hasNext()) throw new NonConformantRDFException("Too many objects for "+resource+" , "+property);
		return result;
	}	
	
	
	/**
	 * @param result
	 * @param property
	 * @param model
	 * @return  an arbitrary Resource object for a Model, Property, Subject, or null if no matching assertion is found.
	 */
	public static Resource getOptionalResourceObject(Resource result,
			Property property, Model model) {
		NodeIterator nodeIter=model.listObjectsOfProperty(result, property);
		while (nodeIter.hasNext()) {
			RDFNode node=nodeIter.nextNode();
			if(node.isResource()) return node.asResource();
		}
		return null;
	}


	public static String[] getArrayObjectsResourcesAsURIs(
			Resource resource, Property property,
			Model myGraph) {

		ArrayList<String> urisResult=new ArrayList<String>();

		NodeIterator myIter=myGraph.listObjectsOfProperty(resource,property);
		while(myIter.hasNext()) {
			RDFNode node=myIter.nextNode();
			if(node.isURIResource()) 
				urisResult.add(node.asResource().getURI());
		}
		return urisResult.toArray(new String[0]);
	}

	public static Resource getRequiredResourceObject(Resource resource,
			Property property, Model model) throws NonConformantRDFException {
		NodeIterator nodeIter=model.listObjectsOfProperty(resource, property);
		while (nodeIter.hasNext()) {
			RDFNode node=nodeIter.nextNode();
			if(node.isResource()) return node.asResource();
		}
		throw new NonConformantRDFException("Resource for property not found: "+resource.getURI()+" --- "+property.getURI());
	}
}
