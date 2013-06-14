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

import net.metarelate.terminology.management.ConstraintsManagerConfig;
import net.metarelate.terminology.webedit.CommandWebConsole;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Validates the inRegister constraint
 * @see DaftValidator
 * @author andrea_splendiani
 *
 */
public class InRegisterValidator implements DaftValidator {
	String property=null;
	String register=null;
	
	public InRegisterValidator(String property,String register) {
		super();
		this.property = property;
		this.register=register;
	}

	public boolean validate(Model m) {
		NodeIterator objects=m.listObjectsOfProperty(ResourceFactory.createProperty(property));
		while(objects.hasNext()) {
			RDFNode object=objects.nextNode();
			if(!object.isURIResource()) return false;
			//First the entity must be known!
			if((!CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(object.asResource().getURI()))
				&&
				(!CommandWebConsole.myInitializer.myFactory.terminologySetExist(object.asResource().getURI()))
			) return false;
			if(!register.equals(ConstraintsManagerConfig.allRegisters)) {
				//we make a more specific test here
				if(!CommandWebConsole.myInitializer.myFactory.terminologySetExist(register)) return false;
				else {
					if(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(register).containsEntity(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyEntity(object.asResource().getURI()))) return true;
					else return false;
				}
			}
			
		}
		return true;
	}

	public String getMessage() {
		return "object of "+property+" is not an URI already defined";
	}

}
