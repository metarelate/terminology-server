package net.metarelate.terminology.publisher.templateElements;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.WebRendererStrings;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.StatementSorter;

public class StatementsTemplateElement extends TemplateParametricClass
		implements TemplateTermElement {
	public static final String statHeader="$statBlock$";
	public StatementsTemplateElement(String templateText) {
		super(templateText);
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
			String registryBaseURL) throws ModelException {
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
		
		Collection<Statement> orderedStatMapConcept=StatementSorter.orderStatementsByLiteralObject(statSetConcept,e.getFactory());
		Collection<Statement> orderedStatMapCode=StatementSorter.orderStatementsByLiteralObject(statSetCode,e.getFactory());
		Collection<Statement> orderedStatMapOther=StatementSorter.orderStatementsByLiteralObject(statSetUndef,e.getFactory());
		
		if(statementsBlockMode.equalsIgnoreCase("html")) {
			result.append(buildSubBlock(orderedStatMapConcept, counterCode, statementsBlockCodeLabel, language, lm));
			result.append(buildSubBlock(orderedStatMapCode, counterCode, statementsBlockConceptLabel, language, lm));
			result.append(buildSubBlock(orderedStatMapOther, counterCode, statementsBlockOtherLabel, language, lm));
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
