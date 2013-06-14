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
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.Loggers;
/**
 * Lists codes contained in a register
 * @author andrea_splendiani
 *
 */
public class ContainedCodesTemplate extends TemplateParametricClass implements
		TemplateTermElement {
	public static final String ccodeHeader="$codes$";
	public ContainedCodesTemplate(String templateText) {
		super(templateText);
		Loggers.publishLogger.debug("New ContainedCodesTemplate\n"+templateText);
		// TODO Auto-generated constructor stub
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
		Set<TerminologyIndividual> childrenSet=((TerminologySet)e).getIndividuals(version);
		Iterator<TerminologyIndividual> myUnsortedIndIter=childrenSet.iterator();
		Comparator<String> codeComparator=new CodeComparator();
		SortedMap<String,TerminologyIndividual> indSorted=new TreeMap<String,TerminologyIndividual>(codeComparator);
		int bogusCounter=1;
		while(myUnsortedIndIter.hasNext()) {
			TerminologyIndividual tempChild=myUnsortedIndIter.next();
			String notation=tempChild.getNotation(tempChild.getLastVersion());
			if(notation!=null) 
				indSorted.put(notation, tempChild);
			else {
				notation="";
				indSorted.put("ZZZZZZ"+bogusCounter, tempChild);
				bogusCounter++;
			}
		}
		Iterator<TerminologyIndividual> myIndIter=indSorted.values().iterator();
		while(myIndIter.hasNext()) {
			String currentCodeLine=rawString;
			TerminologyIndividual myInd=myIndIter.next();
			String notation=myInd.getNotation(myInd.getLastVersion());
			if(notation==null) notation="-";
			String label=myInd.getLabel(myInd.getLastVersion(),language);
			if(label==null) label=myInd.getLabel(myInd.getLastVersion());	//TODO label behaviour should be put in a LabelCompuatationObject (LabelManager ?)
			if(label==null) label="";
			currentCodeLine=currentCodeLine.replace("<<codeURL>>",cacheManager.getValueFor(myInd.getURI(), PublisherConfig.uriHasUrl));
			currentCodeLine=currentCodeLine.replace("<<tmtCodeNotation>>",notation);	
			currentCodeLine=currentCodeLine.replace("<<tmtCodeLabel>>",label);	
			result.append(currentCodeLine);
				
				
		}
			

		return result.toString();
	}
	

}
