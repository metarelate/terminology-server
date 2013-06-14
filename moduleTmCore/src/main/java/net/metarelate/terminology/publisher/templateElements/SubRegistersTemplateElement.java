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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.Loggers;
/**
 * Generates a list of sub-registers
 * @author andreasplendiani
 *
 */
public class SubRegistersTemplateElement extends TemplateParametricClass
		implements TemplateTermElement {
	public static final String subRegHeader="$subreg$";
	public SubRegistersTemplateElement(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New SubRegistersTemplateElement\n"+templateText);
	}

	public boolean isFixed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isPerTerm() {
		// TODO Auto-generated method stub
		return false;
	}

	public String render(TerminologyEntity e, String version, int level,
			String language, String baseURL, CacheManager cacheManager,
			LabelManager lm, BackgroundKnowledgeManager bkm,
			String registryBaseURL,
			String tag) throws ModelException {
		StringBuilder result=new StringBuilder();
		if(!e.isSet()) return "DEBUG:UNDEF";
		Set<TerminologySet> childrenSet=((TerminologySet)e).getCollections(version);
		Iterator<TerminologySet> myUnsortedCollIter=childrenSet.iterator();
		Comparator<String> codeComparator=new CodeComparator();
		SortedMap<String,TerminologySet> collSorted=new TreeMap<String,TerminologySet>(codeComparator);
		int bogusCounter=1;
		while(myUnsortedCollIter.hasNext()) {
			TerminologySet tempChild=myUnsortedCollIter.next();
			String notation=tempChild.getNotation(tempChild.getLastVersion());
			if(notation!=null) 
				collSorted.put(notation, tempChild);
			else {
				notation="";
				collSorted.put("ZZZZZZ"+bogusCounter, tempChild);
				bogusCounter++;
			}
		}
		Iterator<TerminologySet>myCollIter=collSorted.values().iterator();
		while(myCollIter.hasNext()) {
			String currentSubRegLine=rawString;
			TerminologySet myColl=myCollIter.next();
			String notation=myColl.getNotation(myColl.getLastVersion());
			if(notation==null) notation="-";
			String label=myColl.getLabel(myColl.getLastVersion(),language);
			if(label==null) label=myColl.getLabel(myColl.getLastVersion());	//TODO label behaviour should be put in a LabelCompuatationObject (LabelManager ?)
			if(label==null) label="";
			currentSubRegLine=currentSubRegLine.replace("<<subRegURL>>",cacheManager.getValueFor(myColl.getURI(), PublisherConfig.uriHasUrl));
			currentSubRegLine=currentSubRegLine.replace("<<tmtSubRegNotation>>",notation);	
			currentSubRegLine=currentSubRegLine.replace("<<tmtSubRegLabel>>",label);	
			result.append(currentSubRegLine);
					
				
		}
			

		return result.toString();
	}

}
