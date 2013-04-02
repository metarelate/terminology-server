package net.metarelate.terminology.webedit.validators;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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
