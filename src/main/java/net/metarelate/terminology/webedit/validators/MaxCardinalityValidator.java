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

public class MaxCardinalityValidator implements DaftValidator {
	String property=null;
	int max=-1;
	String language=null;
	
	public MaxCardinalityValidator(String property, int maxCardinality,String language) {
		this.property=property;
		max=maxCardinality;
		this.language=language;
		
	}
	public boolean validate(Model model) {
		if(language==null) {
			int i=model.listStatements(null,ResourceFactory.createProperty(property),(RDFNode)null).toSet().size();
			if(max>0 && i>max) return false;
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
			if(max>0 && max>counter) return false;
			else return true;
		}
		
	}
	public String getMessage() {
		if(language==null) return "max. cardinality constraint violeted for "+property;
		else return "min. cardinality constraint violeted for "+property+" (lang="+language+")";
	}

	
	
	

}
