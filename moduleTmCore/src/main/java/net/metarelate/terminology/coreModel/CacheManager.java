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

/**
 * Provides some caching functionalities
 * @author andrea_splendiani
 *
 */
public interface CacheManager {
	/**
	 * Returns a value for a resource/property pair. A random value is returned if more than one is present.
	 */
	public  String getValueFor(String resource, String property);

	/**
	 * Remove all statements having the specified resource and property and a literal as an object
	 */
	public  boolean cleanValueFor(String resource, String property);

	/**
	 * Records a statement
	 * @param resource the URI of the resource
	 * @param property the URI of the property
	 * @param value a literal value
	 */
	public  void recordValue(String resource, String property,
			String value);
	/**
	 * Removes all literal values having the specified resources and property, 
	 * then introduces one new statement having the specified value as a literal
	 * @param value a literal value
	 */
	public  void changeValue(String resource, String property,
			String value);

	/**
	 * Removes all statements having the specified property (Any subject or Object).
	 */
	public void forceCleanProp(String propertyURI);

	/**
	 * Issue a synch to the cache persistence layer (depends on the implementation)
	 */
	public void synch();

	/**
	 * Returns the subject of a statement having the specified property and resource as an object.
	 * If more than one statement is found, one random subjct is returned.
	 * @param urlRequested The URI object of a statement
	 * @param property The property of statement
	 * @return the URI subject of the statement
	 */
	public String getSubjectForValue(String urlRequested, String property);

}