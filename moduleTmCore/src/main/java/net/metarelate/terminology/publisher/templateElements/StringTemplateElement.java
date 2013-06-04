package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.utils.Loggers;

public class StringTemplateElement implements TemplateFixedElement, TemplateTermElement, TemplateGlobalElement {
	private String stringToRender=null;
	public StringTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		Loggers.publishLogger.debug("New StatementsTemplateElement\n"+stringToRender);
	}
	
	
	public String render(String tag) {
		return stringToRender;
	}

	public boolean isFixed() {
		return true;
	}

	public boolean isPerTerm() {
		return false;
	}

	public String render(TerminologyEntity e, String version, int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL,String tag) {
		return stringToRender;
	}


	public String render(TerminologyFactory factory, String tag) {
		return stringToRender;
	}


	
}