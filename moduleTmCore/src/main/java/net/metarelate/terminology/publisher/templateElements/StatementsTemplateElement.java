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


package net.metarelate.terminology.publisher.templateElements;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.StatementsOrganizer;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Generates a html representation of a list of statements
 * @author andreasplendiani
 *
 */
public class StatementsTemplateElement extends TemplateParametricClass
		implements TemplateTermElement {
	public static final String statHeader="$statBlock$";
	public StatementsTemplateElement(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New StatementsTemplateElement\n"+templateText);
		// TODO Auto-generated constructor stub
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version, int level,
			String language, String baseURL, CacheManager cacheManager,
			LabelManager lm, BackgroundKnowledgeManager bkm,
			String registryBaseURL,String tag) throws ModelException {
		StringBuilder result=new StringBuilder();
		
		Model statementsToRender=MetaLanguage.filterForWeb(e.getStatements(version));
		StmtIterator stUnsortedIter=statementsToRender.listStatements();
		int counterCode=0;
		int counterConcept=0;
		int counterUndef=0;
		Set<Statement> statSetConcept=new HashSet<Statement>();
		Set<Statement> statSetCode=new HashSet<Statement>();
		Set<Statement> statSetUndef=new HashSet<Statement>();
		
		while(stUnsortedIter.hasNext()) {
			Statement stat=stUnsortedIter.nextStatement();
			if(bkm.getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyCodeFocus)) {
				statSetCode.add(stat); 
				counterCode++;
			}
			else if(bkm.getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyConceptFocus)) {
				statSetConcept.add(stat);
				counterConcept++;
			}
			else {
				statSetUndef.add(stat);
				counterUndef++;
			}
		}
		
		Collection<Statement> orderedStatMapConcept=StatementsOrganizer.orderStatementsByLiteralObject(statSetConcept,e.getFactory());
		Collection<Statement> orderedStatMapCode=StatementsOrganizer.orderStatementsByLiteralObject(statSetCode,e.getFactory());
		Collection<Statement> orderedStatMapOther=StatementsOrganizer.orderStatementsByLiteralObject(statSetUndef,e.getFactory());
		
		if(statementsBlockMode.equalsIgnoreCase("html")) {
			result.append(buildSubBlock(orderedStatMapConcept, counterConcept, statementsBlockCodeLabel, language, lm));
			result.append(buildSubBlock(orderedStatMapCode, counterCode, statementsBlockConceptLabel, language, lm));
			result.append(buildSubBlock(orderedStatMapOther, counterUndef, statementsBlockOtherLabel, language, lm));
		}
		
		return result.toString();
	}

	private String buildSubBlock(Collection<Statement> stats,int total,String label,String language,LabelManager lm) {
		Iterator<Statement> stIterCode= stats.iterator();
		String result3="";
		if(stIterCode.hasNext()) {
			Statement stat=stIterCode.next();
			result3+="<tr>\n"+
			"<th scope=\"row\" id=\"propCodeRow\" rowspan=\""+total+"\">"+label+"</th>";
			result3+=getStatementsRow(stat,language,lm)+"</tr>";
			while(stIterCode.hasNext()) {
				stat=stIterCode.next();
				result3+="<tr>"+getStatementsRow(stat,language,lm)+"</tr>";
			}
			result3+="\n";
		}
		return result3;
	}
	
	private String getStatementsRow(Statement stat,String language,LabelManager lm) {
		String resultl="";
		Property pred=stat.getPredicate();
		resultl+="<td>"+lm.getLabelForURI(pred.getURI(), language,LabelManager.LANG_DEF_URI)+"</td>";
		RDFNode myNode=stat.getObject();
		if(myNode.isResource()) {
			String uri=((Resource)myNode).getURI();
			String label=lm.getLabelForURI(uri,language, LabelManager.LANG_DEF_URI);
			resultl+="<td><a href=\""+uri+"\">"+label+"</a></td>";
		}
		else if (myNode.isLiteral()) {
			String value= ((Literal)myNode).getValue().toString();
			resultl+="<td>"+value+"</td>";
		}
		else resultl+="<td>n/a</td>";
		return resultl+"\n";
	}
	
	
	
	
}
