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

public abstract class TsCommand {
	public Initializer myInitializer=null;
	protected boolean debugOn=false;
	protected String message="";
	public TsCommand(Initializer myInitializer,String[] args,boolean debug) {
		super();
		this.myInitializer = myInitializer;
		boolean isMessage=false;
		this.debugOn=debug;
		for(String arg : args) {
			/*
			if(arg.equalsIgnoreCase("-d") || arg.equalsIgnoreCase("-debug")) {
				debugOn=true;
				SSLogger.showDebug(true);
			}
			*/
			if(arg.equalsIgnoreCase("-m") || arg.equalsIgnoreCase("-message")) {
				isMessage=true;
			}
			else if(isMessage==true) {
				message=arg;
				isMessage=false;
			}
			
		}
	}

	
	
	public void execute() throws Exception {
		if(!validate()) {
			
			System.out.println("Wrong usage:");
			System.out.println(getLocalHelpMessage());
		}
		else {
			localExecute();
			myInitializer.myFactory.synch();
		}
	}
	public abstract void localExecute() throws ModelException, UnknownURIException, ConfigurationException, WebWriterException, IOException, Exception;
	public abstract boolean validate();
	//No abstract static in Java! Must be a design error...
	
	public abstract String getLocalHelpMessage();
	
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
