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
 * TODO should eventually re-implement what is now in @net.metarelate.terminology.commandline.MetOp
 * This is currently only a stub class.
 */
package net.metarelate.terminology.commandline;

import net.metarelate.terminology.instanceManager.Initializer;

public class OLD_CommandCommand extends TsCommand {

	public OLD_CommandCommand(String sysDir,String[] args) {
		super(sysDir,args);
		for(String arg:args) if(arg.equals("help")) {
			System.out.println(getStaticLocalHelpMessage());
			return;
		}
	}

	@Override
	public void localExecute() {
		// TODO Nothing to do here yet

	}
	
	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	
	public static String getStaticLocalHelpMessage() {
		return "This method is not implemented yet. Please come back later!";
		
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public String getName() {
		return "generic command (unimplemented)";
	}
	
	

}
