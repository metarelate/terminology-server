package net.metarelate.terminology.webedit.validators;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class IsNumericValidator implements DaftValidator {
	String property=null;
	public IsNumericValidator(String property) {
		this.property=property;
	}

	public boolean validate(Model m) {
		NodeIterator objects=m.listObjectsOfProperty(ResourceFactory.createProperty(property));
		while(objects.hasNext()) {
			RDFNode object=objects.nextNode();
			if(object.isLiteral()) {
				String value=object.asLiteral().getValue().toString();
				//TODO note that here we could take an "extended" idea of numeric, 
				//allowing for instance for 123.23. In general, we could have a pattern validator.
				try{
					int i=Integer.parseInt(value);
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}

	public String getMessage() {
		return "Target of "+property+" is not numeric";
	}

}
