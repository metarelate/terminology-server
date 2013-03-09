package net.metarelate.terminology.webedit.validators;

import com.hp.hpl.jena.rdf.model.Model;

public class IsURIValidator implements DaftValidator {
	String property=null;
	public IsURIValidator(String property) {
		this.property=property;
	}

	public boolean validate(Model m) {
		// TODO Not sure we can this here as we already have a model
		return true;
	}

	public String getMessage() {
		return "Target of "+property+" is not a URI";
	}

}
