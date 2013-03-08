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

public class MinCardinalityValidator implements DaftValidator {
	String property=null;
	int min=-1;
	
	public MinCardinalityValidator(String property, int minCardinality) {
		this.property=property;
		min=minCardinality;
		
	}
	public boolean validate(Model model) {
		int i=model.listStatements(null,ResourceFactory.createProperty(property),(RDFNode)null).toSet().size();
		if(i<min) return false;
		else return true;
		
	}

	public String getMessage() {
		return "min. cardinality constraint violeted for "+property;
	}
	
	
	

}
