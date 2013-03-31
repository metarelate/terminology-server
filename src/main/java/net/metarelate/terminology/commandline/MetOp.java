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
	
package net.metarelate.terminology.commandline;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import net.metarelate.terminology.auth.AuthRegistryManager;
import net.metarelate.terminology.auth.AuthServer;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.management.TerminologyManager;
import net.metarelate.terminology.utils.SSLogger;

public class MetOp extends CommandLineTool {
	private static final int ACTION_UPDATE_ADD=1;
	private static final int ACTION_UPDATE_REPLACE=2;
	private static final int ACTION_UPDATE_REMOVE=3;
	private static final int ACTION_REG_ADD_TERM=4;
	private static final int ACTION_REG_DEL_TERM=5;
	private static final int ACTION_REG_SUP_TERM=6;
	private static final int ACTION_REG_TAG = 10;
	private static final int ACTION_VALIDATE = 20;
	private static final int ACTION_INVALIDATE = 21;
	
	private int myAction=0;
	
	private String actionAuthorURI;
	private String description="";
	private String dataContentFile="";
	private String termURI="";	//also used when a register is updated
	private String regURI="";
	private String tag="";
	private String superseedingTermURI="";
	
	public static void main(String[] args) {
		MetOp myMetOp=new MetOp();
		myMetOp.startCommand(args);
	}
	
	
	@Override
	protected void printStartMessage() {
		System.out.println("Welcome to MetOp!");

	}

	@Override
	protected void parseLocal(String[] args) {
		int indexOfA=-1;
		int indexOfO=-1;
		int indexOfD=-1;
		int indexOfT=-1;
		int indexOfR=-1;
		int indexOfData=-1;
		int indexOfTag=-1;
		int indexOfSup=-1;
	
		
		for(int i=0;i<args.length;i++) {
			if(args[i].equals("-o")) { 		//Actor
				indexOfO=i;
			}
			if(args[i].equals("-d")) {		//Description
				indexOfD=i;
			}
			if(args[i].equals("-data")) {	//statements
				indexOfData=i;
			}
			if(args[i].equals("-op")) {		//
				indexOfA=i;
			}
			if(args[i].equals("-t")) {		//term
				indexOfT=i;
			}
			if(args[i].equals("-r")) {		//term
				indexOfR=i;
			}
			if(args[i].equals("-sup")) {		//term
				indexOfSup=i;
			}
			if(args[i].equals("-tag")) {		//term
				indexOfTag=i;
			}
			
		}
		if(indexOfSup>0) {
			superseedingTermURI=args[indexOfSup+1];
		}
		
		if(indexOfD>0) {
			description=args[indexOfD+1];
			SSLogger.log("Got Description: "+description,SSLogger.DEBUG);
		}
		else
			description=null;
		if(indexOfTag>0) {
			tag=args[indexOfTag+1];
			SSLogger.log("Got Tag: "+tag,SSLogger.DEBUG);
		}
		else tag=null;
		
		if(indexOfT>0) {
			termURI=args[indexOfT+1];
			SSLogger.log("Got term URI: "+termURI,SSLogger.DEBUG);
		}	
		else {
			System.out.println("No term specified");
			exitWrongUsage("");
		}
		
		if(indexOfO>=0) {
			actionAuthorURI=args[indexOfO+1];
			SSLogger.log("Got actor: "+actionAuthorURI,SSLogger.DEBUG);
		}
		else {
			System.out.println("Anonymous operations are not permitted");
			exitWrongUsage("");
		}
		if(indexOfA>0) {
			String opString=args[indexOfA+1];
			if(opString.equals("addstats"))
				myAction=ACTION_UPDATE_ADD;
			else if(opString.equals("removestats")) 
				myAction=ACTION_UPDATE_REMOVE;
			else if(opString.equals("replacestats")) 
				myAction=ACTION_UPDATE_REPLACE;
			else if (opString.equals("add"))
				myAction=ACTION_REG_ADD_TERM;	
			else if (opString.equals("tag"))
				myAction=ACTION_REG_TAG;	
			else if (opString.equals("del"))
				myAction=ACTION_REG_DEL_TERM;	
			else if (opString.equals("superseed"))
				myAction=ACTION_REG_SUP_TERM;	
			else if (opString.equals("validate"))
				myAction=ACTION_VALIDATE;	
			else if (opString.equals("invalidate"))
				myAction=ACTION_INVALIDATE;	
			else {
				System.out.println("No operation specified");
				exitWrongUsage("");
			}
		}
		if(indexOfData>0) 
			dataContentFile=args[indexOfData+1];

		
		if(indexOfR>0) {
			regURI=args[indexOfR+1];
		}
	}

	@Override
	protected void executeCommand() throws InvalidProcessException {
		// TODO this is only a stub
		TerminologyManager myManager=myInitializer.myTerminologyManager;
		if(dataContentFile==null) {
			System.out.println("No data specified");
			exitWrongUsage("");
		}
		if(myAction==ACTION_UPDATE_ADD || myAction==ACTION_UPDATE_REMOVE || myAction==ACTION_UPDATE_REPLACE) {
			System.out.println("Updating term: "+termURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			System.out.println("New data located at: "+dataContentFile);
			System.out.print("Mode of operation :");
			if(myAction==ACTION_UPDATE_ADD) System.out.println("ADD");
			if(myAction==ACTION_UPDATE_REMOVE) System.out.println("DEL");
			if(myAction==ACTION_UPDATE_REPLACE) System.out.println("REP");
			
			Model myTempModel=ModelFactory.createDefaultModel();
			myTempModel.read(dataContentFile,"TTL");
			try {
				if(myAction==ACTION_UPDATE_ADD) myManager.addToEntityInformation(termURI, myTempModel, actionAuthorURI, description);
				if(myAction==ACTION_UPDATE_REMOVE) myManager.removeFromEntityInformation(termURI, myTempModel, actionAuthorURI, description);
				if(myAction==ACTION_UPDATE_REPLACE) myManager.replaceEntityInformation(termURI, myTempModel, actionAuthorURI, description);
			
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			} catch (ModelException e) {
				System.out.println("Model error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}

		if(myAction==ACTION_REG_ADD_TERM) {
			System.out.println("Adding term: "+termURI);
			System.out.println("To register: "+regURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			System.out.println("New data located at: "+dataContentFile);
			
			Model myTempModel=ModelFactory.createDefaultModel();
			myTempModel.read(dataContentFile,"TTL");
			try {
				myManager.addTermToRegister(termURI, regURI, myTempModel, actionAuthorURI, description, true);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			} catch (ModelException e) {
				System.out.println("Model error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (ImporterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(myAction==ACTION_REG_TAG) {
			System.out.println("Tagging");
			if(tag==null) {
				System.out.println("Must indicate a tag!");
				exitWrongUsage("");
			}
			
			System.out.println("Tag: "+tag);
	
			try {
				myManager.tagRelease(actionAuthorURI,tag,description);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (ModelException e) {
				System.out.println("Model error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		// Del
		if(myAction==ACTION_REG_DEL_TERM) {
			System.out.println("Deleting term: "+termURI);
			System.out.println("From register: "+regURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			
			try {
				myManager.delTermFromRegister(termURI, regURI, actionAuthorURI, description);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			} catch (ModelException e) {
				System.out.println("Model error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		// Superseed
		if(myAction==ACTION_REG_SUP_TERM) {
			System.out.println("Superseeding term: "+termURI);
			System.out.println("with term: "+superseedingTermURI);
			System.out.println("In register: "+regURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			
		
			try {
				myManager.superseedTermInRegister(termURI, superseedingTermURI,regURI, actionAuthorURI, description);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			} catch (ModelException e) {
				System.out.println("Model error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		
		if(myAction==ACTION_VALIDATE) {
			System.out.println("Validating term or register: "+termURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			/*	
			try {
				myManager.validate(termURI, actionAuthorURI, description, true);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			}
			*/
		
		}
		
		if(myAction==ACTION_INVALIDATE) {
			System.out.println("Invalidating term or register: "+termURI);
			System.out.println("Command issued from: "+actionAuthorURI);
			System.out.println("description: "+description);
			/*		
			try {
				myManager.validate(termURI, actionAuthorURI, description, false);
			} catch (AuthException e) {
				System.out.println("Auth error");
				System.out.println(e.getLocalizedMessage());
				e.printStackTrace();
			} catch (RegistryAccessException e) {
				System.out.println("Registry error");
				System.out.println(e.getLocalizedMessage());
			}
			*/
		}
		// TODO Auto-generated method stub

	}

	@Override
	protected void printLocalHelp() {
		System.out.println("Local help is unavailable");
		// TODO Auto-generated method stub

	}

}
