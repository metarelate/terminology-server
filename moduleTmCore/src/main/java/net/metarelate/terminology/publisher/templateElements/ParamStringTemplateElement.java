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
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.WebRendererStrings;
import net.metarelate.terminology.utils.AdvancedClassLabelExtractor;
import net.metarelate.terminology.utils.Loggers;

public class ParamStringTemplateElement extends TemplateParametricClass implements TemplateTermElement,TemplateGlobalElement{
	public static final String strPlusHeader="$str+$";
	
	
	public ParamStringTemplateElement(String str) {
		super(str);
		Loggers.publishLogger.debug("New ParamStringTemplateElement\n"+str);
		
	}

	public boolean isFixed() {
		return false;
	}

	public boolean isPerTerm() {
		return true;
	}

	public String render(TerminologyEntity e, String version,int level,String language,String baseURL, CacheManager cacheManager, LabelManager lm, BackgroundKnowledgeManager bkm,String registryBaseURL,String tag) throws ModelException {
		if(e.isVersioned() && (! printIfVersioned)) return "";
		if(!e.isVersioned() && (! printIfUnVersioned)) return "";
		String resultString=rawString;
		String label=e.getLabel(version, language);
		//if(label==null) label=e.getLabel(version, CoreConfig.DEFAULT_LANGUAGE);
		//if(label==null) label="No label for "+e.getURI();
		resultString=resultString.replace("<<tmtLabel>>",AdvancedClassLabelExtractor.getLabelFor(e, version, language, lm));
		resultString=resultString.replace("<<tmtVersion>>", version);
		String description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, language);
		if(description==null) description=e.getGenericVersionSpecificStringValueObjectByLanguage(MetaLanguage.commentProperty,version, CoreConfig.DEFAULT_LANGUAGE);
		if(description==null) description="";
		
		Set<Resource> typesSet=e.getGenericVersionSpecificURIObjects(MetaLanguage.typeProperty,version);
		String types="";
		for(Resource type:typesSet) {
			types+=" "+lm.getLabelForURI(type.getURI(), language, LabelManager.LANG_DEF_SHORTURI); // TODO needs better separator management
		}
		resultString=resultString.replace("<<tmtDescription>>", description);
		//rawString.replaceAll("<<tmtAuthors>>", replacement);
		resultString=resultString.replace("<<tmtTypes>>",types);
		resultString=resultString.replace("<<tmtURI>>",e.getURI());
		String code=e.getGenericVersionSpecificStringValueObject(MetaLanguage.notationProperty, version);
		if(code==null) code=e.getResource().getLocalName();
		resultString=resultString.replace("<<tmtCode>>",code);

		String stem="";
		if(e.isSet()) stem=PublisherConfig.setStemString;
		if(e.isIndividual()) stem=PublisherConfig.individualStemString;
		//TODO note below we have nulls for docs, but this is not an issue (as far as we don't use the links)
		resultString=resultString.replace("<<tmtRDFLink>>",cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+".rdf");
		resultString=resultString.replace("<<tmtTurtleLink>>",cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+stem+".ttl");
		
		String statusStr=null; 
		if(e.getStateURI(version)!=null) statusStr=lm.getLabelForURI(e.getStateURI(version),language,lm.LANG_DEF_URI);
		if(statusStr==null) statusStr=WebRendererStrings.getValueFor(WebRendererStrings.UNDEFINED, language);
		resultString=resultString.replace("<<tmtStatus>>",statusStr);
		
		String ownerStr="";
		if(e.getOwnerURI()!=null) ownerStr=lm.getLabelForURI(e.getOwnerURI(),language, lm.LANG_DEF_URI);
	
		resultString=resultString.replace("<<tmtOwner>>",ownerStr);
		
		
		resultString=resultString.replace("<<tmtLastUpdate>>", e.getLastUpdateDate());
		resultString=resultString.replace("<<tmtGenerationDate>>", e.getGenerationDate());
		Loggers.publishLogger.trace("Entity URI : "+e.getURI() );
		Loggers.publishLogger.trace("Version :"+version);
		resultString=resultString.replace("<<tmtActionDate>>", e.getActionDate(version));
		
		
		String versionStr=version;
		
		//TODO the dependency on webRendererString should at this point be eliminated (or at least re-defined).
		if(e.isLastVersion(version)) {
			versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.IS_LATEST_VERSION, language)+")";
		}
		else versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.HAS_LATEST_VERSION,language)+" <a href=\""+cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+e.getLastVersion()+"\">"+e.getLastVersion()+"</a>)";		
		resultString=resultString.replace("<<tmtNewerVersion>>", versionStr);
		
		Iterator<TerminologySet> containers= e.getContainers(version).iterator();
		String containerURL="";
		String containerLabel="";
		//TODO Note that the cache may not be prsent for -doc exports
		if(containers.hasNext()) { //TODO note that we expect only one container! This maybe should be made implicit by design, or this template should be changed.
			TerminologySet container=containers.next();
			containerURL=cacheManager.getValueFor(container.getURI(),PublisherConfig.uriHasUrl);
			containerLabel=container.getLabel(container.getLastVersion(),language);
			
		}
		
		if(containerURL!=null) resultString=resultString.replace("<<tmtFatherURL>>",containerURL );
		if(containerLabel!=null) resultString=resultString.replace("<<tmtFatherLabel>>", containerLabel);
		
		
		
		return resultString;
	}

	public String render(TerminologyFactory factory, String tag) throws ModelException {
		String resultString=rawString;
		resultString=resultString.replace("<<tmtTag>>", tag);
		resultString=resultString.replace("<<tmtOwner>>", "Author is not implemented");
		String roots="";
		for(TerminologySet s:factory.getRootCollections()) {
			if(s.getVersionsForTag(tag).length>0)
				roots+=s.getLabel(s.getVersionsForTag(tag)[0])+" ";
			else roots+=s.getLabel(s.getLastVersion())+" ";
		}
		resultString=resultString.replace("<<tmtRoots>>", roots);
		return resultString;
	}



}
