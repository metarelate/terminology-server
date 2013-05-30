package net.metarelate.terminology.publisher.templateElements;

import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;

public class SetCodeValuesTemplate extends TemplateParametricClass implements
		TemplateTermElement {
	public static final String setCodeValHeader="$setCodesProps$";
	
	public SetCodeValuesTemplate(String templateText) {
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
		if(!e.isSet()) return "DEBUG:SETONLY";
		Set<TerminologyIndividual> codes=((TerminologySet)e).getIndividuals(version);
		TreeSet<String> properties=new TreeSet<String>();
		for(TerminologyIndividual code:codes) {
			StmtIterator stats=code.getStatements(version).listStatements();
			while(stats.hasNext()) {
				properties.add(stats.nextStatement().getPredicate().getURI().toString());
			}
		}
		String result=rawString;
		result=result.replace("<<tmtColNumber>>",new Integer(properties.size()+1).toString());
		
		String BlockHeader=sepTableDesc;
		for(int i=0;i<properties.size()+1;i++)	// TODO we also have the code!
			BlockHeader=BlockHeader+colTableDesc+sepTableDesc;
		
		result=result.replace("<<colBlockLatexStyle>>", BlockHeader);
		
		String description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, language);
		if(description==null) description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, CoreConfig.DEFAULT_LANGUAGE);
		if(description==null) description="No description found";
		result=result.replace("<<tmtDescription>>",description);
		
		
		String headerRow=headerRepBlockPre+"Code"+headerRepBlockPost; // TODO note that the code is hard-coded!
		boolean first=true;
		for(String prop:properties) {
			headerRow+=sepHeader;
			String label=lm.getLabelForURI(lm.getLabelForURI(prop, LabelManager.LANG_DEF_SHORTURI), LabelManager.LANG_DEF_SHORTURI);
			headerRow+=headerRepBlockPre+label+headerRepBlockPost;	
		}
		headerRow+=endHeader;
		result=result.replace("<<headerRow>>", headerRow);
		
		

	
		StringBuilder codesString=new StringBuilder();
		for(TerminologyIndividual code:codes) {
			String line="";
			String notation=code.getNotation(version);
			if(notation==null) notation=code.getResource().getLocalName();
			Model stats=code.getStatements(version);
			line+=notation;
			for(String property:properties) {
				String value="N/A";
				NodeIterator objects= stats.listObjectsOfProperty(stats.createProperty(property));
				while(objects.hasNext()) {
					RDFNode obj=objects.nextNode();
					if(obj.isLiteral()) {
						value=obj.asLiteral().getValue().toString();
						// TODO we need to do some real escaping here
						value=value.replace("&","\\&");
						value=value.replace("_","\\_");
					}
					else if(obj.isResource()) value=lm.getLabelForURI(obj.asResource().getURI().toString(), LabelManager.LANG_DEF_SHORTURI);
					//System.out.println("innerLoop2");
				}
				
				
				line+=sepLine+value;
			}
			line+=endLine;
			codesString.append(line+"\n");
		}
		result=result.replace("<<valuesRow>>",codesString.toString());
			
		
		
		return result;
	}

}
