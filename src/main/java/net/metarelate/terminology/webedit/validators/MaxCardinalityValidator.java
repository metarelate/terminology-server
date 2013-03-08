package net.metarelate.terminology.webedit.validators;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class MaxCardinalityValidator implements DaftValidator {
	String property=null;
	int max=-1;
	
	public MaxCardinalityValidator(String property, int maxCardinality) {
		this.property=property;
		max=maxCardinality;
		
	}
	public boolean validate(Model model) {
		int i=model.listStatements(null,ResourceFactory.createProperty(property),(RDFNode)null).toSet().size();
		if(max>0 && i>max) return false;
		else return true;
		
	}
	public String getMessage() {
		return "max. cardinality constraint violeted for "+property;
	}

	
	
	

}
