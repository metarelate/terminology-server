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


package net.metarelate.terminology.webedit.validators;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Validates the oneOf constraint
 * @see DaftValidator
 * @author andrea_splendiani
 *
 */
public class OptionValidator implements DaftValidator {
	String[] options=null;
	String property=null;
	public OptionValidator(String property, String[] options) {
		this.options=options;
		this.property=property;
	}

	public boolean validate(Model m) {
		NodeIterator objects=m.listObjectsOfProperty(ResourceFactory.createProperty(property));
		boolean collectiveToReturn=true;	//return false if at least once it's false.
		while(objects.hasNext()) {
			RDFNode object=objects.nextNode();
			String needle=null;
			if(object.isURIResource()) {
				 needle=object.asResource().getURI();
			}
			else if(object.isLiteral()) {
				needle=object.asLiteral().getValue().toString();
			}
			else return false;
		
			boolean localToReturn=false; //return true if at least one option is good.
			for(String o:options) {
				localToReturn=localToReturn||o.equals(needle);
			}
			collectiveToReturn=collectiveToReturn&&localToReturn;
		}
		return collectiveToReturn;
	}

	public String getMessage() {
		return "Invalid target for "+property+" (option constraints)";
	}

}
