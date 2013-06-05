package net.metarelate.terminology.utils;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;

public class AdvancedClassLabelExtractor {
	public static String getLabelFor(TerminologyEntity e, String version, LabelManager lm) {
		return getLabelFor(e,version,CoreConfig.DEFAULT_LANGUAGE,lm);
	}
	public static String getLabelFor(TerminologyEntity e, String version, String language,LabelManager lm) {
		//First we check if the seeked version is there.
		String result=e.getLabel(version, language);
		if(result!=null) {
			Loggers.coreLogger.trace("Found label for "+e.getURI()+" for precise version and language");
			return result;
		}
		result=e.getLabel(version, e.getLastVersion());
		if(result!=null) {
			Loggers.coreLogger.trace("Found label for "+e.getURI()+" for last version and language");
			return result;
		}
		//Just in case this was not the default language, we 
		if(!language.equals(CoreConfig.DEFAULT_LANGUAGE)) {
			result=e.getLabel(version, CoreConfig.DEFAULT_LANGUAGE);
			if(result!=null) {
				Loggers.coreLogger.trace("Found label for "+e.getURI()+" for precise version and default language");
				return result;
			}
			result=e.getLabel(e.getLastVersion(), CoreConfig.DEFAULT_LANGUAGE);
			if(result!=null) {
				Loggers.coreLogger.trace("Found label for "+e.getURI()+" for last version and default language");
				return result;
			}
		}
		
		// TODO going for URIs now, we could look into the label manager or in other properties
		
		return e.getResource().getLocalName();
	}
	
}
