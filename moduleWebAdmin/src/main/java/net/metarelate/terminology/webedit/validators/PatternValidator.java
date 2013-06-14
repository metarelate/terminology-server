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
 * Validates the Pattern constraint
 * @see DaftValidator
 * @author andrea_splendiani
 *
 */
public class PatternValidator implements DaftValidator {
	String property=null;
	String pattern=null;

	public PatternValidator(String property, String pattern) {
		this.property=property;
		this.pattern=pattern;
	}

	public boolean validate(Model m) {
		// TODO Auto-generated method stub
		NodeIterator objects=m.listObjectsOfProperty(ResourceFactory.createProperty(property));
		while(objects.hasNext()) {
			RDFNode object=objects.nextNode();
			if(object.isLiteral()) {
				String value=object.asLiteral().getValue().toString();
				if(!value.matches(pattern)) return false;
			}
		}
		return true;
		
		
	}

	public String getMessage() {
		return "Target of "+property+" does not match the pattern "+pattern;

	}

}
