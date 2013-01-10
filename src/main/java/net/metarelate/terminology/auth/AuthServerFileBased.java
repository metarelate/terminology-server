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

package net.metarelate.terminology.auth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.metarelate.terminology.exceptions.ConfigurationException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class AuthServerFileBased extends AuthServer {
	private Model myModel=null;


	public AuthServerFileBased(File[] authFiles) throws ConfigurationException {
		myModel=ModelFactory.createDefaultModel();
		for(int i=0;i<authFiles.length;i++) {
			try {
				myModel.read(new FileInputStream(authFiles[i]),"http://thisInstance.org/auth/","Turtle");
			} catch (FileNotFoundException e) {
				throw new ConfigurationException("Unable to read auth configuration file: "+authFiles[i].getAbsolutePath()+" Note that this file should be written in Turtle syntax");
			}
		}
	}

	@Override
	public boolean contains(String agent, String action, String entity) {
		if(myModel.contains(ResourceFactory.createResource(agent),ResourceFactory.createProperty(action),ResourceFactory.createResource(entity)))
			return true;
		else return false;
	}

	//TODO note that this is in memory, if something changes permissions, we need synch/flush methods.
}
