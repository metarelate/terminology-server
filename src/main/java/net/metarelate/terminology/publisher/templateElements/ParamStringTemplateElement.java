package net.metarelate.terminology.publisher.templateElements;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;

public class ParamStringTemplateElement implements TemplateTermElement{
	public static final String strPlusHeader="$str+$";
	
	String rawString=null;
	public ParamStringTemplateElement(String str) {
		rawString=str;
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version,int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL) {
		String resultString=rawString;
		resultString=resultString.replaceAll("<<tmtLabel>>",e.getLabel(version, language));
		resultString=resultString.replaceAll("<<tmtVersion>>", version);
		String description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, language);
		if(description==null) description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, CoreConfig.DEFAULT_LANGUAGE);
		else description="";
		
		Set<Resource> typesSet=e.getGenericVersionSpecificURIObjects(MetaLanguage.typeProperty,version);
		String types="";
		for(Resource type:typesSet) {
			types+=" "+lm.getLabelForURI(type.getURI(), language, LabelManager.LANG_DEF_SHORTURI); // TODO needs better separator management
		}
		resultString=resultString.replaceAll("<<tmtDescription>>", description);
		//rawString.replaceAll("<<tmtAuthors>>", replacement);
		resultString=resultString.replaceAll("<<tmtTypes>>",types);
		
		return resultString;
	}



}
