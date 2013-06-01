package net.metarelate.terminology.webedit.validators;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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
