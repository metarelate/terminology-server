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

/**
 * TODO this is to be re-designed following a new initialization procedure
 */

package net.metarelate.terminology.instanceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;


import net.metarelate.terminology.auth.AuthConfig;
import net.metarelate.terminology.auth.AuthRegistryManager;
import net.metarelate.terminology.auth.AuthServer;
import net.metarelate.terminology.auth.AuthServerFactory;
import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.CacheManager;
import net.metarelate.terminology.coreModel.CacheManagerTDBImpl;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyFactoryTDBImpl;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.NonConformantRDFException;
import net.metarelate.terminology.management.ConstraintsManager;
import net.metarelate.terminology.management.RegistryPolicyManager;
import net.metarelate.terminology.management.TerminologyManager;
import net.metarelate.terminology.publisher.PublisherConfig;
import net.metarelate.terminology.publisher.PublisherManager;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


//TODO Note the use of thisInstance.org as a bougs URL of namespace. Perhaps this could be linked to the instance seed (URL), although not necessary.

/**
 * Initializer is responsible for initalizing an instance of the terminology server (e.g. directory structure).
 * It can be called with or without arguments. Arguments override initialization default settings.
 * If directories of necessary configuration files are missing, default versions are created.
 * @author andreasplendiani
 *
 */
public class Initializer {
	private String rootDirectoryString=null;
	private static String confDirAbsoluteString=null;
	private Model globalConfigurationGraph=null;
	private static String dbDirAbsoluteString=null;
	private static String cacheDirAbsoluteString=null;
	private static String authDirAbsoluteString=null;
	private static String gitDirAbsoluteString=null;
	private static String templatesDirAbsoluteString=null;
	private static String webDirAbsoluteString=null;
	private static String prefixFileAbsoluteString=null;
	private Map<String,String> nsPrefixMap=null;
	private static String seedFileAbsoluteString=null;
	private static String instanceIdentifier=null; // This will be the unique identifier for this terminology server instance
	private String defaultUserURI=null;
	
	private static String serverAddress=null;
	private static String serverName=null;
	
	
	
	
	private  	AuthServer myAuthServer=null;
	public  		AuthRegistryManager myAuthManager=null;
	public  		TerminologyManager myTerminologyManager=null;
	public  		TerminologyFactory myFactory=null;
	public 		ConstraintsManager myConstraintsManager=null;
	public 		RegistryPolicyManager myRegistryPolicyManager=null;
	public 		CacheManager myCache=null;
	public 		PublisherManager myPublisherManager=null;
	
	//protected String rootDirString=CoreConfig.rootDirString;
	
	
	
	public Initializer(String confDir) throws ConfigurationException {
		if((new File(confDir)).isDirectory()) {
			rootDirectoryString=confDir;
			Loggers.coreLogger.debug("Root directory provided via command line: "+confDir);
		}
		else resolveConfDir();
		construct();
	}
	
	public Initializer() throws ConfigurationException {
		resolveConfDir();
		construct();
	}
	
	private void resolveConfDir() throws ConfigurationException {
		String dirFromSystemVar=System.getenv("TSHOME");
		File defaultDirFile=new File(System.getProperty( "user.home" ),CoreConfig.systemRootDirString);
		if(dirFromSystemVar!=null)  {
			Loggers.coreLogger.trace("Trying system dir from system TSHOME :"+dirFromSystemVar);
			if((new File(dirFromSystemVar)).isDirectory()) {
				rootDirectoryString=dirFromSystemVar;
				Loggers.coreLogger.debug("Root directory provided via system variable: "+rootDirectoryString);
				return;
			}
		}
		Loggers.coreLogger.trace("Trying default system dir :"+defaultDirFile);
		if(defaultDirFile.isDirectory()) {
			rootDirectoryString=defaultDirFile.getAbsolutePath();
			Loggers.coreLogger.debug("Root directory is default: "+rootDirectoryString);

		}
		if(rootDirectoryString==null) throw new ConfigurationException("Unable to find valid root directory");
		
	}
	
	private void construct() throws ConfigurationException{
		Loggers.coreLogger.debug("Initialization started");
		checkAndRetrieveInformation();
		Loggers.coreLogger.debug("Configuration check OK");
		//prepareConfigurationLayout();
		//prepareDefaultFiles();
		Loggers.coreLogger.debug("Building System components");
		buildSystemComponents();
		Loggers.coreLogger.info("TServer is ready");
	}
	
	
	private void checkAndRetrieveInformation() throws ConfigurationException {
		confDirAbsoluteString=checkDirectory(rootDirectoryString,CoreConfig.confDirString);
		Loggers.coreLogger.debug("Configuration directory found at: "+confDirAbsoluteString);
		Loggers.coreLogger.trace("Loading configuration files");
		globalConfigurationGraph=loadConfigurationGraph();
		Loggers.coreLogger.debug("Configuration files found for a total of "+globalConfigurationGraph.size()+" statements");
		
		Loggers.coreLogger.debug("Checking systems diectories from configuration");
		dbDirAbsoluteString=extractDirAndCheck("DB",CoreConfig.hasDBDirProperty);
		cacheDirAbsoluteString=extractDirAndCheck("Cache",CoreConfig.hasCacheDirProperty);
		authDirAbsoluteString=extractDirAndCheck("Auth",CoreConfig.hasAuthDirProperty);
		gitDirAbsoluteString=extractDirAndCheck("Git",CoreConfig.hasGitDirProperty);	
		templatesDirAbsoluteString=extractDirAndCheck("Templates",CoreConfig.hasTemplatesDirProperty);	
		webDirAbsoluteString=extractDirAndCheck("WebApp",CoreConfig.hasWebDirProperty);	
		
		Loggers.coreLogger.debug("Checking systems files from configuration");
		prefixFileAbsoluteString=extractFileAndCheck("Prefix",CoreConfig.hasPrefixFileProperty);
		File prefixFile=new File(prefixFileAbsoluteString);
		Model prefixModel=ModelFactory.createDefaultModel();
		try {
			prefixModel.read(new FileInputStream(prefixFileAbsoluteString),"http://thisInstance.org/configuration/","Turtle");
			nsPrefixMap=prefixModel.getNsPrefixMap();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new ConfigurationException("Problems in reading prefix file: "+prefixFileAbsoluteString);
		}
		Loggers.coreLogger.debug("Found map for "+nsPrefixMap.keySet().size()+" prefixes");	
		
		seedFileAbsoluteString=extractFileAndCheck("InstanceSeed",CoreConfig.hasSeedFileProperty);
		instanceIdentifier=getSeedString(seedFileAbsoluteString);
		Loggers.coreLogger.debug("Instance ID: "+instanceIdentifier);
		
		
		Loggers.coreLogger.debug("Now recovering system information from configuration files");
		defaultUserURI=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleResourceString(globalConfigurationGraph, CoreConfig.hasDefaultUserURIProperty);
		if(defaultUserURI==null) throw new ConfigurationException("Unable to find default user");
		Loggers.coreLogger.debug("Default user : "+defaultUserURI);
		
		analyseSystemParameters();
		/*
		 * WORKING HERE
		 */
	}

	private String checkDirectory(String baseDir, String dir) throws ConfigurationException {
		File dirToCheck=new File(baseDir,dir);
		if(dirToCheck.isDirectory()) {
			Loggers.coreLogger.debug("Found directory: "+dirToCheck.getAbsolutePath());
			return dirToCheck.getAbsolutePath();
		}
		else {
			Loggers.coreLogger.fatal("Directory not foound : "+dirToCheck);
			throw new ConfigurationException("Unable to find directory: "+dirToCheck);
		}
	}
	
	private String extractDirAndCheck(String dirDisplayName,String dirProperty) throws ConfigurationException {
		String tempDir=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(globalConfigurationGraph, dirProperty);
		if(tempDir==null ||!((new File(tempDir)).isDirectory()))
				throw new ConfigurationException("Unable to find Dir "+dirDisplayName+" at: "+tempDir);
		Loggers.coreLogger.debug(dirDisplayName+" dir found at: "+tempDir);
		return tempDir;
	}
	
	private String extractFileAndCheck(String fileDisplayName,String fileProperty) throws ConfigurationException {
		String tempFile=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(globalConfigurationGraph, fileProperty);
		if(tempFile==null ||!((new File(tempFile)).isFile()))
				throw new ConfigurationException("Unable to find File "+fileDisplayName+" at: "+tempFile);
		Loggers.coreLogger.debug(fileDisplayName+" file found at: "+tempFile);
		return tempFile;
	}
	
	/*
	 * REFACTOR LINE BELOW
	 */
	
	//TODO Inits could be made more modular
	private void analyseSystemParameters() throws ConfigurationException {
		serverAddress="Unknown";
		serverName="Unknown";
		try {
			InetAddress myAddress=InetAddress.getLocalHost();
			serverAddress=myAddress.getHostAddress();
			serverName=myAddress.getHostName();
		} catch (UnknownHostException e) {
			Loggers.coreLogger.warn("Unable to determine server properties");
			e.printStackTrace();
		}
		Loggers.coreLogger.debug("Server address : "+serverAddress);
		Loggers.coreLogger.debug("Server name : "+serverName);
	}
	



	private void buildSystemComponents() throws ConfigurationException {
		// TODO maybe paramaters passed throughout the system could be a bit rationalized
		
		String tdbPath=dbDirAbsoluteString;
	
		CoreConfig.parseConfiguration(getConfigurationGraph());
		myFactory=new TerminologyFactoryTDBImpl(tdbPath);
		myAuthServer=AuthServerFactory.createServerFromConfig(getConfigurationGraph());
		myRegistryPolicyManager=new RegistryPolicyManager(getConfigurationGraph());
		myConstraintsManager=new ConstraintsManager(this);
		myAuthManager=new AuthRegistryManager(myAuthServer,myFactory);
		myTerminologyManager=new TerminologyManager(this);
		myCache=new CacheManagerTDBImpl(this,cacheDirAbsoluteString);
		myPublisherManager=new PublisherManager(this);
		
	}


	
	public String getServerName() {
		return serverName;
	}
	
	
	private String getSeedString(String seedFileString) throws ConfigurationException {
		String result=null;
		Model seedModel=ModelFactory.createDefaultModel();
		try {
			seedModel.read(new FileInputStream(seedFileString),"http://thisInstance.org/configuration/","Turtle");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new ConfigurationException("Problems in reading seed file : "+seedFileString);
		}
		try {
			result=SimpleQueriesProcessor.getSingleMandatoryLiteral(ResourceFactory.createResource("http://thisInstance.org"), ResourceFactory.createProperty(CoreConfig.hasInstanceIdentifierURI), seedModel).getValue().toString();
		} catch (NonConformantRDFException e) {
			e.printStackTrace();
			throw new ConfigurationException("Unable to read in seed from configuration file");
		}
		if(result==null) throw new ConfigurationException("Null seed");
		return result;
			
	}
	
	public String getWebDirectory() {
		return webDirAbsoluteString;
	}
	public String getTemplatesDirectory() {
		return templatesDirAbsoluteString;
	}
	
	/*
	public String getRootDirectory() {
		return rootDirectoryString;
	}
	*/
	public String getDefaultUserURI() {
		return defaultUserURI;
	}
	
	/**
	 * The configuration graph is the union of all graphs in the configuration dir.
	 * @throws ConfigurationException 
	 */
	public Model getConfigurationGraph() throws ConfigurationException {
		return globalConfigurationGraph;
	}

	private Model loadConfigurationGraph() throws ConfigurationException {
		Model configuration=ModelFactory.createDefaultModel();
		File confDir=new File(confDirAbsoluteString);
		for (File confFile : confDir.listFiles()) {
			try {
				configuration.read(new FileInputStream(confFile),"http://thisInstance.org/configuration/","Turtle");
			} catch (FileNotFoundException e) {
				throw new ConfigurationException("Unable to read configuration file: "+confFile.getAbsolutePath()+" Note that this file should be written in Turtle syntax");
			}
		}
		return configuration;
	}
	
	
	public static File[] getAuthFiles() {
		File authConfDir=new File(authDirAbsoluteString);
		return authConfDir.listFiles();
	}
	
	public boolean isRemote() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasRemote() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getWorkingDirectory() {
		return System.getProperty("user.dir");
	}
	
	public Map<String,String> getPrefixMap() {
		return nsPrefixMap;
	}

	public String getWARFile() throws ConfigurationException {
		File warDir=new File(getWebDirectory());
		//Note that we should have already checked this is an ok dir;
		File[] warFiles=warDir.listFiles();
		if(warFiles.length==0) throw new ConfigurationException("Invalid WAR file");
		return warFiles[0].getAbsolutePath();
	}
	
	
	


}
