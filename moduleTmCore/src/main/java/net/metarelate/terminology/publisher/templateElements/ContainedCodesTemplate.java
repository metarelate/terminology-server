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
