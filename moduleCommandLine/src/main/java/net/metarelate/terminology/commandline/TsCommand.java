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
 * TODO check System.out.println usage
 * TODO clean usage vs @net.metarelate.terminology.ts negative validate should prevent init() !
 */
package net.metarelate.terminology.commandline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;
/**
 * A super-class for command line commands
 * @author andrea_splendiani
 *
 */
public abstract class TsCommand {
	protected Initializer myInitializer=null;
	protected String nextIsMessage="";		// TODO to move here from other commands, and check it is properly implemented
	protected String sysDir=null;
	
	/**
	 * Constructor
	 * @param sysDir the system directory
	 * @param args arguments to the command line
	 */
	public TsCommand(String sysDir,String[] args) {
		super();
		this.sysDir=sysDir;
		
		
		//hre we have some other bits of parsing common to all commands.
		boolean isMessage=false;
		for(String arg : args) {
			if(arg.equalsIgnoreCase("-m") || arg.equalsIgnoreCase("-message")) {
				isMessage=true;
			}
			else if(isMessage==true) {
				nextIsMessage=arg;
				isMessage=false;
			}
			
		}
	}

	
	/**
	 * Execute the command (checks if the right parameters are provided, then initalizes the system and run the command "content")
	 * @throws Exception
	 */
	public void execute() throws Exception {
		if(!validate()) {
			Loggers.commandLogger.debug("Validation failed at second step. Should have failed first!");
			System.out.println("Wrong usage :");
			System.out.println(getLocalHelpMessage());
			
		}
		else {
			buildInitializer();
			localExecute();
			myInitializer.myFactory.synch();
		}
	}
	
	private void buildInitializer() {
		Loggers.commandLogger.info("Initializing system");
		try {
			if(sysDir!=null) {
				myInitializer=new Initializer(sysDir);
			}
			else {
				myInitializer=new Initializer();
			}
		} catch (ConfigurationException e) {
				Loggers.commandLogger.fatal("Unable to initialize the system");
				e.printStackTrace();
				System.exit(-1);
		}
		
	}

	/**
	 * Executes the "actual" content of the command. This method needs to be implemented by command with their own execution code.
	 * @throws ModelException
	 * @throws UnknownURIException
	 * @throws ConfigurationException
	 * @throws WebWriterException
	 * @throws IOException
	 * @throws Exception
	 */
	public abstract void localExecute() throws ModelException, UnknownURIException, ConfigurationException, WebWriterException, IOException, Exception;
	
	/**
	 * @return true if parameters are correct and systems initalization can start
	 */
	public abstract boolean validate();
	
	/**
	 * @return the command specific help message
	 */
	public abstract String getLocalHelpMessage();
	
	/**
	 * 
	 * @return a name for this command (for displaying purposes only)
	 */
	public abstract String getName();
	
	protected Model readIntoModel(ArrayList<String> files) {
		Model inputModel=ModelFactory.createDefaultModel();
		for(String fileName:files) {
			File file = new File(fileName);
			FileInputStream fileInput=null;
			try {
				if(file.isAbsolute()) {
					fileInput=new FileInputStream(fileName);
					System.out.println("Reading absolute file: "+fileName);
				}
				else {
					File newFile=new File(myInitializer.getWorkingDirectory(),fileName);
					System.out.println("Reading (ex relative) file: "+newFile.getAbsolutePath());
					fileInput=new FileInputStream(newFile.getAbsolutePath());
				}
			}
			catch (Exception e) {
				System.out.println("Unable to find file: "+fileName);
				System.out.println("Working dir is: "+myInitializer.getWorkingDirectory());
				System.exit(-1);
			}
			
			if(fileName.endsWith("ttl")) {
				System.out.println("Turtle");
				inputModel.read(fileInput,"http://bogus.net/","Turtle");
			}
			if(fileName.endsWith("rdf")) {
				inputModel.read(fileInput,"http://bogus.net/","RDF/XML");
			}
			if(fileName.endsWith("owl")) {
				inputModel.read(fileInput,"http://bogus.net/","RDF/XML");
			}
		}
		return inputModel;
	}
	
	
}
