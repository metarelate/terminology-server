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

package net.metarelate.terminology.config;


public class CoreConfig {
	public static final String INIT_VERSION = "v0";
	
	public static String VERSION_NUMBER="0.7.0";
	public static String VERSION_CODENAME="tdb-all";
	
	public static String UNDEFINED_LABEL="undefined";
	
	//public static String totalGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/totalGraph";
	//public static String labelsGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/labelsGraph";
	//public static String extraGraphURI="http://metoffice.gov.uk/terminology/tools/terminologyManager/extraGraph";

	public static String DEFAULT_FROM_RDF_DESCRIPTION="imported from table";

	public static final String DEAFULT_FROM_RDF_IMPORT_ACTION = "http://metoffice.gov.uk/action/importFromTables";

	public static final String DEFAULT_IMPORT_STATUS = MetaLanguage.statusOutOfRegistry.getURI();

	public static final String DEFAULT_LANGUAGE = "en";

	
	//public static Resource totalGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/totalGraph");
	//public static Resource labelsGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/labelsGraph");
	//public static Resource extraGraph=ResourceFactory.createResource("http://metoffice.gov.uk/terminology/tools/terminologyManager/extraGraph");

	public static final String rootDirString=".tserver";
	public static final String gitDirString="git";
	public static final String dbDirString="db";
	public static final String confDirString="conf";
}
