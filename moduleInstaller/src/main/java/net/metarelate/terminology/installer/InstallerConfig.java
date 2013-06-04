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

package net.metarelate.terminology.installer;

public class InstallerConfig {
	public static String commandDir="commands";
	public static String libDir="lib";
	public static String resourcesDir="resources";
	public static String webDir="web";
	public static String confSubDir="configtemplates";
	public static String templatesSubDir="predefinedtemplates";
	public static String[] mustHaveConfTemplates={"defaultAuthConfig.ttl",
													"defaultPropertiesConfig.ttl",
													"demoConstraints.ttl",
													"defaultProcessConfig.ttl",
													"defaultServerConfig.ttl",
													"prefixFile.ttl"};
	public static String defaultUserHome=".tserver";
	public static String targetConfString="conf"; //TODO this should be the same as in InitializerConfig, but it is left by convention.
	
	public static String targetWebString="web";
	public static String targetDBString="db";
	public static String targetCacheString="cache";
	public static String targetAuthString="auth";
	public static String targetGitString="git";
	public static String targetTemplatesString="templates";
	public static String targetLibDirString="lib";
	public static String targetCommandDirString="bin";
	
	public static String authFilePrefix="defaultAuth";
	public static String prefixFileName="prefixFile.ttl"; //TODO this should be the same as in InitializerConfig, but it is left by convention.
	public static String seedFileName="instanceSeed.ttl"; //TODO this should be the same as in InitializerConfig, but it is left by convention.
	public static String commandName="tsc.sh";
}
