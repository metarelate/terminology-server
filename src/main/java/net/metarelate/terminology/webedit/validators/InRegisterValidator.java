package net.metarelate.terminology.webedit.validators;

import net.metarelate.terminology.management.ConstraintsManagerConfig;
import net.metarelate.terminology.webedit.CommandWebConsole;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
