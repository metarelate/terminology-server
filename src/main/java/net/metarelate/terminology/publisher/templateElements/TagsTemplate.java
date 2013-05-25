package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.exceptions.ModelException;

public class TagsTemplate extends TemplateParametricClass implements
		TemplateTermElement, TemplateGlobalElement, TemplateFixedElement {
	public static final String tagsHeader="$tags$";
	public TagsTemplate(String templateText) {
		super(templateText);
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

	public String render(TerminologyFactory factory) throws ModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String render(TerminologyEntity e, String version, int level,
			String language, String baseURL, CacheManager cacheManager,
			LabelManager lm, BackgroundKnowledgeManager bkm,
			String registryBaseURL) throws ModelException {
		
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
