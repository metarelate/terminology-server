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

package net.metarelate.terminology.auth;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A collection of static configuration fields
 * @author andrea_splendiani
 *
 */
public class AuthConfig {
	/**
	 * A resource that identifies the File-based authentication modality
	 */
	public static final String isConfigFileString = "http://metarelate.net/config/authIsInFile";
	
	/**
	 * Represents "all" entries. 
	 * This is used to specify general authorizations: e.g.: agent-action-all.
	 * 
	 */
	public static final String allEntities="http://metarelate.net/config/allEntities";
	
	/**
	 * Represents "all" actions
	 */
	public static final String allActions="http://metarelate.net/config/allActions";
	
	/**
	 * Represents "all" agents
	 */
	public static final String allActors="http://metarelate.net/config/allActors";
	
	/**
	 * A property specifying which Auth mode is used
	 */
	public static final String authConfigURI="http://metarelate.net/config/hasAuthConfigType";
	
	/**
	 * Same as @see authConfigURI, but as Property class.
	 */
	public static final Property authConfigProperty=ResourceFactory.createProperty(authConfigURI);

}
