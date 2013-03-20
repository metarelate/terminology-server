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
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyFactoryTDBImpl;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.NonConformantRDFException;
import net.metarelate.terminology.management.ConstraintsManager;
import net.metarelate.terminology.management.RegistryPolicyManager;
import net.metarelate.terminology.management.TerminologyManager;
import net.metarelate.terminology.utils.SSLogger;
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
	private static String instanceIdentifier=null; // This will be the unique identifier for this terminology server instance
	private static String serverAddress=null;
	private static String serverName=null;
	private static String userHomeString=null;
	private static String gitDirAbsoluteString=null;
	private static String dbDirAbsoluteString=null;
	private static String confDirAbsoluteString=null;
	private static String authDirAbsoluteString=null;
	private static String seedFileAbsoluteString=null;
	private static String prefixFileAbsoluteString=null;
	public static String defaultUserName=null;
	private String rootDirectoryString=null;
	
	private  AuthServer myAuthServer=null;
	public  AuthRegistryManager myAuthManager=null;
	public  TerminologyManager myTerminologyManager=null;
	public  TerminologyFactory myFactory=null;
	public ConstraintsManager myConstraintsManager=null;
	public RegistryPolicyManager myRegistryPolicyManager=null;
	public boolean debugMode=true;	// TODO this shold come from the configuration file
	
	protected String rootDirString=CoreConfig.rootDirString;
	
	private Map<String,String> nsPrefixMap=null;
	
	public Initializer(String confDir) throws ConfigurationException {
		rootDirString=confDir;
		construct();
	}
	
	public Initializer() throws ConfigurationException {
		construct();
	}
	
	public Initializer(String sysDir, boolean debug) throws ConfigurationException {
		debugMode=debug;
		construct();
	}

	public void construct() throws ConfigurationException{
		prepareConfigurationLayout();
		prepareDefaultFiles();
		buildSystemComponents();
	}
	
	public String getRootDirectory() {
		return rootDirectoryString;
	}
	
	//TODO Inits could be made more modular
	public void prepareConfigurationLayout() throws ConfigurationException {
		if(userHomeString==null) userHomeString = System.getProperty( "user.home" );
		SSLogger.log("User Home: "+ userHomeString);
		//System.out.println("User Home: "+ userHomeString);
		File rootDirectory=new File(userHomeString,rootDirString);
		checkOrCreateDirectory(rootDirectory);
		rootDirectoryString=rootDirectory.getAbsolutePath();
		// Note: we don't allow overriding of host or server name. This may be changed.
		try {
			InetAddress myAddress=InetAddress.getLocalHost();
			serverAddress=myAddress.getHostAddress();
			serverName=myAddress.getHostName();
			defaultUserName=System.getProperty("user.name");
		} catch (UnknownHostException e) {
			serverAddress="Unknown";
			serverName="Unknown";
			defaultUserName="Unknown";
			e.printStackTrace();
		}
		
		File gitDir;
		if(gitDirAbsoluteString!=null) gitDir=new File(gitDirAbsoluteString);
		else{
			gitDir=new File(rootDirectory.getAbsolutePath(),CoreConfig.gitDirString);
			gitDirAbsoluteString=gitDir.getAbsolutePath();
		}
		checkOrCreateDirectory(gitDir);
		
		File dbDir;
		if(dbDirAbsoluteString!=null) dbDir=new File(dbDirAbsoluteString);
		else{
			dbDir=new File(rootDirectory.getAbsolutePath(),CoreConfig.dbDirString);
			dbDirAbsoluteString=dbDir.getAbsolutePath();	
		}
		checkOrCreateDirectory(dbDir);
		
		File confDir;
		if(confDirAbsoluteString!=null) confDir=new File(confDirAbsoluteString);
		else{
			confDir=new File(rootDirectory.getAbsolutePath(),CoreConfig.confDirString);
			confDirAbsoluteString=confDir.getAbsolutePath();
		}
		checkOrCreateDirectory(confDir);
		
		File authDir;
		if(authDirAbsoluteString!=null) authDir=new File(authDirAbsoluteString);
		else{
			authDir=new File(rootDirectory.getAbsolutePath(),CoreConfig.authDirString);
			authDirAbsoluteString=authDir.getAbsolutePath();
		}
		checkOrCreateDirectory(authDir);
		
		File seedFile;
		if(seedFileAbsoluteString!=null) seedFile=new File(seedFileAbsoluteString);
		else{
			seedFile=new File(rootDirectory.getAbsolutePath(),CoreConfig.seedFileString);
			seedFileAbsoluteString=seedFile.getAbsolutePath();
		}
		checkOrCreateSeedFile();
		
		File prefixFile;
		if(prefixFileAbsoluteString!=null) prefixFile=new File(prefixFileAbsoluteString);
		else{
			prefixFile=new File(rootDirectory.getAbsolutePath(),CoreConfig.prefixFileString);
			prefixFileAbsoluteString=prefixFile.getAbsolutePath();
		}
		checkOrCreatePrefixFile();
		

		// TODO Note also that we should be sure time is in synch globally
	}
	



	public void buildSystemComponents() throws ConfigurationException {
	Model configuration=null;
		try {
			configuration = getConfigurationGraph();
		} catch (ConfigurationException e) {
			SSLogger.log("Problems in reading configuration files");
			e.printStackTrace();
			System.exit(-1);
		}
		String tdbPath=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(configuration, MetaLanguage.tdbPrefixProperty);
	
		//TODO for the time being only tdb is supported!
		if(tdbPath==null) {
			//throw new ConfigurationException("Unable to find a TDB directory");
			SSLogger.log("Unable to find a TDB directory");
			System.exit(-1);
		}
		
		myFactory=new TerminologyFactoryTDBImpl(tdbPath);
		myAuthServer=AuthServerFactory.createServerFromConfig(getConfigurationGraph());
		myRegistryPolicyManager=new RegistryPolicyManager(getConfigurationGraph());
		myConstraintsManager=new ConstraintsManager(this);
		myAuthManager=new AuthRegistryManager(myAuthServer,myFactory);
		myTerminologyManager=new TerminologyManager(this);
		
	}

	
	private void checkOrCreateSeedFile() throws ConfigurationException {
		File seedFile=new File(seedFileAbsoluteString);
		if(!seedFile.exists()) {
			instanceIdentifier=UUID.randomUUID().toString();
			String idStatement="<http://thisInstance.org> <"+CoreConfig.hasInstanceIdentifierURI+"> "+"\""+instanceIdentifier+"\";\n.";
			writeInFile(seedFile,idStatement);
		}
		else {
			Model seedModel=ModelFactory.createDefaultModel();
			try {
				seedModel.read(new FileInputStream(seedFileAbsoluteString),"http://thisInstance.org/configuration/","Turtle");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new ConfigurationException("Configuration file not found: "+seedFileAbsoluteString);
			}
			try {
				instanceIdentifier=SimpleQueriesProcessor.getSingleMandatoryLiteral(ResourceFactory.createResource("http://thisInstance.org"), ResourceFactory.createProperty(CoreConfig.hasInstanceIdentifierURI), seedModel).getValue().toString();
			} catch (NonConformantRDFException e) {
				e.printStackTrace();
				throw new ConfigurationException("Unable to read instance id from configuration file");
			}
			
		}
		
	}
	
	private void checkOrCreatePrefixFile() throws ConfigurationException {
		// TODO Auto-generated method stub
		File prefixFile=new File(prefixFileAbsoluteString);
		if(!prefixFile.exists()) {
			String defaultContent=
					"@prefix rdfs:   	<http://www.w3.org/2000/01/rdf-schema#> .\n"+
					"@prefix rdf:    	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"+
					"@prefix xsd:    	<http://www.w3.org/2001/XMLSchema#> .\n"+
					"@prefix skos: 		<http://www.w3.org/2004/02/skos/core#> .\n"+
					"[] a <http://bog.us/bougs> ;\n" +
					".";
			writeInFile(prefixFile,defaultContent);
		}
		else {
			Model prefixModel=ModelFactory.createDefaultModel();
			try {
				prefixModel.read(new FileInputStream(prefixFileAbsoluteString),"http://thisInstance.org/configuration/","Turtle");
				nsPrefixMap=prefixModel.getNsPrefixMap();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				throw new ConfigurationException("Prefixes file not found: "+seedFileAbsoluteString);
			}
			
		}
		
	}
	
	
	private void checkOrCreateDirectory (File dir) throws ConfigurationException  {
		if(dir.exists()) {
			if(!dir.isDirectory()) throw new ConfigurationException("Unable to create directory "+dir+" as a file with the same path already exists");
		}
		else dir.mkdir();
		
	}
	
	private void prepareDefaultFiles() throws ConfigurationException {
		//TODO we only do this is no config is found. If there is some config, it's up to the user to have it complete.
		File confDir=new File(confDirAbsoluteString);
		if(confDir.listFiles().length>0) return;
			
		String defServerStatements="<http://thisInstance.org> <"+MetaLanguage.tdbPrefixProperty+"> "+"\""+dbDirAbsoluteString+"\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n.\n";
		defServerStatements+="<http://thisInstance.org> <"+MetaLanguage.authConfigURI +"> "+"<"+AuthConfig.isConfigFileString+"> ;\n.\n";
		String baseURL=getServerName()+"/web";
		File diskPrefixFile=new File(getWorkingDirectory(),CoreConfig.baseDiskDir);
		String diskPrefix=diskPrefixFile.getAbsolutePath();
		defServerStatements+="<http://thisInstance.org> <"+MetaLanguage.baseURLProperty +"> "+"\"http://"+baseURL+"\" ;\n.\n";
		defServerStatements+="<http://thisInstance.org> <"+MetaLanguage.diskPrefixProperty +"> "+"\""+diskPrefix+"\" ;\n.\n";
		
		createFileAndFillWithString(confDirAbsoluteString,"defaultServerConfig.ttl",defServerStatements);
		
		// TODO this is : me what I can on what. Arguably this should start with me can create anything at the top register (empty register?)
		// However, as a conf option, one could be granted access to everything.
		String defAuthStatements="<"+getDefaultUserURI()+"> <"+AuthConfig.allURI+"> "+"<"+AuthConfig.allURI+"> ;\n.\n";
		createFileAndFillWithString(authDirAbsoluteString,"defaultAuthConfig.ttl",defAuthStatements);
		
		
		String defProcessStatements=
				"@prefix core:		<http://metarelate.net/core/types/>	.\n" +
				"@prefix states: 	<http://metarelate.net/core/states/> .\n" +
				"@prefix actions:	<http://metarelate.net/core/actions/> .\n" +
				"@prefix config:		<http://metarelate.net/core/config/> .\n" +
				"@prefix default:	<http://metarelate.net/default/config/> .\n" +
				"@prefix rdfs:		<http://www.w3.org/2000/01/rdf-schema#> .\n" +
				"actions:update a core:action;\n" +
				"rdfs:label	\"Update\"@en;\n" +
				"config:overrides actions:update;\n" +
				"config:hasEffectOnCode default:actionUpdate1;\n" +
				"config:hasEffectOnCode default:actionUpdate2;\n" +
				"config:hasEffectOnReg default:actionUpdate1;\n" +
				"config:hasEffectOnReg default:actionUpdate2;\n" +
				".\n" +
				"default:actionUpdate1 a core:actionRole;\n" +
				"config:preThis states:default;\n" +
				"config:postThis states:default;\n" +
				".\n" +
				"default:actionUpdate2 a core:actionRole;\n" +
				"config:preThis states:valid;\n" +
				"config:postThis	states:valid;\n" +
				".\n" +
				"actions:obsolete a core:action;\n" +
				"rdfs:label	\"Obsolete\"@en;\n" +
				"config:overrides actions:obsolete;\n" +
				"config:hasEffectOnCode default:actionObsolete1;\n" +
				"config:hasEffectOnCode default:actionObsolete2;\n" +
				"config:hasEffectOnReg default:actionObsolete1;\n" +
				"config:hasEffectOnReg default:actionObsolete2;\n" +
				".\n" +
				"default:actionObsolete1 a core:actionRole;\n" +
				"config:preThis states:default;\n" +
				"config:postThis states:obsoleted;\n" +
				".\n" +
				"default:actionObsolete2 a core:actionRole;\n" +
				"config:preThis states:valid;\n" +
				"config:postThis states:obsoleted;\n" +
				".\n" +
				"actions:supersed a core:action;\n" +
				"rdfs:label	\"Supersed\"@en;\n" +
				"config:overrides actions:supersed;\n" +
				"config:hasEffectOnCode default:actionSupersed1 ;\n" +
				".\n" +
				"default:actionSupersed1 a core:actionRole;\n	" +
				"config:preThis	states:valid;\n" +
				"config:preAux	states:valid;\n" +
				"config:postThis	states:superseded;\n" +
				"config:postAux	states:valid;\n" +
				".\n" +
				"actions:add	a core:action;\n" +
				"rdfs:label	\"Add\"@en;\n" +
				"config:overrides actions:add;\n" +
				"config:hasEffectOnReg default:addAction1 ;\n" +
				"config:hasEffectOnReg default:addAction2 ;\n" +
				".\n" +
				"default:addAction1 a core:actionRole;\n" +
				"config:preThis	states:valid;\n" +
				"config:postThis	states:default;	\n" +
				".\n" +
				"default:addAction2 a core:actionRole;\n" +
				"config:preThis	states:default;\n" +
				"config:postThis	states:default;	\n" +
				".\n" +
				"actions:validate	a core:action;\n" +
				"rdfs:label	\"Validate\"@en;	\n" +
				"config:hasEffectOnReg default:validateAction1 ;\n" +
				"config:hasEffectOnCode default:validateAction1 ;\n" +
				".\n" +
				"default:validateAction1  a core:actionRole; \n" +
				"config:preThis states:default;\n" +
				"config:postThis states:valid;\n" +
				".\n" +
				"states:obsoleted a core:state;\n" +
				"rdfs:label	\"Obsoleted\"@en;\n" +
				"config:overrides states:obsoleted;\n" +
				".\n" +
				"states:superseded a core:state;\n" +
				"rdfs:label	\"Superseded\"@en;\n" +
				"config:overrides states:superseded;\n" +
				".\n" +
				"states:default a core:state;\n" +
				"rdfs:label	\"Default\"@en;\n" +
				"config:overrides states:default;\n" +
				".\n" +
				"states:valid a core:state;\n" +
				"rdfs:label	\"Valid\"@en;\n" +
				".\n";

		createFileAndFillWithString(confDirAbsoluteString,"defaultProcessConfig.ttl",defProcessStatements);
	
		String validationStatements=
				"@prefix core:		<http://metarelate.net/core/types/>	.\n"+
				"@prefix states: 	<http://metarelate.net/core/states/> .\n"+
				"@prefix actions:	<http://metarelate.net/core/actions/> .\n"+
				"@prefix config:		<http://metarelate.net/core/config/> .\n"+
				"@prefix default:	<http://metarelate.net/default/config/> .\n"+
				"@prefix rdfs:		<http://www.w3.org/2000/01/rdf-schema#> .\n"+
				"@prefix rdf:		<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"+
				"@prefix skos:		<http://www.w3.org/2004/02/skos/core#> .\n"+
				"\n"+
				"default:typesXRegisters a config:RegisterValidationConstraint;\n"+
				"	config:onObjectProperty	rdf:type; \n"+
				"	config:pseudoOrder 	\"A\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:oneOf	skos:Collection;\n"+
				"	config:oneOf	skos:Scheme;\n"+
				"	config:oneOf	default:GenericCollection;\n"+
				"	.\n"+
				"	\n"+
				"default:labelXRegisters a config:RegisterValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:label; \n"+
				"	config:pseudoOrder 	\"B\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"en\";\n"+
				"	.\n"+
				"	\n"+
				"default:notationXRegisters a config:RegisterValidationConstraint;\n"+
				"	config:onDataProperty	skos:notation; \n"+
				"	config:pseudoOrder 	\"C\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:type			config:String;\n"+
				"	.	\n"+
				"	\n"+
				"default:descriptionXRegistersEN a config:RegisterValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:description; \n"+
				"	config:pseudoOrder 	\"D\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"en\";\n"+
				"	.\n"+
				"	\n"+
				"default:descriptionXRegistersIT a config:RegisterValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:description; \n"+
				"	config:pseudoOrder 	\"E\";\n"+
				"	config:minCardinality	\"0\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"it\";\n"+
				"	.\n"+
				" \n"+
				"default:typesXCodes a config:CodeValidationConstraint;\n"+
				"	config:onObjectProperty	rdf:type; \n"+
				"	config:pseudoOrder 	\"A\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:oneOf	skos:Concept;\n"+
				"	config:oneOf	default:GenericConcept;\n"+
				"	.\n"+
				" \n"+
				"default:labelXCodes a config:CodeValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:label; \n"+
				"	config:pseudoOrder 	\"B\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"en\";\n"+
				"	.\n"+
				"	\n"+
				"default:notationXCodes a config:CodeValidationConstraint;\n"+
				"	config:onDataProperty	skos:notation; \n"+
				"	config:pseudoOrder 	\"C\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:type			config:String;\n"+
				"	.\n"+
				"	\n"+
				"default:descriptionXCodesEN a config:CodeValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:description; \n"+
				"	config:pseudoOrder 	\"D\";\n"+
				"	config:minCardinality	\"1\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"en\";\n"+
				"	.	\n"+
				"	\n"+
				"default:descriptionXCodesIT a config:CodeValidationConstraint;\n"+
				"	config:onDataProperty	rdfs:description; \n"+
				"	config:pseudoOrder 	\"E\";\n"+
				"	config:minCardinality	\"0\";\n"+
				"	config:maxCardinality	\"1\";\n"+
				"	config:language			\"it\";\n"+
				"	.	\n";
				
				
			
		createFileAndFillWithString(confDirAbsoluteString,"defaultValidationRules.ttl",validationStatements);
	}
	

	
	public String getDefaultUserURI() {
		//TODO this may need to be made machine independent
		return "http://"+instanceIdentifier+"/"+defaultUserName;
	}

	private void createFileAndFillWithString(String dir, String fileName, String content) throws ConfigurationException {
		//File confDir=new File(dir);
		//if(confDir.listFiles().length==0) {
			//We need at least to specify a default TDB.

			try {
				BufferedWriter defFile=new BufferedWriter(new FileWriter(new File(dir,fileName).getAbsolutePath()));
				defFile.write(content);
				defFile.flush(); // TODO Is this implied by close() ?
				defFile.close();
			} catch (IOException e) {
				throw new ConfigurationException("Unable to initialize configuration file: "+fileName+" in "+confDirAbsoluteString);
			}
			
		//}
		
	}
	
	private void writeInFile(File file, String content) throws ConfigurationException  {
		BufferedWriter defFile;
		try {
			defFile = new BufferedWriter(new FileWriter(file));
			defFile.write(content);
			defFile.flush(); // TODO Is this implied by close() ?
			defFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ConfigurationException("Unable to write to : "+file.getAbsolutePath());
			
		}
		
		
	}
	
	// TODO we should cache this!!!
	/**
	 * The configuration graph is the union of all graphs in the configuration dir.
	 * @throws ConfigurationException 
	 */
	public Model getConfigurationGraph() throws ConfigurationException {
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

	public String getServerName() {
		return serverName;
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
	
	
	


}
