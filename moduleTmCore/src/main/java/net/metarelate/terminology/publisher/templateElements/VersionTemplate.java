package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.utils.Loggers;

public class VersionTemplate extends TemplateParametricClass implements
		TemplateTermElement {
	public static final String versionHeader="$version$";
	public VersionTemplate(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New VersionTemplate\n"+versionHeader);
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
		StringBuilder result=new StringBuilder();
		String currentVersion=e.getLastVersion();
		while(currentVersion!=null) {
			String currentVersionBlock=rawString;
			
			String description=e.getActionDescription(currentVersion);
			if(description==null) description=""; // TODO this should be the default design of getActionDescription()
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersionLink>>", cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+currentVersion);
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersion>>", currentVersion);
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersionDate>>", e.getActionDate(currentVersion));
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersionAction>>", lm.getLabelForURI(e.getActionURI(currentVersion), language, lm.LANG_DEF_URI));
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersionActionAuthor>>", lm.getLabelForURI(e.getActionAuthorURI(currentVersion),language, lm.LANG_DEF_URI));
			currentVersionBlock=currentVersionBlock.replace("<<tmtVersionActionDescription>>", description);
			
			result.append(currentVersionBlock);
			currentVersion=(e.getPreviousVersion(currentVersion));
		}
		
		return result.toString();
	}

}
