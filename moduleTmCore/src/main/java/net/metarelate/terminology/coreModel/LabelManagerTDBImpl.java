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

// TO

//
package net.metarelate.terminology.coreModel;
// TODO some bits of label processing is here, some in utils and some on the entity. This should be uniformed!
import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class LabelManagerTDBImpl implements LabelManager {
	private Model myLabelGraph;
	private String defaultLanguage=CoreConfig.DEFAULT_LANGUAGE;
	public LabelManagerTDBImpl(Model labelGraph) {
		myLabelGraph=labelGraph;
		
	}

	/**
	 * @see LabelManager#getLabelForURI(String, int)
	 */
	public String getLabelForURI(String uri, int method) {
		return getLabelForURI(uri,defaultLanguage,method);
	}

	/**
	 * @see LabelManager#getLabelForURI(String,String,int)
	 */
	public String getLabelForURI(String uri, String language, int method) {
		// TODO we could definitively optimize this.
		//Does a language exists in the specified language ?
		NodeIterator myObjIter=myLabelGraph.listObjectsOfProperty(myLabelGraph.createResource(uri), MetaLanguage.labelProperty);
		
		while(myObjIter.hasNext()) {
			RDFNode myObj=myObjIter.nextNode();
			if(myObj.isLiteral()) {
				Literal myLit=myObj.asLiteral();
				if(myLit.getLanguage().equals(language)) {
					return myLit.getValue().toString();
				}
			}
		}
		//A perfect match was not found. What's next ?
		if(method==LANG_DEF_SHORTURI || method==LANG_DEF_NULL || method==LANG_DEF_URI) {
			//We look for the default language
			myObjIter=myLabelGraph.listObjectsOfProperty(myLabelGraph.createResource(uri), MetaLanguage.labelProperty);
			
			while(myObjIter.hasNext()) {
				RDFNode myObj=myObjIter.nextNode();
				if(myObj.isLiteral()) {
					Literal myLit=myObj.asLiteral();
					if(myLit.getLanguage().equals(CoreConfig.DEFAULT_LANGUAGE)) {
						return myLit.getValue().toString();
					}
				}
			}
		}
		//Trying no lang
		if(method==LANG_DEF_SHORTURI || method==LANG_DEF_NULL || method==LANG_DEF_URI) {
			myObjIter=myLabelGraph.listObjectsOfProperty(myLabelGraph.createResource(uri), MetaLanguage.labelProperty);
			while(myObjIter.hasNext()) {
				RDFNode myObj=myObjIter.nextNode();
				if(myObj.isLiteral()) {
					Literal myLit=myObj.asLiteral();
					if(myLit.getLanguage().equals("")) {
							return myLit.getValue().toString();
					}
				}
			}
		}
	
		// Still no label found.
		if(method==LANG_DEF_SHORTURI) {
			String result=ResourceFactory.createResource(uri).getLocalName();
			//Loggers.coreLogger.trace("Got a short uri as a label for : "+uri+" -> "+result);
			return result;
		}
		else if(method==URI_IF_NULL || method==LANG_DEF_URI) return uri;
		else if(method==UNDEF_IF_NULL) return getUndefString(language);
		else if(method==LANG_DEF_NULL) return null;
		else return null;
			
			
		
	}

	// TODO remove duplicate in Config
	private String getUndefString(String language) {
		if(language.equals("en")) return "Undefined";
		else if(language.equals("fr")) return "Ind�fini";
		else if(language.equals("ru")) return "\u041D\u0435\u043E\u043F\u0440\u0435\u0434\u0435\u043B\u0435\u043D\u043D\u044B\u0435 ";
		else if(language.equals("es")) return "indefinido";
		else if(language.equals("zh")) return "\u672A\u5B9A\u7FA9";
		else if(language.equals("ar")) return "\u063A\u064A\u0631 \u0645\u062D\u062F\u062F";
		else return "Undefined";
	}

	/**
	 * @see LabelManager#getLanguagesForURI(String)
	 */
	public String[] getLanguagesForURI(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see LabelManager#registerLabels(Model)
	 */
	public void registerLabels(Model labelsModel) {
		myLabelGraph.add(labelsModel);
		
	}

}
