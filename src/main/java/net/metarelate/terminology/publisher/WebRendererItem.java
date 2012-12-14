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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.utils.CodeComparator;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

public abstract class WebRendererItem {
	protected static final int STYLE_SET_SKOS_COLLECTION=1;
	protected static final int STYLE_TERM_SKOS_COLLECTION=2;
	protected static final int STYLE_SET_SKOS_SCHEME=3;
	protected static final int STYLE_TERM_SKOS_SCHEME=4;
	protected static final int STYLE_TERM_SKOS_CONCEPT=5;
	protected int type=0;
	protected String itemUrl=null;
	protected Model labelsModel=null;
	protected TerminologyEntity entity=null;
	protected LabelManager myLabelManager=null;
	protected Hashtable<String, String> uri2UrlMap=new Hashtable<String,String>();
	
	protected abstract void determineType(String version) ;
	
	public WebRendererItem(TerminologyEntity entity) {
		this.entity=entity;
		this.myLabelManager=entity.getFactory().getLabelManager();
	}
	
	public String getHtmlRepresentation(String version, boolean versionPage, String language, SortedMap<String,String> dataLangMap, SortedMap<String,String> langMap, String baseURL) {
		String result1=getVersionOpening(version,versionPage,language);
		String result2=getBredCrumbs(version,language,baseURL);
		String result3=	"<div id=\"content\">\n" +
						"<article>\n";
		String result4=getHGroup( version,  language);
		String result5="<section id=\"main\">";
		String result6=getHeadBlock(language,version,dataLangMap);
		String result7=getMetaTable( version,  language, versionPage);
		String result71=getStatementsBlock(MetaLanguage.filterForWeb(entity.getStatements(version)),language);
		String result75="";
		if(entity.isVersioned()) {
			result75=getVersionBlock(version,language,baseURL);
		}
		//String result8="</section><section id=\"second\">";
		String result8="</section>";
		String result9=getNavigationPanel(version,language) ;// Statements
		
		
		
		//String result11=""; // Navigation
		//String result2=getVersionHeader(dataLangMap,version);
		//String result3=getStatusVersionedBlock(version);
		
		
		//String result6=
		//String result7=getNavigationPanel(version, uri2Url));
		String result11=getClosing(language,langMap);
		
				
		String resultLast="";
		return result1+result2+result3+result4+result5+result6+result7+result71+result75+result8+result9+result11;		
	}
	
	// TODO Description and Authors meta-data to be implemented
	public String getVersionOpening(String version,boolean showVersion, String language) {
		String titleStr="";
		String label=entity.getLabel(version,language);
		if(label==null) label=entity.getLabel(version,CoreConfig.DEFAULT_LANGUAGE);
		
		if(entity.isVersioned()) titleStr=label + " v. "+version;
		else titleStr=entity.getLabel(version);
		String descriptionStr="";
		String authorStr="";
		
		//////
		String result1="<!DOCTYPE html><html lang=\"en\"><head>\n" +
				"<title>"+titleStr+"</title>\n" +
				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
				"<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0\" />\n" +
				"<meta name=\"robots\" content=\"nofollow\" />\n"+
				"<link rel=\"shortcut icon\" href=\"http://www.metoffice.gov.uk/favicon.ico\" />\n" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://reference.metoffice.gov.uk/data/wmo/web/css/normalize.css\" />\n" +
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://reference.metoffice.gov.uk/data/wmo/web/css/default.css\" />\n" +
				"<!--[if lt IE 9]>\n" +
				"<script src=\"//html5shiv.googlecode.com/svn/trunk/html5.js\"></script>\n" +
				"<![endif]-->\n" +
				//"<meta name=\"description\" content=\""+descriptionStr+"\" />\n" +
				//"<meta name=\"author\" content=\""+authorStr+"\" />\n" +
				"<!-- Replace favicon.ico & apple-touch-icon.png in the root of your domain and delete these references -->\n" +
				"<style type=\"text/css\"></style>\n" +
				"</head>\n" +
				"<body>\n" +
				"<div id=\"sizer\">\n" +
				"<!-- begin head -->\n" +
				"<header> <a id=\"skip\" accesskey=\"S\" href=\"#moContent\">Skip Navigation</a>\n" +
				"<div class=\"moNavTabs\">\n" +
				"<ul class=\"fc\">\n" +
				"<li id=\"primary0\" class=\"active\"><a href=\"http://reference.metoffice.gov.uk/data/wmo/tdcf/\">WMO codes</a></li>\n" +
				"<li id=\"primary1\"><a href=\"http://reference.metoffice.gov.uk/data/wmo/docs/\">Documentation</a></li>\n" +
				"<li id=\"primary2\"><a href=\"http://reference.metoffice.gov.uk:8080/wmo/\">Trac</a></li>"+
				"</ul>\n" +
				"</div>\n" +
				"<div class=\"logoBar fc\"> <a id=\"topLogo\" href=\"http://www.metoffice.gov.uk/\" title=\"Go to Met Office home page\"><img src=\"http://reference.metoffice.gov.uk/data/wmo/web/img/head-mo-logo.png\" alt=\"Met Office\" /></a>\n" +
				"<h1>Search</h1>\n" +
				"<form action=\"http://search.metoffice.gov.uk/kb5/metoffice/metoffice/results.page\" method=\"get\" id=\"fsrch\">\n"+
				"<label for=\"searchInput\">Search</label>\n" +
				"<input type=\"text\" name=\"qt\" id=\"searchInput\" value=\"\" maxlength=\"1000\" accesskey=\"4\">\n" +
				"<button type=\"submit\" name=\"button\" id=\"button\" class=\"button\" value=\"Search\">Search</button>\n" +
				"</form>\n" +
				"</div>\n" +
				"</header>\n" +
				"<!-- end head -->\n";
			
				
	
		return result1;
	}
	
	private String getHGroup(String version, String language) {
		String codeString=entity.getNotation(version);
		String labelString=entity.getLabel(version,language);
		boolean includeLabel=true;
		if(labelString==null) labelString=entity.getLabel(version,CoreConfig.DEFAULT_LANGUAGE);
		if(codeString==null) {
			codeString=labelString;
			includeLabel=false;
		}
		String labelAndTypeStr="";
		if(includeLabel)
			if(labelString!=null) labelAndTypeStr=labelString+" - ";
		labelAndTypeStr=labelAndTypeStr+WebRendererStrings.getValueFor(WebRendererStrings.HAS_TYPE,language)+": ";
		// TODO new in object methods should be used here.
		String[] types=SimpleQueriesProcessor.getArrayObjectsResourcesAsURIs(entity.getResource(), MetaLanguage.typeProperty, entity.getStatements(version));
		if(types!=null) {
			for(int i=0;i<types.length;i++) {
				labelAndTypeStr=labelAndTypeStr+myLabelManager.getLabelForURI(types[i], language, myLabelManager.LANG_DEF_SHORTURI)+"&nbsp;&nbsp;";
			}
		}
		/////
	
		String result="<!-- getHGroup -->" +
				"<hgroup>" +
				"<h1>"+codeString+"</h1>" +
				"<h2 id=\"labelAndType\">"+labelAndTypeStr+"</h2>" +
				"</hgroup>";
		return result;
	}
	
	public String getHeadBlock(String language,String version,SortedMap<String,String>formatsMap) {
		String comment=entity.getGenericVersionSpecificStringValueObject(MetaLanguage.commentProperty,version);
		if(comment==null) comment=WebRendererStrings.getValueFor(WebRendererStrings.NOCOMMENTPROVIDED, language);
		String result1="<p>"+comment+"</p>\n" +
				"<dl>" +
				"<dt>URI:</dt>" +
				"<dd><a href=\""+entity.getResource().getURI()+"\">"+entity.getResource().getURI()+"</a></dd>\n" +
						"<dt>"+WebRendererStrings.getValueFor(WebRendererStrings.DOWNLOAD_IN, language)+": </dt>";
		String result2="";
		Iterator<String> standards=formatsMap.keySet().iterator();
		while(standards.hasNext()) {
			String std=standards.next();
			result2+="<dd><a href=\""+formatsMap.get(std)+"\">"+std+"</a></dd>\n";
		}
		
		String result3="</dl>";
		return result1+result2+result3;
	}
	
	protected abstract String getMetaLabel(String language);
	
	private String getMetaTable(String version, String language, boolean versionPage) {				
		String result1="<table>\n<caption id=\"metaInformationValue\">"+getMetaLabel(language)+"</caption>\n";
		
		String statusStr=null; 
		if(entity.getStateURI(version)!=null) statusStr=myLabelManager.getLabelForURI(entity.getStateURI(version),language,myLabelManager.LANG_DEF_URI);
		if(statusStr==null) statusStr=WebRendererStrings.getValueFor(WebRendererStrings.UNDEFINED, language);
		String result2="<tr><th scope=\"row\" id=\"metaStatusCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.STATUS, language)+"</th>"+
				"<td id=\"metaStatusValue\">"+statusStr+"</td></tr>";
		
		String result3="<tr><th scope=\"row\" id=\"metaOwnerCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.OWNER, language)+"</th>"+
		"<td>"+myLabelManager.getLabelForURI(entity.getOwnerURI(),language, myLabelManager.LANG_DEF_URI)+"</td></tr>";
		
		String result4="";
		String[] tags=entity.getTagsForVersion(version);
		if(tags.length>0) {
			String tagsStr="";
			for(int i=0;i<tags.length;i++) {
				tagsStr+=tags[i]+" ";
			}
			result4="<tr><th scope=\"row\">"+WebRendererStrings.getValueFor(WebRendererStrings.IN_TAGS, language)+"</th>"+
					"<td>"+tagsStr+"</td></tr>";
			
		}
		
		String result5="";
		String result6="";
		String result7="";
		// TODO proper formatting of time in html5
		if(entity.isVersioned()) {
			result5="<tr><th scope=\"row\" id=\"metaGenerationDateCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.GENERATION_DATE, language)+"</th>\n"+
					"<td id=\"metaGenerationDateValue\"><time datetime=\""+entity.getGenerationDate()+"\">"+entity.getGenerationDate()+"</time></td>\n"+
					"</tr>";
			if(versionPage) {
				result6="<tr><th scope=\"row\" id=\"metaLastUpdateCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.THIS_VERSION_DATE, language)+"</th>\n"+
				"<td id=\"metaLastUpdateValue\"><time datetime=\""+entity.getActionDate(version)+"\">"+entity.getActionDate(version)+"</time></td>\n"+
				"</tr>";
			}
			else {
				result6="<tr><th scope=\"row\" id=\"metaLastUpdateCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.LAST_UPDATE, language)+"</th>\n"+
				"<td id=\"metaLastUpdateValue\"><time datetime=\""+entity.getLastUpdateDate()+"\">"+entity.getLastUpdateDate()+"</time></td>\n"+
				"</tr>";
			}
			String versionStr=version;
			if(entity.isLastVersion(version)) versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.IS_LATEST_VERSION, language)+")";
			else versionStr+=" ("+WebRendererStrings.getValueFor(WebRendererStrings.HAS_LATEST_VERSION,language)+" <a href=\""+itemUrl+"/"+entity.getLastVersion()+"\">"+entity.getLastVersion()+"</a>)";		
			
			
			result7="<tr><th scope=\"row\" id=\"metaVersionCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.VERSION, language)+"</th>\n"+
				"<td><span id=\"metaVersionValue\">"+versionStr+"</span></td></tr>";
		}
		String result8="</table>";
		
			
		return result1+result2+result3+result4+result5+result6+result7+result8;
	}
	
	// TODO to remove!!!!!!!!!
	public String getClosing() {
		return "";
	}
	public String getClosing(String language, SortedMap langMap ) {
		String result1="</article>" +
				"<div id=\"pageLang\">" +
				"<div id=\"langSelector\"> "+WebRendererStrings.getValueFor(WebRendererStrings.ALSO_AVAILABLE_IN_LANG, language)+":"+
				"<div id=\"langContainer\">";
		Iterator<String> langsIter=langMap.keySet().iterator();
		while(langsIter.hasNext()) {
			String langKey=langsIter.next();
			result1=result1+"<a lang=\""+WebRendererStrings.getLangKeyExpanded(langKey)+"\" rel=\"alternate\" hreflang=\""+langKey+"\" title=\""+WebRendererStrings.getLangKeyExpanded(langKey)+"\" href=\""+langMap.get(langKey)+"\">"+WebRendererStrings.getLangKeyExpanded(langKey)+"</a> ";
		}
		
		result1=result1+"</div></div></div></div>";
		String result2="<!-- begin base -->\n" +
				"<footer>\n" +
				"<div id=\"footerlogoBar\"><img id=\"footerLogo\" src=\"http://reference.metoffice.gov.uk/data/wmo/web/img/head-mo-logo.png\" alt=\"Met Office\" /> </div>\n"+
				"<div id=\"footerContents\">\n" +
				"<ul id=\"footerLeft\">\n" +
				"<li class=\"first\"><a href=\"http://www.metoffice.gov.uk/about-us/legal\" accesskey=\"8\">Legal</a></li>\n" +
				"<li><a href=\"http://www.metoffice.gov.uk/about-us/legal/privacy-and-cookie\">Privacy and cookies</a></li>\n" +
				"</ul>\n" +
				"<ul id=\"footerRight\">\n" +
				"<li class=\"first\"><a href=\"http://www.metoffice.gov.uk/about-us/legal#licences\">© Crown copyright</a></li>\n" +
				"<li><a href=\"http://www.metoffice.gov.uk/\">www.metoffice.gov.uk</a></li>\n" +
				"</ul>\n" +
				"</div>\n" +
				"</footer>\n" +
				"<!-- end base -->\n" +
				"</div>\n" +
				"<script src=\"http://reference.metoffice.gov.uk/data/wmo/web/js/common_bottom.min.js\" type=\"text/javascript\"></script>\n" +
				"<!-- START OF SmartSource Data Collector TAG -->\n" +
				"<!-- Copyright (c) 1996-2011 Webtrends Inc.  All rights reserved. -->\n" +
				"<!-- Version: 9.4.0 -->\n" +
				"<!-- Tag Builder Version: 3.2  -->\n" +
				"<!-- Created: 8/1/2011 3:15:41 PM -->\n" +
				"<script src=\"http://reference.metoffice.gov.uk/data/wmo/web/js/webtrends.min.js\" type=\"text/javascript\"></script>\n" +
				"<!-- Warning: The two script blocks below must remain inline. Moving them to an external -->\n" +
				"<!-- JavaScript include file can cause serious problems with cross-domain tracking.      -->\n" +
				"<script type=\"text/javascript\">\n" +
				"//<![CDATA[\n" +
				"var _tag=new WebTrends();\n" +
				"_tag.dcsGetId();\n" +
				"//]]>\n" +
				"</script>\n" +
				"<script type=\"text/javascript\">\n" +
				"//<![CDATA[\n" +
				"_tag.dcsCustom=function(){\n" +
				"// Add custom parameters here.\n" +
				"//_tag.DCSext.param_name=param_value;\n" +
				"}\n" +
				"_tag.dcsCollect();\n" +
				"//]]>\n" +
				"</script>\n" +
				"<noscript>\n" +
				"<div><img alt=\"\" id=\"DCSIMG\" width=\"1\" height=\"1\" src=\"http://statse.webtrendslive.com/dcshckprv00000spazrt5ckdq_6d2t/njs.gif?dcsuri=/nojavascript&amp;WT.js=No&amp;WT.tv=9.4.0&amp;dcssip=www.metoffice.gov.uk\" /></div>\n" +
				"</noscript>\n" +
				"<!-- END OF SmartSource Data Collector TAG -->\n" +
				"</body>\n" +
				"</html>\n";
		return result1+result2;
	}
	
	/**
	 * Register a Map of associations URIs to URL, that the renderer can use to render links
	 * @param uri2Url
	 */
	public void registerUrlMap(Hashtable<String, String> uri2Url) {
		uri2UrlMap=uri2Url;
		
	}
	
	
	/**
	 * Note that "bread crumbs" here are not real, as we don't follow the user path,
	 * as these pages are statically generated.
	 * The approach here taken (list the first father of the current node recursively) works only if
	 * only one father x term or register is present. This is the intended usage, but this restriction is not enforced by the code.
	 * The following code would break if more than one father is present.
	 */
	private String getBredCrumbs(String version, String language,String baseURL) {
		String current=entity.getNotation(version);
		if(current==null || current=="") current=entity.getLabel(version,language);
		if(current==null) current=entity.getLabel(version,CoreConfig.DEFAULT_LANGUAGE);
		current="<a href=\""+itemUrl+"\">"+current+"</a>";
		Collection<TerminologySet>fathers=entity.getContainers(version);
		while(fathers.size()>0) {
			TerminologySet father=fathers.iterator().next();
			String fatherStr=father.getNotation(father.getLastVersion());
			if(fatherStr==null || fatherStr=="") fatherStr=father.getLabel(father.getLastVersion(),language);
			if(fatherStr==null) fatherStr=father.getLabel(father.getLastVersion(),CoreConfig.DEFAULT_LANGUAGE);
			fathers=father.getContainers(father.getLastVersion());
			String fatherURL=uri2UrlMap.get(father.getURI());
			if(father!=null) fatherURL="href=\""+fatherURL+"\"";
			else fatherURL="";
			current="<a "+fatherURL+">"+fatherStr+"</a> &gt; "+current;
		}
		
		return "<nav id=\"breadcrumb\"><p> <a href=\""+baseURL+"\">Home</a> &gt; "+current+" </p></nav>";
	}
	
	//TODO obsolete
	public String getMeta() {
		String result1="<div class=\"metaHeader\">\n"+
				"<div class=\"titles\">Meta information</div><br/>\n"+	
				"<div class=\"Information box\">\n";
		String result2="Owner: <i>"+myLabelManager.getLabelForURI(entity.getOwnerURI(), myLabelManager.URI_IF_NULL) +"</i><br/>\n";
		String result3="Generation date: <i>"+entity.getGenerationDate()+"</i><br/>\n";
		String result4="Last updated: <i>"+entity.getLastUpdateDate()+"</i><br/>\n";
		String result5="</div>\n</div>\n";
		return result1+result2+result3+result4+result5;
	}


	/*
	public String getStatusVersionedBlock(String version,Model labelRepository) {
		String result1="<div class=\"statusHeader\">\n"+
		"<div class=\"titles\">Status</div><br/>\n";
		String result2="";
		if(entity.getStateURI(version)!=null) result2="Status<sup>(1)</sup>: <i>"+SimpleQueriesProcessor.getLabelorURIForURI(entity.getStateURI(version),labelRepository)+"</i><br/>\n";
		else result2="<i>No status information defined<sup>(1)</sup</i>";
		String result3="</div>"+
		"</div>";
		return result1+result2+result3;
		
	}
	*/
	public String getVersionBlock(String version, String language, String rootURL) {
		/////
		String result1="<table><caption>\n"+
		"<span id=\"strVersionHistory\">"+WebRendererStrings.getValueFor(WebRendererStrings.VERSION_HISTORY, language)+"</span></caption>\n"+
		"<tr>\n"+
		"<th scope=\"col\"><span id=\"versionColTitle\">"+WebRendererStrings.getValueFor(WebRendererStrings.VERSION, language)+"</span></th>\n"+
		"<th scope=\"col\"><span id=\"dateColTitle\">"+WebRendererStrings.getValueFor(WebRendererStrings.DATE, language)+"</span></th>\n"+
		"<th scope=\"col\"><span id=\"actionColTitle\">"+WebRendererStrings.getValueFor(WebRendererStrings.ACTION, language)+"</span></th>\n"+
		"<th scope=\"col\"><span id=\"actorColTitle\">"+WebRendererStrings.getValueFor(WebRendererStrings.ACTOR, language)+"</span></th>\n"+
		"<th scope=\"col\"><span id=\"descColTitle\">"+WebRendererStrings.getValueFor(WebRendererStrings.DESCRIPTION, language)+"</span></th>\n"+
		"</tr>";
		
		String result2="";
		String currentVersion=entity.getLastVersion();
		while(currentVersion!=null) {
			//if(currentVersion.equals(version))
			//	result4+="<tr><td><b>"+currentVersion+"</b></td>";
			//else
			result2+="<tr><td> <a href=\""+uri2UrlMap.get(entity.getURI())+"/"+currentVersion+"\">"+currentVersion+"</a></td>";
			result2+="<td><time datetime=\""+entity.getActionDate(currentVersion)+"\">"+entity.getActionDate(currentVersion)+"</time></td>";
			result2+="<td>"+myLabelManager.getLabelForURI(entity.getActionURI(currentVersion), language, myLabelManager.LANG_DEF_URI)+"</td>";
			result2+="<td>"+myLabelManager.getLabelForURI(entity.getActionAuthorURI(currentVersion),language, myLabelManager.LANG_DEF_URI)+"</td>";
			result2+="<td>"+entity.getActionDescription(currentVersion)+"</td>";
			result2+="</tr>";
			currentVersion=(entity.getPreviousVersion(currentVersion));
		}
		
		String result3="</table>";
		
		// TODO we should print all possible versions somewhere (ask John!)
		String result5="Total number of versions known: <i>"+entity.getNumberOfVersions()+"</i> (";
		ArrayList<String> versionsToSort=new ArrayList<String>();
		String currV=entity.getLastVersion();
		versionsToSort.add(currV);
		while(currV!=null) {
			currV=entity.getPreviousVersion(currV);
			if(currV!=null) versionsToSort.add(currV);
		}
		
		String[] versions=versionsToSort.toArray(new String[versionsToSort.size()]);
		for (int v=0;v<versions.length;v++) result5+="<a href="+rootURL+"/"+versions[v]+">"+versions[v]+"</a> ";
		String result6=")<br/>";
		// end TODO
		
		return result1+result2+result3;
			
	}
	
	/*
	public abstract String getVersionHeader(SortedMap<String,String> stdMap, String version);
	*/
	public abstract String getNavigationPanel(String version, String language);
	
	
	public String getStatementsBlock(Model statsModel, String language) {
		String result1="<table><caption>"+getStatementsTitle(language)+"</caption>\n";
		String result2="<tr><th scope=\"col\"></th>"+
		"<th scope=\"col\" id=\"propAttributeCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.ATTRIBUTE , language)+"</th>"+
		"<th scope=\"col\" id=\"propValueCol\">"+WebRendererStrings.getValueFor(WebRendererStrings.VALUE , language)+"</th>"+
		"</tr>";
		String result3="";
		String result4="";
		String result5="";
		
		StmtIterator stUnsortedIter=statsModel.listStatements();
		SortedMap<String,Statement> statMapConcept=new TreeMap<String,Statement>();
		SortedMap<String,Statement> statMapCode=new TreeMap<String,Statement>();
		SortedMap<String,Statement> statMapUndef=new TreeMap<String,Statement>();
		int counterCode=0;
		int counterConcept=0;
		int counterUndef=0;
		System.out.println(">>>Statement block for: "+entity.getURI());
		while(stUnsortedIter.hasNext()) {
			Statement stat=stUnsortedIter.nextStatement();
			if(entity.getFactory().getBackgroundKnowledgeManager().getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyCodeFocus)) {
				statMapCode.put("zzz"+counterCode,stat); // TODO wrong!
				//System.out.println(">>>CODE "+"a"+counterCode+" "+stat);
				counterCode++;
			}
			else if(entity.getFactory().getBackgroundKnowledgeManager().getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyConceptFocus)) {
				statMapConcept.put("zzz"+counterConcept,stat);// TODO wrong!
				//System.out.println(">>>CONCEPT "+"a"+counterConcept+" "+stat);
				counterConcept++;
			}
			else {
				statMapUndef.put("zzz"+counterUndef,stat);// TODO wrong!
				//System.out.println(">>>UNDEF "+"a"+counterUndef+" "+stat);
				counterUndef++;
			}
		}
		Iterator<Statement> stIterCode=statMapCode.values().iterator();
		if(stIterCode.hasNext()) {
			Statement stat=stIterCode.next();
			result3+="<tr>\n"+
			"<th scope=\"row\" id=\"propCodeRow\" rowspan=\""+counterCode+"\">"+getFocusOnCodeTitle(language)+"</th>";
			result3+=getStatementsRow(stat,language)+"</tr>";
			while(stIterCode.hasNext()) {
				stat=stIterCode.next();
				result3+="<tr>"+getStatementsRow(stat,language)+"</tr>";
			}
			result3+="\n";
		}
		Collection<Statement> orderedStatMapConcept=orderConceptStatements(statMapConcept);
		Iterator<Statement> orderedStatIter=orderedStatMapConcept.iterator();
		if(orderedStatIter.hasNext()) {
			Statement stat=orderedStatIter.next();
			result4+="<tr>\n"+
			"<th scope=\"row\"  rowspan=\""+counterConcept+"\">"+getFocusOnConceptTitle(language)+"</th>";
			result4+=getStatementsRow(stat,language)+"</tr>";
			while(orderedStatIter.hasNext()) {
				stat=orderedStatIter.next();
				result4+="<tr>"+getStatementsRow(stat,language)+"</tr>";
			}
			result4+="\n";
		}
		Iterator<Statement> stIterUndef=statMapUndef.values().iterator();
		if(stIterUndef.hasNext()) {
			Statement stat=stIterUndef.next();
			result5+="<tr>\n"+
			"<th scope=\"row\"  rowspan=\""+counterUndef+"\">"+WebRendererStrings.getValueFor(WebRendererStrings.FOCUS_UNDEF , language)+"</th>";
			result5+=getStatementsRow(stat,language)+"</tr>";
			while(stIterUndef.hasNext()) {
				stat=stIterUndef.next();
				result5+="<tr>"+getStatementsRow(stat,language)+"</tr>";
			}
			result5+="\n";
		}

		String result6="</table>";
		return result1+result2+result3+result4+result5+result6;	
	}
	
	private Collection<Statement> orderConceptStatements(
			SortedMap<String, Statement> statMapConcept) {
		int counter=0;
		Comparator<String> codeComparator= new CodeComparator();
		TreeMap<String,Statement> orderedCodeStatements=new TreeMap<String,Statement>(codeComparator);
		Iterator<Statement> statementsIter=statMapConcept.values().iterator();
		while(statementsIter.hasNext()) {
			Statement stat=statementsIter.next();
			RDFNode object=stat.getObject();
			String seed=null;
			if(object.isURIResource()) {
				TerminologyEntity objectEntity=null;
				if(entity.getFactory().terminologyIndividualExist(object.asResource().getURI())) {
					objectEntity=entity.getFactory().getOrCreateTerminologyIndividual(object.asResource().getURI());
					seed=objectEntity.getNotation(objectEntity.getLastVersion());
				}
					
			}
			if(seed!=null) {
				if(orderedCodeStatements.containsKey(seed)) {
					orderedCodeStatements.put(seed+counter, stat);
					counter=counter+1;
				}
				else {
					orderedCodeStatements.put(seed, stat);
				}
					
				
			}
			else orderedCodeStatements.put(stat.getPredicate().getURI()+counter,stat );
			counter+=1;
		}
		return orderedCodeStatements.values();
	}

	protected abstract String getFocusOnConceptTitle(String language);

	protected abstract String getFocusOnCodeTitle(String language) ;

	protected abstract String getStatementsTitle(String language) ;

	private String getStatementsRow(Statement stat,String language) {
		String resultl="";
		Property pred=stat.getPredicate();
		resultl+="<td>"+myLabelManager.getLabelForURI(pred.getURI(), language,LabelManager.LANG_DEF_URI)+"</td>";
		RDFNode myNode=stat.getObject();
		if(myNode.isResource()) {
			String uri=((Resource)myNode).getURI();
			String label=myLabelManager.getLabelForURI(uri,language, LabelManager.LANG_DEF_URI);
			resultl+="<td><a href=\""+uri+"\">"+label+"</a></td>";
		}
		else if (myNode.isLiteral()) {
			String value= ((Literal)myNode).getValue().toString();
			resultl+="<td>"+value+"</td>";
		}
		else resultl+="<td>n/a</td>";
		return resultl+"\n";
	}
	
}
