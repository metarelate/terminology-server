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

package net.metarelate.terminology.publisher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.coreModel.BackgroundKnowledgeManager;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.publisher.templateElements.BreadCrumbsTemplate;
import net.metarelate.terminology.publisher.templateElements.ContainedCodesTemplate;
import net.metarelate.terminology.publisher.templateElements.DummyTemplateElement;
import net.metarelate.terminology.publisher.templateElements.LangMapTemplate;
import net.metarelate.terminology.publisher.templateElements.ParamStringTemplateElement;
import net.metarelate.terminology.publisher.templateElements.SetCodeValuesTemplate;
import net.metarelate.terminology.publisher.templateElements.StatementsTemplateElement;
import net.metarelate.terminology.publisher.templateElements.StringTemplateElement;
import net.metarelate.terminology.publisher.templateElements.SubRegistersTemplateElement;
import net.metarelate.terminology.publisher.templateElements.TagsTemplate;
import net.metarelate.terminology.publisher.templateElements.TemplateElement;
import net.metarelate.terminology.publisher.templateElements.TemplateTermElement;
import net.metarelate.terminology.publisher.templateElements.TemplateFixedElement;
import net.metarelate.terminology.publisher.templateElements.TemplateGlobalElement;
import net.metarelate.terminology.publisher.templateElements.VersionTemplate;

import net.metarelate.terminology.utils.Loggers;

public class TemplateManager {
	private final String openTag="<!-- tmtOpen>";
	private final String closeTag="<tmtClose -->";
	private final String strPlusHeader="$str+$";
	private final String strBreadcrumbsHeader="$bcrumbs$";
	
	
	private File templateFile=null;
	Map<String,ArrayList<TemplateElement>> indTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> setTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> preTemplates=new Hashtable<String,ArrayList<TemplateElement>>();
	Map<String,ArrayList<TemplateElement>> postTemplates=new Hashtable<String,ArrayList<TemplateElement>>();

	public TemplateManager(String templateFileDir) throws ConfigurationException, IOException {
		super();
		Loggers.publishLogger.info("Loading templates in "+templateFileDir);
		templateFile=new File(templateFileDir);
		if(!templateFile.isDirectory()) throw new ConfigurationException("No template at "+templateFileDir);
		File[] templateFiles=templateFile.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String fName) {
				if(fName.endsWith(".tmt")) return true;
				else return false;
			}});
		for(File templateFile:templateFiles) {
			//TODO we should constrain to known languge strings
			String tfName=templateFile.getName();
			Loggers.publishLogger.trace("template: "+tfName);
			String[] tfNameBits=tfName.split("\\.");
			if(tfNameBits.length!=3) throw new ConfigurationException("Invalid template file name for "+tfName);
			if(tfNameBits[0].equalsIgnoreCase("code")) {
				indTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("set")) {
				setTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("pre")) {
				preTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			else if(tfNameBits[0].equalsIgnoreCase("post")) {
				postTemplates.put(tfNameBits[1], parseTemplate(templateFile));
			}
			
		}
	}
	
	public String getPageForLang(String language, TerminologySet set, String version, int level,String baseURL,CacheManager cacheManager,LabelManager lm, BackgroundKnowledgeManager bkm, String registryBaseURL) throws ConfigurationException, ModelException {
		return expandTermTemplate(setTemplates,language,set,version, level,baseURL,cacheManager,lm,bkm,registryBaseURL);
	}
	public String getPageForLang(String language, TerminologyIndividual ind, String version, int level,String baseURL,CacheManager cacheManager,LabelManager lm, BackgroundKnowledgeManager bkm, String registryBaseURL) throws ConfigurationException, ModelException {
		return expandTermTemplate(indTemplates,language,ind,version,level,baseURL,cacheManager,lm,bkm,registryBaseURL); 	// TODO we don't care about levels here
	}
	public String getIntroForLang(String language,String tag,TerminologyFactory tf) throws ConfigurationException, ModelException {
		return expandFixedTemplate(preTemplates,language,tag,tf);
	}
	public String getClosingForLang(String language,String tag,TerminologyFactory tf) throws ConfigurationException, ModelException {
		return expandFixedTemplate(postTemplates,language,tag,tf);
	}
	
	


	private String expandTermTemplate(Map<String,ArrayList<TemplateElement>> templateMap, String language, TerminologyEntity entity, String version, int level,String baseURL,CacheManager cacheManager,LabelManager lm, BackgroundKnowledgeManager bkm, String registryBaseURL) throws ConfigurationException, ModelException {
		if(templateMap.get(language)==null) {
			language=CoreConfig.DEFAULT_LANGUAGE;
			if(templateMap.get(language)==null) throw new ConfigurationException("No suitable template defined for "+entity.getURI());
		}
		StringBuilder answer=new StringBuilder();
		for(TemplateElement t:templateMap.get(language)) answer.append(((TemplateTermElement)t).render(entity, version, level,language,baseURL,cacheManager,lm,bkm,registryBaseURL));
		return answer.toString();
	}
	
	private String expandFixedTemplate(Map<String,ArrayList<TemplateElement>> templateMap, String language, String tag, TerminologyFactory tf) throws ConfigurationException, ModelException {
		Loggers.publishLogger.debug("Fixed template expansion for language: "+language);
		if(templateMap.get(language)==null) {
			language=CoreConfig.DEFAULT_LANGUAGE;
			if(templateMap.get(language)==null) throw new ConfigurationException("No suitable template defined for pre or post block");
		}
		StringBuilder answer=new StringBuilder();
		for(TemplateElement t:templateMap.get(language)) answer.append(((TemplateGlobalElement)t).render(tf,tag));
		return answer.toString();
	}
	
	private ArrayList<TemplateElement> parseTemplate(File templateFile) throws IOException {
		ArrayList<TemplateElement> bits=new ArrayList<TemplateElement>();
		String templateString=readFileAsString(templateFile);
		int runningIndex=0;
		Loggers.publishLogger.trace(templateString); //TODO test
		while(runningIndex<templateString.length()) {
			Loggers.publishLogger.trace("Running index: "+runningIndex);
			int firstBit=templateString.indexOf(openTag,runningIndex);
			int secondBit=templateString.indexOf(closeTag,runningIndex);
			Loggers.publishLogger.trace("First bit: "+firstBit); //TODO test
			Loggers.publishLogger.trace("Second bit: "+secondBit); //TODO test
			if(firstBit<0) {
				bits.add(new StringTemplateElement(templateString.substring(runningIndex)));
				runningIndex=templateString.length()+1;
				break;
			}
			if(firstBit>runningIndex) bits.add(new StringTemplateElement(templateString.substring(runningIndex,firstBit)));
			//TODO here we should parse the real block
			if(firstBit>0 && secondBit>0) bits.add(parseElement(templateString.substring(firstBit+openTag.length(),secondBit)));
			runningIndex=secondBit+openTag.length();
			
		}
		return bits;
	}
	
	private String readFileAsString(File fileToRead) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (fileToRead));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");
	    
	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    reader.close();
	    return stringBuilder.toString();
	}

	public String[] getLanguages() {
		return indTemplates.keySet().toArray(new String [0]);
	}
	
	private TemplateElement parseElement(String elemString) {
		if(elemString.startsWith(ParamStringTemplateElement.strPlusHeader)) return new ParamStringTemplateElement(elemString.substring(ParamStringTemplateElement.strPlusHeader.length()));
		else if (elemString.startsWith(BreadCrumbsTemplate.bcrumbsHeader)) return new BreadCrumbsTemplate(elemString.substring(BreadCrumbsTemplate.bcrumbsHeader.length()));
		else if (elemString.startsWith(LangMapTemplate.langMapHeader)) return new LangMapTemplate(elemString.substring(LangMapTemplate.langMapHeader.length()),this);
		else if (elemString.startsWith(TagsTemplate.tagsHeader)) return new TagsTemplate(elemString.substring(TagsTemplate.tagsHeader.length()));
		else if (elemString.startsWith(VersionTemplate.versionHeade)) return new VersionTemplate(elemString.substring(VersionTemplate.versionHeade.length()));
		else if (elemString.startsWith(StatementsTemplateElement.statHeader)) return new StatementsTemplateElement(elemString.substring(StatementsTemplateElement.statHeader.length()));
		else if (elemString.startsWith(SubRegistersTemplateElement.subRegHeader)) return new SubRegistersTemplateElement(elemString.substring(SubRegistersTemplateElement.subRegHeader.length()));
		else if (elemString.startsWith(ContainedCodesTemplate.ccodeHeader )) return new ContainedCodesTemplate(elemString.substring(ContainedCodesTemplate.ccodeHeader.length()));
		else if (elemString.startsWith(SetCodeValuesTemplate.setCodeValHeader )) return new SetCodeValuesTemplate(elemString.substring(SetCodeValuesTemplate.setCodeValHeader.length()));

		else return new StringTemplateElement(elemString);
	}
	

}
