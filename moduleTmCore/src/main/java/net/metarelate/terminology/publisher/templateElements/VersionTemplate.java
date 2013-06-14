/* 
 (C) British Crown Copyright 2011 - 2013, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.utils.Loggers;

/**
 * Generates a version history block
 * @author andreasplendiani
 *
 */
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
