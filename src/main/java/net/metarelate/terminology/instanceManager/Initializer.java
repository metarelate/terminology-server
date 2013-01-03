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
import java.io.InputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.utils.SSLogger;

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
	
	public Initializer(String[] args) throws ConfigurationException {
		// Process parameters
		construct();
	}
	
	public Initializer() throws ConfigurationException {
		construct();
	}
	
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
			serverName=myAddress.getCanonicalHostName();
		} catch (UnknownHostException e) {
			serverAddress="Unknown";
			serverName="Unknown";
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
		initializeConfFiles();
	}
	

	
	
	
	private void checkOrCreateDirectory (File dir) throws  ConfigurationException  {
		if(dir.exists()) {
			if(!dir.isDirectory()) throw new ConfigurationException("Unable to create directory "+dir+" as a file with the same path already exists");
		}
		else dir.mkdir();
		
	}
	
	private void initializeConfFiles() throws ConfigurationException {
		File confDir=new File(confDirAbsoluteString);
		if(confDir.listFiles().length==0) {
			//We need at least to specify a default TDB.
			String defStatement="<http://thisInstance.org> <"+MetaLanguage.tdbPrefixProperty+"> "+"\""+dbDirAbsoluteString+"\"^^<http://www.w3.org/2001/XMLSchema#string> ;\n.";

			try {
				BufferedWriter defFile=new BufferedWriter(new FileWriter(new File(confDirAbsoluteString,"defaultConf.ttl").getAbsolutePath()));
				defFile.write(defStatement);
				defFile.flush(); // TODO Is this implied by close() ?
				defFile.close();
			} catch (IOException e) {
				throw new ConfigurationException("Unable to initialize configuration file: defaultConf.ttl in "+confDirAbsoluteString);
			}
			
		}
		
	}
	
	
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

	
	
	
	


}
