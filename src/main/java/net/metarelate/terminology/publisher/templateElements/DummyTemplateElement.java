package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.exceptions.ModelException;

public class DummyTemplateElement implements TemplateTermElement,TemplateGlobalElement {
	private String stringToRender=null;
	public DummyTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		System.out.println("Dummy block: "+stringToRender); //TODO test
	}
	
	public String render(TerminologyEntity e, String version, int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL) {
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