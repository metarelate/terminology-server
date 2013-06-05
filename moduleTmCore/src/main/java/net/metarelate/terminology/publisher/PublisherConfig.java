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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class PublisherConfig {
	
	public static final String uriHasUrl="http://metarelate.net/internal/cache/uriHasUrl"; // TODO maybe we should move this somewhere else
	public static final String uriHasDisk="http://metarelate.net/internal/cache/uriHasDisk"; // TODO maybe we should move this somewhere else
	
	/**
	 * The string used to compose the file name for a set 
	 * in a files-based html/rdf representation (e.g.: register.en.html).
	 * The same string is also removed from a URL when looking for a URI,
	 * when an .rdf/.html/.ttl extension is provided.
	 */
	public static String setStemString="register";	
	/**
	 * The string used to compose the file name for an individual 
	 * in a files-based html/rdf representation (e.g.: register.en.html).
	 * The same string is also removed from a URL when looking for a URI,
	 * when an .rdf/.html/.ttl extension is provided.
	 */
	public static String individualStemString="code";
	
	
	// The root of the website
	//public static final Property sitePrefixProperty=ResourceFactory.createProperty("http://metarelate.net/config/sitePrefix");
	// The root of the disk location
	public static final Property diskPrefixProperty=ResourceFactory.createProperty("http://metarelate.net/config/diskPrefix");
	// The address of the css fill
	public static final Property cssAddressProperty=ResourceFactory.createProperty("http://metarelate.net/config/cssAddress");
	//public static final Property cssAddressProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#cssAddress");
	// For every entity a namespace to be used to compose a URL 
	
	// Override the base path for a specific collection (or entity?) // TODO decide
	public static final Property overrideBasePathProperty=ResourceFactory.createProperty("http://metarelate.net/config/overridesDiskPrefix");	
	// Override the base namespace for a specific collection (or entity?) // TODO decide

	public static final Property baseURLProperty = ResourceFactory.createProperty("http://metarelate.net/config/baseURL");	
	
	public static final Property overrideBaseSiteProperty=ResourceFactory.createProperty("http://metarelate.net/config/overrideSitePrefix");
	
	
	//public static final Property localIdProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#localID");	

}
