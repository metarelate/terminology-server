package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;

public class StringTemplateElement implements TemplateFixedElement, TemplateTermElement, TemplateGlobalElement {
	private String stringToRender=null;
	public StringTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		System.out.println("String block: "+stringToRender); //TODO test
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

	public String render(TerminologyEntity e, String version, int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm) {
		return stringToRender;
	}


	public String render(TerminologyFactory factory) {
		return stringToRender;
	}


	
}