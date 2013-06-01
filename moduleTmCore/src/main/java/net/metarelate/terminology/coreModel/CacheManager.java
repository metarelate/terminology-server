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

public interface CacheManager {

	public  String getValueFor(String resource, String property);

	public  boolean cleanValueFor(String resource, String property);

	public  void recordValue(String resource, String property,
			String value);

	public  void changeValue(String resource, String property,
			String value);

	public void forceCleanProp(String propertyURI);

	public void synch();

	public String getSubjectForValue(String urlRequested, String property);

}