package net.metarelate.terminology.publisher.templateElements;

import java.util.Collection;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.PublisherManager;
import net.metarelate.terminology.utils.Loggers;

public class BreadCrumbsTemplate extends TemplateParametricClass implements TemplateTermElement {
	public final static String bcrumbsHeader="$bcrumbs$";
	public BreadCrumbsTemplate(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New BreadCrumbsTemplate\n"+templateText);
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version, int level,String language, String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL,String tag) throws ModelException {
		StringBuilder result=new StringBuilder();
		result.insert(0,"<a href=\""+e.getURI()+"\">"+getBestLabel(e, version, language)+"</a> "+spacingStringValue);
		Collection<TerminologySet>fathers=e.getContainers(version);
		while(fathers.size()>0) {
			TerminologySet father=fathers.iterator().next(); //We assume only one iterator.
			String fatherStr=getBestLabel(father,father.getLastVersion(),language);
			String fatherURL=cacheManager.getValueFor(father.getURI(), PublisherConfig.uriHasUrl);
			if(father!=null) fatherURL="href=\""+fatherURL+"\"";
			else fatherURL="";
			result.insert(0,"<a "+fatherURL+">"+fatherStr+"</a> "+spacingStringValue);	
			
			fathers=father.getContainers(father.getLastVersion());
		}
		
		return " <a href=\""+registryBaseURL+"\">Home</a> &gt; "+result.toString()+" </p></nav>";
	}

		
	private String getBestLabel(TerminologyEntity e, String version, String language) {
		String current=e.getNotation(version);
		if(current==null || current=="") current=e.getLabel(version,language);
		if(current==null) current=e.getLabel(version,CoreConfig.DEFAULT_LANGUAGE);
		return current;
		
	}

	
		
		
		
			
	
	
	
	
}
