package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;

public interface  TemplateTermElement extends TemplateElement {

	public abstract String render(TerminologyEntity e, String version, int level, String language, String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm, String registryBaseURL) throws ModelException;



}
