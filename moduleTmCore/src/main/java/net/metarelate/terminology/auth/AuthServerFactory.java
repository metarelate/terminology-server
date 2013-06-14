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

package net.metarelate.terminology.auth;


import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
/**
 * Creates an Authorization Server (currently from a file only).
 * @author andrea_splendiani
 *
 */
public class AuthServerFactory {
	/**
	 * Generates an Authorization Server from a file (in this case, this will be a
	 * @see AuthServerFileBased .
	 * @param config A model containing all configuration files which the authority server should be based on 
	 * (this include actual statements on who is authorized to perform which action
	 * on which register, with possible "wildcards").
	 * @return An authorization server
	 * @throws ConfigurationException
	 */
	public static AuthServer createServerFromConfig(Model config) throws ConfigurationException {
		AuthServer myServer=null;
		Resource authType=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleResource(config,AuthConfig.authConfigProperty );
		if(authType.getURI().equals(AuthConfig.isConfigFileString)) {
			
		}
		else throw new ConfigurationException("Unable to determine a valid (and supported) configuration modality");
		myServer=new AuthServerFileBased(Initializer.getAuthFiles());
		
		return myServer;
		
	}

}
