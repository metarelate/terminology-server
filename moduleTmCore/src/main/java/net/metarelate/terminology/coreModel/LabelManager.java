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
	
package net.metarelate.terminology.coreModel;

import com.hp.hpl.jena.rdf.model.Model;
/**
 * The label manager handles label that are part of a global label pool that can be accessed by the system
 * to render URIs with their label. Labels that defined within registers are not seen by the LabelManager.
 * @author andrea_splendiani
 *
 */
public interface  LabelManager {
	/**
	 * Specifies to return the URI of a resource if no label is found
	 */
	public static final int URI_IF_NULL=1;
	
	/**
	 * Specifies to return the undefined string {@link uk.gov.metoffice.terminology.config.CoreConfig#UNDEFINED_LABEL} if no label is found.
	 */
	public static final int UNDEF_IF_NULL=2;
	
	/**
	 * If no label for the specified label is found, look for the a label in the default language. If no label is found, look for a label without language specification. If nothings is found returns the local part of the URI as a label.
	 */
	public static final int LANG_DEF_SHORTURI=3;
	
	/**
	 * If no label for the specified label is found, look for the a label in the default language. If no label is found, look for a label without language specification. If nothings is found returns null.
	 */
	public static final int LANG_DEF_NULL=4;
	
	/**
	 * If no label for the specified label is found, look for the a label in the default language. If no label is found, look for a label without language specification. If nothings is found returns the URI.
	 */
	public static final int LANG_DEF_URI=5;
	
	/**
	 * Returns a label for a URI, following the method specified. Note that a random label matching the requirements is returned.
	 * @param uri
	 * @param method what to do if no label matching the criteria is found
	 */
	public abstract String getLabelForURI(String uri, int method);
	
	/**
	 * Returns a label for a URI, for a given language and following the method specified. Note that a random label matching the requirements is returned.
	 * @param uri
	 * @param language specified as in the RDF representation
	 * @param method what to do if no label matching the criteria is found
	 */
	public abstract String getLabelForURI(String uri, String language,int method);
	
	/**
	 * Returns the list of languages in which labels for the given uri are available.
	 * TODO this is currently unimplemented and returns null.
	 * @param uri
	 * @return
	 */
	public abstract String[] getLanguagesForURI(String uri);
	
	/**
	 * Record a set of labels to uri association.
	 * @param labelsModel a Jena Model containing a set of statements like uri rdfs:label label
	 * TODO other types of statements maybe be included in the label manager (not an error, but not a design choice)
	 */
	public abstract void registerLabels(Model labelsModel);
}
