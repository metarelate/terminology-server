/* 
 (C) British Crown Copyright 2011 - 2012, Met Office

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

package net.metarelate.terminology.publisher;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;

public class WebRendererIndividual extends WebRendererItem {
	TerminologyIndividual myTerm;
	public WebRendererIndividual(TerminologyIndividual term, String url) {
		super(term);
		myTerm=term;
		itemUrl=url;
		//labelsModel=labelRepository;
		
		determineType(term.getLastVersion());
		SSLogger.log("created renderer for indivudual "+term.getURI()+" style :"+type);
	}

	protected  void determineType(String version) {
		if(entity.getStatements(version).contains(entity.getResource(),MetaLanguage.typeProperty,MetaLanguage.skosCollectionType)) {
			this.type=STYLE_TERM_SKOS_COLLECTION;
		}
		else if (entity.getStatements(version).contains(entity.getResource(),MetaLanguage.typeProperty,MetaLanguage.skosConceptType)) {
			this.type=STYLE_TERM_SKOS_CONCEPT;
		}
		else if (entity.getStatements(version).contains(entity.getResource(),MetaLanguage.typeProperty,MetaLanguage.skosSchemeType)) {
			this.type=STYLE_SET_SKOS_SCHEME;
		}
	}

/*
	@Override
	public String getVersionHeader(SortedMap<String, String> stdMap,
			String version) {
		//Writing collection header
		String code=myTerm.getGenericVersionSpecificStringValueObject(MetaLanguage.notationProperty, version);
		String comment=myTerm.getGenericVersionSpecificStringValueObject(MetaLanguage.commentProperty,version);
		
		//TODO this may be removed
		String[] types=SimpleQueriesProcessor.getArrayObjectsResourcesAsURIs(myTerm.getResource(), MetaLanguage.typeProperty, myTerm.getStatements(version));

		String result1="<div class=\"termHeader\">\n";
		String result2="";
		if(code!=null) result2="<div class=\"code\">["+code+"]</div><br/>";
	
		String result3="<div class=\"EntityName\">"+myTerm.getLabel(version)+"</div><br/>";
		String result4="<div class=\"uri\">"+myTerm.getURI()+"</div><br/>";
		//if comment
		String result5="";
		if(types!=null) {
			result5+="<div class=\"type\"><b>Type(s): </b> ";
			for(int i=0;i<types.length;i++) {
				// TODO maybe we should have a more ad-hoc label processor
				result5+=SimpleQueriesProcessor.getLabelorURIForURI(types[i],labelsModel)+" ";
			}
			result5+="</div><br/>";
		}
		String explString="";
		if(type==STYLE_TERM_SKOS_CONCEPT) explString="This a iso19135 register item that represent a SKOS concpet";
		String result6="<div class=\"comments\">"+explString+"</div><br/>";
		String result7="";
		if(comment!=null) result7="<div class=\"comments\">("+comment+")</div><br/>";
		
		String result8="<div class=\"formats\">Also available in\n";
		Iterator<String> standards=stdMap.keySet().iterator();
		while(standards.hasNext()) {
			String std=standards.next();
			result8+="<a href=\""+stdMap.get(std)+"\">"+std+"</a> ";
		}
		
		result8+="</div>";
		String result9="<div class=\"languages\">\n"+
		"<input type=\"button\" value=\"uk\" onclick=\"alert(\'Yet to be implemented!\')\" /> "+
		" <input type=\"button\" value=\"it\" onclick=\"alert(\'Yet to be implemented!\')\" /> "+
		"</div>"+
		"</div>";
		
		return result1+result2+result3+result4+result5+result6+result7+result8+result9;
	}
*/
	@Override
	public String getNavigationPanel(String version, String language) throws WebSystemException {
		
		String result1="<section id=\"second\">"+
		"<h2 id=\"definedInRegStr\">"+WebRendererStrings.getValueFor(WebRendererStrings.DEFINED_IN_REGISTER , language)+"</h2>";
		Iterator<TerminologySet> myUnsortedContainers;
		try {
			myUnsortedContainers = myTerm.getContainers(version).iterator();
		} catch (ModelException e) {
			SSLogger.log(e.getMessage(),SSLogger.DEBUG);
			e.printStackTrace();
			throw new WebSystemException("Cannot build navigation panel for "+myTerm.getURI());
		}
		SortedMap<String,TerminologySet> containersSorted=new TreeMap<String,TerminologySet>();
		int bogusCounter=1;
		while(myUnsortedContainers.hasNext()) {
			TerminologySet tempSet=myUnsortedContainers.next();
			String notation=tempSet.getNotation(version);
			if(notation!=null) 
				containersSorted.put(notation, tempSet);
			else {
				notation="";
				containersSorted.put("ZZZZZZ", tempSet);
				bogusCounter++;
			}
		}
		Iterator<TerminologySet>myContainers=containersSorted.values().iterator();
		String result2="<ul>\n";
		while(myContainers.hasNext()) {
			TerminologySet myCont=myContainers.next();
			result2+="<li><a href=\""+uri2UrlMap.get(myCont.getURI())+"\"  class=\"moreb\" >"+myCont.getLabel(myCont.getLastVersion(),language)+"</a></li>";
		}
		result2+="</ul><br/>\n";

		String result3="</section>";
				
		return result1+result2+result3;
		
		
	}
	
	@Override
	protected String getMetaLabel(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.META_INFO_LABEL_CODE, language);
	}

	
	@Override
	protected String getFocusOnConceptTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.FOCUS_CONCEPT_CODE , language);
	}

	@Override
	protected String getFocusOnCodeTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.FOCUS_CODE_CODE , language);
	}

	@Override
	protected String getStatementsTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.CODE_DESCRIPTION , language);
	}
	

	
	
	
	
	
}
