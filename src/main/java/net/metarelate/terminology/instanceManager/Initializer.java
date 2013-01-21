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

package net.metarelate.terminology.instanceManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.metarelate.terminology.auth.AuthConfig;
import net.metarelate.terminology.auth.AuthServer;
import net.metarelate.terminology.auth.AuthServerFactory;
import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
	public static String defaultUserName=null;
	
	public static AuthServer authServer=null;
	
	public Initializer(String[] args) throws ConfigurationException {
		// Process parameters
		construct();
	}
	
	public Initializer() throws ConfigurationException {
		construct();
	}
	//TODO Inits could be made more modular
	public void construct() throws ConfigurationException {
		if(userHomeString==null) userHomeString = System.getProperty( "user.home" );
		SSLogger.log("User Home: "+ userHomeString);
		System.out.println("User Home: "+ userHomeString);
		File rootDirectory=new File(userHomeString,CoreConfig.rootDirString);
		checkOrCreateDirectory(rootDirectory);
		// Note: we don't allow overriding of host or server name. This may be changed.
		try {
			InetAddress myAddress=InetAddress.getLocalHost();
			serverAddress=myAddress.getHostAddress();
			serverName=myAddress.getHostName();
			defaultUserName=System.clearProperty("user.name");
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
		initializeConfFiles();
		
		// We need to create AuthManager (eventually taking care of connections).
		authServer=AuthServerFactory.createServerFromConfig(getConfigurationGraph());
		
		
		// TODO Perhaps we should look into an initialization sequence
		// TODO Note also that we should be sure time is in synch globally
	}
	

	
	
	
	private void checkOrCreateDirectory (File dir) throws ConfigurationException  {
		if(dir.exists()) {
			if(!dir.isDirectory()) throw new ConfigurationException("Unable to create directory "+dir+" as a file with the same path already exists");
		}
		else dir.mkdir();
		
	}
	
	private void initializeConfFiles() throws ConfigurationException {		
		String defServerStatements="<http://thisInstance.org> <"+MetaLanguage.tdbPrefixProperty+"> "+"\""+dbDirAbsoluteString+"\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n.";
		defServerStatements+="<http://thisInstance.org> <"+MetaLanguage.authConfigURI +"> "+"<"+AuthConfig.isConfigFileString+"> ;\n.";
		createFileAndFillWithString(confDirAbsoluteString,"defaultServerConfig.ttl",defServerStatements);
		
		// TODO this is : me what I can on what. Arguably this should start with me can create anything at the top register (empty register?)
		// However, as a conf option, one could be granted access to everything.
		String defAuthStatements="<"+getDefaultUserURI()+"> <"+AuthConfig.allURI+"> "+"<"+AuthConfig.allURI+"> ;\n.";
		createFileAndFillWithString(authDirAbsoluteString,"defaultAuthConfig.ttl",defAuthStatements);
	}
	

	
	public String getDefaultUserURI() {
		//TODO this may need to be made machine independent
		return "http://"+serverName+"/"+defaultUserName;
	}

	private void createFileAndFillWithString(String dir, String fileName, String content) throws ConfigurationException {
		File confDir=new File(dir);
		if(confDir.listFiles().length==0) {
			//We need at least to specify a default TDB.

			try {
				BufferedWriter defFile=new BufferedWriter(new FileWriter(new File(dir,fileName).getAbsolutePath()));
				defFile.write(content);
				defFile.flush(); // TODO Is this implied by close() ?
				defFile.close();
			} catch (IOException e) {
				throw new ConfigurationException("Unable to initialize configuration file: "+fileName+" in "+confDirAbsoluteString);
			}
			
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

	
	
	
	


}
