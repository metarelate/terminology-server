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

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.TemplateManager;
import net.metarelate.terminology.utils.Loggers;
/**
 * Generates a language map links block
 * @author andreasplendiani
 *
 */
public class LangMapTemplate extends TemplateParametricClass implements TemplateFixedElement,
		TemplateGlobalElement, TemplateTermElement {

	public static final String langMapHeader = "$LangMap$";
	private TemplateManager tm=null;
	public LangMapTemplate(String substring, TemplateManager templateManager) {
		super(substring);
		this.tm=templateManager;
		Loggers.publishLogger.debug("New LangMapTemplate\n"+substring);
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyFactory factory, String tag) throws ModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String render(String tag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String render(TerminologyEntity e, String version, int level,String language, String baseURL, CacheManager cacheManager,LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL,String tag) throws ModelException {
		StringBuilder res=new StringBuilder();
		String stem="error";
		if(e.isSet()) stem=PublisherConfig.setStemString; //TODO use throughout the system
		else if(e.isIndividual()) stem=PublisherConfig.individualStemString;
		for(String lang:tm.getLanguages()) {					
			res.append("<a lang=\""+labelMap.get(lang)+"\" rel=\"alternate\" hreflang=\""+lang+"\" title=\""+labelMap.get(lang)+"\" href=\""+cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+"."+lang+".html"+"\">"+labelMap.get(lang)+"</a> ");
		}
		
		
		return res.toString();
	}
}
