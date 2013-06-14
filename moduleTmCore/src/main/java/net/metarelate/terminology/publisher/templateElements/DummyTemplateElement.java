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

class DummyTemplateElement implements TemplateTermElement,TemplateGlobalElement {
	private String stringToRender=null;
	public DummyTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		Loggers.publishLogger.debug("New DummyTemplateElement\n"+stringToRender);
	}
	
	public String render(TerminologyEntity e, String version, int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL,String tag) {
		return "Dummy element for entity "+e.getURI()+" , should expand accoring to "+stringToRender;
	}
	public boolean isFixed() {
		return false;
	}
	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyFactory factory, String tag) throws ModelException {
		return "Dummy element for factory with  "+factory.getAllSets().size()+" sets, "+stringToRender;
	}

}