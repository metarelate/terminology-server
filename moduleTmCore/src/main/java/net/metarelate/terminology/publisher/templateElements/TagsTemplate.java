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
import net.metarelate.terminology.utils.Loggers;

/**
 * Generates a list of tags
 * @author andreasplendiani
 *
 */
public class TagsTemplate extends TemplateParametricClass implements
		TemplateTermElement, TemplateGlobalElement, TemplateFixedElement {
	public static final String tagsHeader="$tags$";
	public TagsTemplate(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New TagsTemplate\n"+templateText);
	}

	public boolean isFixed() {
		return true;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(String tag) {
		// TODO Auto-generated method stub
		return null;
	}

	public String render(TerminologyFactory factory, String tag) throws ModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String render(TerminologyEntity e, String version, int level,
			String language, String baseURL, CacheManager cacheManager,
			LabelManager lm, BackgroundKnowledgeManager bkm,
			String registryBaseURL,
			String tag) throws ModelException {
		
		String tagsStr="";
		String[] tags=e.getTagsForVersion(version);
		if(tags.length>0) {
			
			for(int i=0;i<tags.length;i++) {
				tagsStr+=tags[i]+" ";
			}
		}
		
		return tagsStr;
	}

}
