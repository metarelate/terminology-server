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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Validates the MaxCardinality constraint
 * @see DaftValidator
 * @author andrea_splendiani
 *
 */
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
