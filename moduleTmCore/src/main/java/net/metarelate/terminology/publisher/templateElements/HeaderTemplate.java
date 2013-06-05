package net.metarelate.terminology.publisher.templateElements;

import java.util.HashMap;
import java.util.Map;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.utils.Loggers;

public class HeaderTemplate implements TemplateTermElement{
	public static final String headerHeader="$onLevel$";
	public static final String paramDelimiter="||";
	public static final String paramEqualSign="::";
	String templateString=null;
	Map<String,String> replacementMap=new HashMap<String,String>();
	
	public HeaderTemplate(String stringToParse) {
		super();
		Loggers.publishLogger.debug("New HeaderTemplate: "+stringToParse);
		while(stringToParse.contains(paramDelimiter)) {
			String param=stringToParse.substring(0, stringToParse.indexOf(paramDelimiter));
			stringToParse=stringToParse.substring(stringToParse.indexOf(paramDelimiter)+paramDelimiter.length());
			String[] values=param.split(paramEqualSign);
			replacementMap.put(values[0], values[1]);
			Loggers.publishLogger.trace("Mapping levels: "+values[0]+" to "+values[1]);
		}
		templateString=stringToParse;
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version, int level,
			String language, String baseURL, CacheManager cacheManager,
			LabelManager lm, BackgroundKnowledgeManager bkm,
			String registryBaseURL,
			String tag) throws ModelException {
		Loggers.publishLogger.trace("Header template render called for level "+level);
		String key=new Integer(level).toString();
		String rep=replacementMap.get(key);
		Loggers.publishLogger.trace("Replacement planned: "+rep);
		return templateString.replace("<<tmtOnLevel>>", rep);
	}

}
