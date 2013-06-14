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

import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
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
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.utils.Loggers;

/**
 * Generates a table of codes for a set
 * @author andreasplendiani
 *
 */
public class SetCodeValuesTemplate extends TemplateParametricClass implements
		TemplateTermElement {
	public static final String setCodeValHeader="$setCodesProps$";
	
	public SetCodeValuesTemplate(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New SetCodeValuesTemplate\n"+templateText);
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
			String registryBaseURL,String tag) throws ModelException, WebWriterException {
		if(tag==null) throw new WebWriterException("Template SetCodesValues needs a tag!");
		Loggers.publishLogger.debug("Rendering latexy table for "+e.getURI()+" v. "+version+" l="+level);
		if(!e.isSet()) return "DEBUG:SETONLY";
		Set<TerminologyIndividual> codes=((TerminologySet)e).getIndividuals(version);
		Loggers.publishLogger.trace("No. individuals "+codes.size());
		TreeSet<String> properties=new TreeSet<String>();
		for(TerminologyIndividual code:codes) {
			//Which code version ?
			String[] codeVersions=code.getVersionsForTag(tag);
			if(codeVersions.length>1) throw new WebWriterException("Only one version x tag is supported by SetCodesValue template!");
			if(codeVersions.length==0) throw new ModelException("Asynch containment in set and codes for: "+code+" at tag: "+tag);
			StmtIterator stats=code.getStatements(codeVersions[0]).listStatements();
			while(stats.hasNext()) {
				Statement stat=stats.nextStatement();
				Loggers.publishLogger.trace("Found stat "+stat.toString());
				properties.add(stat.getPredicate().getURI().toString());
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
			String label=lm.getLabelForURI(prop, LabelManager.LANG_DEF_SHORTURI);
			headerRow+=headerRepBlockPre+label+headerRepBlockPost;	
		}
		headerRow+=endHeader;
		result=result.replace("<<headerRow>>", headerRow);
		
		

	
		StringBuilder codesString=new StringBuilder();
		for(TerminologyIndividual code:codes) {
			String line="";
			String codeVersion=code.getVersionsForTag(tag)[0]; // TODO this should have been already checked
			String notation=code.getNotation(codeVersion);
			if(notation==null) notation=code.getResource().getLocalName();
			Model stats=code.getStatements(codeVersion);
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
