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

package net.metarelate.terminology.utils;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;

//TODO should be uniformed with label manager
/**
 * Implements heuristics for finding labels for entities
 * @author andrea_splendiani
 *
 */
public class AdvancedClassLabelExtractor {
	/**
	 * Returns a label for the default language. @see #getLabelFor(TerminologyEntity, String, LabelManager)
	 * @param e the enity
	 * @param version the version
	 * @param lm the label manager
	 * @return
	 */
	public static String getLabelFor(TerminologyEntity e, String version, LabelManager lm) {
		return getLabelFor(e,version,CoreConfig.DEFAULT_LANGUAGE,lm);
	}
	/**
	 * Returns a label for the specified version and language, but attempts default language and last version if nothing is found.
	 * As an ultimate instance, returns the local name of the entity URI.
	 * Note that labels are looked for in the entity definition only (labelManager is currently not used)
	 * @param e the entity
	 * @param version
	 * @param language
	 * @param lm the label manager
	 * @return
	 */
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
