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

import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
/**
 * Manages permissions to operate on the Registry Manager.
 * 
 * @author andreasplendiani
 *
 */
public  class AuthRegistryManager {
	AuthServer myAuthServer=null;
	TerminologyFactory myFactory=null;
	public AuthRegistryManager(AuthServer myAuthServer,TerminologyFactory myFactory) {
		this.myAuthServer = myAuthServer;
		this.myFactory=myFactory;
	}

	/**
	 * Answers whether a given "agent" is authorized to perform a given "action" on the target entity, that could be a register or a code.
	 * Unspecified constraints (null values) are considered as all. e.g.: agent-null-target would return whether the agent is allowed all operations on the target.
	 * If no authorization directives are provided by the AuthManager, the request is 
	 * 
	 * @param agent the URI of the agent performing the action. Through the code the agent is also referred to as "actor" or "actorURI". A null values stands for all agents.
	 * @param action the URI of the action. A null value stands for all actions.
	 * @param entity the URI of the target entity. A null value stands for all targets.
	 * @param server the authority server (that follows an Open World assumption)
	 * @param factory (the terminology factory). This knows about the containment of registers.
	 * @return true if the operation is authorized, false otherwise.
	 * @throws ModelException 
	 */
	public boolean can(String agent,
			String action, String entity) throws RegistryAccessException, ModelException {
		System.out.println("Asking auth for: "+agent+" "+action+" "+entity);
		if(agent==null) agent=AuthConfig.allURI;
		if(action==null) action=AuthConfig.allURI;
		if(entity==null) entity=AuthConfig.allURI;
		
		if(myAuthServer.contains(agent,action,entity) ||
				myAuthServer.contains(agent,action,AuthConfig.allURI)	||
				myAuthServer.contains(agent,AuthConfig.allURI,entity)	||
				myAuthServer.contains(agent,AuthConfig.allURI,AuthConfig.allURI)	||
				myAuthServer.contains(AuthConfig.allURI,action,entity)	||
				myAuthServer.contains(AuthConfig.allURI,action,AuthConfig.allURI)	||
				myAuthServer.contains(AuthConfig.allURI,AuthConfig.allURI,entity)	||
				myAuthServer.contains(AuthConfig.allURI,AuthConfig.allURI,AuthConfig.allURI)	
				
				) {
			
			System.out.println("Granted");
			return true;
		
		}
		else {
			if(entity.equals(AuthConfig.allURI))
				return false;
			boolean answer=false;
			Set<TerminologySet> containers;
			if(myFactory.terminologyIndividualExist(entity)) {
				TerminologyIndividual myInd=myFactory.getUncheckedTerminologyIndividual(entity);
				containers=myInd.getContainers(myInd.getLastVersion());
			}
			else if(myFactory.terminologySetExist(entity)) {
				TerminologySet mySet=myFactory.getUncheckedTerminologySet(entity);
				containers=mySet.getContainers(mySet.getLastVersion());
			}
			else throw new RegistryAccessException("Unknown: "+entity);
			Iterator<TerminologySet> contIter=containers.iterator();
			while(contIter.hasNext()) {
				TerminologySet parent=contIter.next();
				System.out.println("Asking up: "+parent.getURI());
				answer=answer || can(agent,action,parent.getURI());
			}
			return answer;
		}
		
		
		

	
	}
}
