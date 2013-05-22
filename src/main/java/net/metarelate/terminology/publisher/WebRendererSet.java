/* 
 (C) British Crown Copyright 2011 - 2012, Met Office

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

package net.metarelate.terminology.publisher;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
//TODO maybe we should change of all Strings concatenations to StringBuilder for performance

public class WebRendererSet extends WebRendererItem {
	TerminologySet myCollection=null;
	public WebRendererSet(TerminologySet collection, String url) {
		super(collection);
		myCollection=collection;
		itemUrl=url;
		//labelsModel=labelRepository;
		entity=collection;
		determineType(myCollection.getLastVersion());
		SSLogger.log("created renderer for set "+collection.getURI()+" style :"+type);
		// DEBUG
		String[] myVersions=collection.getVersions();
		if(myVersions.length>1) {
			System.out.println("--DEBUG--");
			System.out.println("Collection "+collection.getURI());
			for(int tv=0;tv<myVersions.length;tv++) {
				System.out.println("Version "+myVersions[tv]);
				System.out.println("Model URI"+collection.getVersionURI(myVersions[tv]));
				System.out.println("Label "+collection.getLabel(myVersions[tv]));
				System.out.println("State "+collection.getStateURI(myVersions[tv]));
				System.out.println("Actor "+collection.getActionAuthorURI(myVersions[tv]));
				System.out.println("Action "+collection.getActionURI(myVersions[tv]));
				System.out.println("Date "+collection.getActionDate(myVersions[tv]));
				System.out.println("Description "+collection.getActionDescription(myVersions[tv]));
				Model tModel=collection.getStatements(myVersions[tv]);
				System.out.println("Model size: "+tModel.size());
			}
		}
		
	}

	protected  void determineType(String version) {
		if(entity.getStatements(version).contains(entity.getResource(),MetaLanguage.typeProperty,MetaLanguage.skosCollectionType)) {
			this.type=STYLE_SET_SKOS_COLLECTION;
		}
		else if (entity.getStatements(version).contains(entity.getResource(),MetaLanguage.typeProperty,MetaLanguage.skosSchemeType)) {
			this.type=STYLE_SET_SKOS_SCHEME;
		}
		
	}
	
	
	
	
	@Override
	public String getNavigationPanel(String version, String language) throws ModelException {
		
		Set<TerminologySet>myContainersSet=myCollection.getContainers(version);
		String result1="<section id=\"second\">";
		int bogusCounter=1;
		String result2="";
		if(myContainersSet.size()>0) {
			result1+="<h2 id=\"definedInRegStr\">"+WebRendererStrings.getValueFor(WebRendererStrings.DEFINED_IN_REGISTER , language)+"</h2>";
		
			Iterator<TerminologySet> myUnsortedContainers=myContainersSet.iterator();
			Comparator<String> codeComparator=new CodeComparator();
			SortedMap<String,TerminologySet> containersSorted=new TreeMap<String,TerminologySet>(codeComparator);
			bogusCounter=1;
			while(myUnsortedContainers.hasNext()) {
				TerminologySet tempSet=myUnsortedContainers.next();
				String notation=tempSet.getNotation(version);
				if(notation!=null) 
					containersSorted.put(notation, tempSet);
				else {
					notation="";
					containersSorted.put("ZZZZZZ", tempSet);
					bogusCounter++;
				}
			}
			Iterator<TerminologySet>myContainers=containersSorted.values().iterator();
		
			result2="<ul>\n";
			while(myContainers.hasNext()) {
				TerminologySet myCont=myContainers.next();
				result2+="<li><a href=\""+uri2UrlMap.get(myCont.getURI())+"\"  class=\"moreb\" >"+myCont.getLabel(myCont.getLastVersion(),language)+"</a></li>";
			}
			result2+="</ul>\n";
		}
		
		Set<TerminologySet> childrenSet=myCollection.getCollections(version);
		String result3="";
		if(childrenSet.size()>0) {
			result3="<h2>"+WebRendererStrings.getValueFor(WebRendererStrings.SUBREGISTERS,language)+"</h2>\n<ul>";
			Iterator<TerminologySet> myUnsortedCollIter=childrenSet.iterator();
			Comparator<String> codeComparator=new CodeComparator();
			SortedMap<String,TerminologySet> collSorted=new TreeMap<String,TerminologySet>(codeComparator);
			bogusCounter=1;
			while(myUnsortedCollIter.hasNext()) {
				TerminologySet tempChild=myUnsortedCollIter.next();
				String notation=tempChild.getNotation(version);
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
				TerminologySet myColl=myCollIter.next();
				String notation=myColl.getNotation(myColl.getLastVersion());
				if(notation!=null) notation="["+notation+"] ";
				else notation="";
				result3+="<li><a href=\""+uri2UrlMap.get(myColl.getURI())+"\" class=\"moreb\">"+notation+myColl.getLabel(myColl.getLastVersion(),language)+"</a></li>";
			}
			result3+="</ul>";
		}
		
		// Conatined codes
		Set<TerminologyIndividual> indSet=myCollection.getIndividuals(version);
		String result4="";	
		if(indSet.size()>0) {
			result4+="<h2>"+WebRendererStrings.getValueFor(WebRendererStrings.CODES_IN_REGISTER , language)+"</h2>\n<ul>";
			Iterator<TerminologyIndividual> myIndUnsortedIter=indSet.iterator();
			Comparator<String> codeComparator=new CodeComparator();
			SortedMap<String,TerminologyIndividual> individualsSorted=new TreeMap<String,TerminologyIndividual>(codeComparator);
			bogusCounter=1;
			while(myIndUnsortedIter.hasNext()) {
				TerminologyIndividual tempInd=myIndUnsortedIter.next();
				String notation=tempInd.getNotation(tempInd.getLastVersion());
				if(notation!=null) individualsSorted.put(notation, tempInd);
				else {
					notation="";
					individualsSorted.put("ZZZZZ"+bogusCounter, tempInd);
					bogusCounter++;
				}
				
			}
			Iterator<TerminologyIndividual>myIndIter=individualsSorted.values().iterator();

			while(myIndIter.hasNext()) {
				TerminologyIndividual myInd=myIndIter.next();
				String notation=myInd.getNotation(myInd.getLastVersion());
				if(notation!=null) notation="["+notation+"] ";
				else notation="";
				result4+="<li><a href=\""+uri2UrlMap.get(myInd.getURI())+"\" class=\"moreb\">"+notation+myInd.getLabel(myInd.getLastVersion(),language)+"</a></li>";
			}
			result4+="</ul>";
		}
		
		String result5="</section>";
		
		return result1+result2+result3+result4+result5;
	}

	@Override
	protected String getMetaLabel(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.META_INFO_LABEL_REGISTER, language);
	}

	@Override
	protected String getFocusOnConceptTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.FOCUS_CONCEPT_REGISTRY , language);
	}

	@Override
	protected String getFocusOnCodeTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.FOCUS_CODE_REGISTRY , language);
	}

	@Override
	protected String getStatementsTitle(String language) {
		return WebRendererStrings.getValueFor(WebRendererStrings.REGISTER_DESCRIPTION , language);
	}


	
	
	
	

	
	
	
	
}
