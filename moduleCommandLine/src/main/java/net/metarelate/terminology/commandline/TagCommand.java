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

package net.metarelate.terminology.commandline;

import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

public class TagCommand extends TsCommand {
	String tag=null;
	String message="";
	public TagCommand(String sysDir,String[] args) {
		super(sysDir,args);
		
	
		boolean nextIsTag=false;
		
		for(String arg:args) {
			if(arg.equalsIgnoreCase("-t")) {
				nextIsTag=true;
			}
			else if(nextIsTag) {
				tag=arg;
				nextIsTag=false;
			}
			
		}
	}



	@Override
	public void localExecute() throws ModelException {
		
		String authorURI=myInitializer.getDefaultUserURI();
		try {
			myInitializer.myTerminologyManager.tagRelease(authorURI, tag, message);
		} catch (AuthException e) {
			Loggers.commandLogger.fatal("Sorry, could not do it.");
			e.printStackTrace();
		}
	}
	
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	
	public static String getStaticLocalHelpMessage() {
		return 	"ts tag -t tag -m message\n"+
				"  tag the current state of the terminology server\n"+
				"  -t tag: required\n"+
				"  -t message: optional ";
	}



	@Override
	public boolean validate() {
		if(tag==null) return false;
		else return true;
	}



	@Override
	public String getName() {
		return "tag";
	}

}
