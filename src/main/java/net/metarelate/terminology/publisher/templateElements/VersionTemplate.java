package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;

public class VersionTemplate extends TemplateParametricClass implements
		TemplateTermElement {
	public static final String versionHeade="$version$";
	public VersionTemplate(String templateText) {
		super(templateText);
		// TODO Auto-generated constructor stub
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
			String registryBaseURL) throws ModelException {
		StringBuilder result=new StringBuilder();
		String currentVersion=e.getLastVersion();
		while(currentVersion!=null) {
			String currentVersionBlock=rawString;
			
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersionLink>>", cacheManager.getValueFor(e.getURI(), PublisherConfig.uriHasUrl)+"/"+currentVersion);
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersion>>", currentVersion);
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersionDate>>", e.getActionDate(currentVersion));
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersionAction>>", lm.getLabelForURI(e.getActionURI(currentVersion), language, lm.LANG_DEF_URI));
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersionActionAuthor>>", lm.getLabelForURI(e.getActionAuthorURI(currentVersion),language, lm.LANG_DEF_URI));
			currentVersionBlock=currentVersionBlock.replaceAll("<<tmtVersionActionDescription>>", e.getActionDescription(currentVersion));
			
			result.append(currentVersionBlock);
			currentVersion=(e.getPreviousVersion(currentVersion));
		}
		
		return result.toString();
	}

}
