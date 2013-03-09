package net.metarelate.terminology.webedit.validators;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class MinCardinalityValidator implements DaftValidator {
	String property=null;
	int min=-1;
	String language=null;
	
	public MinCardinalityValidator(String property, int minCardinality, String language) {
		this.property=property;
		this.language=language;
		min=minCardinality;
		
	}
	public boolean validate(Model model) {
		if(language==null) {
			int i=model.listStatements(null,ResourceFactory.createProperty(property),(RDFNode)null).toSet().size();
			if(i<min) return false;
			else return true;
		}
		else{
			int counter=0;
			StmtIterator stats=model.listStatements(null,ResourceFactory.createProperty(property),(RDFNode)null);
			while(stats.hasNext()) {
				Statement stat=stats.nextStatement();
				if(stat.getObject().isLiteral())
					if(stat.getObject().asLiteral().getLanguage()!=null)
						if(language.equals(stat.getObject().asLiteral().getLanguage()))
							counter++;
			}
			if(counter<min) return false;
			else return true;
		}
	}

	public String getMessage() {
		if(language==null) return "min. cardinality constraint violeted for "+property;
		else return "min. cardinality constraint violeted for "+property+" (lang="+language+")";
	}
	
	
	

}
