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

package net.metarelate.terminology.modelBuilders;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

class TerminologyModelBuilderConfig {
	public static final Resource pragmaExpandDashAndSuppress=ResourceFactory.createResource("http://metarelate.net/config/ExpandDashAndSuppress");
	public static final Resource pragmaExpandTree =ResourceFactory.createResource("http://metarelate.net/config/ExpandTreeProcedure");
	public static final Resource pragmaSuppress =ResourceFactory.createResource("http://metarelate.net/config/pragmaSuppressSource");
	public static final Resource pragmaHardLimitCut =ResourceFactory.createResource("http://metarelate.net/config/hardLimitCut");

	public static final Property pragmaTreeCollection = ResourceFactory.createProperty("http://metarelate.net/config/pragmaTreeCollection");
	public static final Property pragmaSchemaProperty = ResourceFactory.createProperty("http://metarelate.net/config/pragmaSchema");

	public static final Property pragmaPropProperty = ResourceFactory.createProperty("http://metarelate.net/config/pragmaProp");
	public static final Property pragmaOverrideProp = ResourceFactory.createProperty("http://metarelate.net/config/overrideProp");
	public static final Property pragmaHardLimit = ResourceFactory.createProperty("http://metarelate.net/config/hardLimit");
	public static final Property pragmaPad = ResourceFactory.createProperty("http://metarelate.net/config/pragmaPad");
}
