package net.metarelate.terminology.publisher.templateElements;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Resource;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.WebRendererStrings;
import net.metarelate.terminology.utils.SSLogger;

public class ParamStringTemplateElement extends TemplateParametricClass implements TemplateTermElement{
	public static final String strPlusHeader="$str+$";
	
	
	public ParamStringTemplateElement(String str) {
		super(str);
		
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version,int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL) throws ModelException {
		if(e.isVersioned() && (! printIfVersioned)) return "";
		if(!e.isVersioned() && (! printIfUnVersioned)) return "";
		String resultString=rawString;
		resultString=resultString.replaceAll("<<tmtLabel>>",e.getLabel(version, language));
		resultString=resultString.replaceAll("<<tmtVersion>>", version);
		String description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, language);
		if(description==null) description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, CoreConfig.DEFAULT_LANGUAGE);
		if(description==null) description="";
		
		Set<Resource> typesSet=e.getGenericVersionSpecificURIObjects(MetaLanguage.typeProperty,version);
		String types="";
		for(Resource type:typesSet) {
			types+=" "+lm.getLabelForURI(type.getURI(), language, LabelManager.LANG_DEF_SHORTURI); // TODO needs better separator management
		}
		resultString=resultString.replaceAll("<<tmtDescription>>", description);
		//rawString.replaceAll("<<tmtAuthors>>", replacement);
		resultString=resultString.replaceAll("<<tmtTypes>>",types);
		resultString=resultString.replaceAll("<<tmtURI>>",e.getURI());
		String code=e.getGenericVersionSpecificStringValueObject(MetaLanguage.notationProperty, version);
		if(code==null) code=e.getResource().getLocalName();
		resultString=resultString.replaceAll("<<tmtCode>>",code);

		String stem="";
		if(e.isSet()) stem=PublisherConfig.registerStemString;
		if(e.isIndividual()) stem=PublisherConfig.codeStemString;
		resultString=resultString.replaceAll("<<tmtRDFLink>>",cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+".rdf");
		resultString=resultString.replaceAll("<<tmtTurtleLink>>",cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+".ttl");
		
		String statusStr=null; 
		if(e.getStateURI(version)!=null) statusStr=lm.getLabelForURI(e.getStateURI(version),language,lm.LANG_DEF_URI);
		if(statusStr==null) statusStr=WebRendererStrings.getValueFor(WebRendererStrings.UNDEFINED, language);
		resultString=resultString.replaceAll("<<tmtStatus>>",statusStr);
		
		String ownerStr="";
		if(e.getOwnerURI()!=null) ownerStr=lm.getLabelForURI(e.getOwnerURI(),language, lm.LANG_DEF_URI);
	
		resultString=resultString.replaceAll("<<tmtOwner>>",ownerStr);
		
		
		resultString=resultString.replaceAll("<<tmtLastUpdate>>", e.getLastUpdateDate());
		resultString=resultString.replaceAll("<<tmtGenerationDate>>", e.getGenerationDate());
		resultString=resultString.replaceAll("<<tmtActionDate>>", e.getActionDate(version));
		
		
		String versionStr=version;
		
		//TODO the dependency on webRendererString should at this point be eliminated (or at least re-defined).
		if(e.isLastVersion(version)) {
			versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.IS_LATEST_VERSION, language)+")";
		}
		else versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.HAS_LATEST_VERSION,language)+" <a href=\""+cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+e.getLastVersion()+"\">"+e.getLastVersion()+"</a>)";		
		resultString=resultString.replaceAll("<<tmtNewerVersion>>", versionStr);
		
		Iterator<TerminologySet> containers= e.getContainers(version).iterator();
		String containerURL="";
		String containerLabel="";
		if(containers.hasNext()) { //TODO note that we expect only one container! This maybe should be made implicit by design, or this template should be changed.
			TerminologySet container=containers.next();
			containerURL=cacheManager.getValueFor(container.getURI(),PublisherConfig.uriHasUrl);
			containerLabel=container.getLabel(container.getLastVersion(),language);
			
		}
		
		resultString=resultString.replaceAll("<<tmtFatherURI>>",containerURL );
		resultString=resultString.replaceAll("<<tmtFatherLabel>>", containerLabel);
		
		
		
		return resultString;
	}



}
