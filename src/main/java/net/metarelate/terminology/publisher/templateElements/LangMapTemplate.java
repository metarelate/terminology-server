package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.TemplateManager;

public class LangMapTemplate extends TemplateParametricClass implements TemplateFixedElement,
		TemplateGlobalElement, TemplateTermElement {

	public static final String langMapHeader = "$LangMap$";
	private TemplateManager tm=null;
	public LangMapTemplate(String substring, TemplateManager templateManager) {
		super(substring);
		this.tm=templateManager;
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
	
	public String render(TerminologyEntity e, String version, int level,String language, String baseURL, CacheManager cacheManager,LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL) throws ModelException {
		StringBuilder res=new StringBuilder();
		String stem="error";
		if(e.isSet()) stem=PublisherConfig.registerStemString; //TODO use throughout the system
		else if(e.isIndividual()) stem=PublisherConfig.codeStemString;
		for(String lang:tm.getLanguages()) {					
			res.append("<a lang=\""+labelMap.get(lang)+"\" rel=\"alternate\" hreflang=\""+lang+"\" title=\""+labelMap.get(lang)+"\" href=\""+cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+"."+lang+".html"+"\">"+labelMap.get(lang)+"</a> ");
		}
		
		
		return res.toString();
	}
}
