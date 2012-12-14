package net.metarelate.terminology.auth;

import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.AuthException;

/**
 * Manages permissions to operate on the Registry Manager.
 * 
 * @author andreasplendiani
 *
 */
public abstract class AuthRegistryManager {
	
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
	 */
	public static boolean can(String agent,
			String action, String entity,AuthServer server,TerminologyFactory factory) throws AuthException {
		if(agent==null) agent=AuthConfig.allURI;
		if(action==null) action=AuthConfig.allURI;
		if(entity==null) entity=AuthConfig.allURI;
		
		if(server.contains(agent,action,entity) ||
			server.contains(agent,action,AuthConfig.allURI)	||
			server.contains(agent,AuthConfig.allURI,entity)	||
			server.contains(agent,AuthConfig.allURI,AuthConfig.allURI)	||
			server.contains(AuthConfig.allURI,action,entity)	||
			server.contains(AuthConfig.allURI,action,AuthConfig.allURI)	||
			server.contains(AuthConfig.allURI,AuthConfig.allURI,entity)	||
			server.contains(AuthConfig.allURI,AuthConfig.allURI,AuthConfig.allURI)	
				) return true;
		else {
			if(entity.equals(AuthConfig.allURI))
				return false;
			boolean answer=false;
			Set<TerminologySet> containers;
			if(factory.terminologyIndividualExist(entity)) {
				TerminologyIndividual myInd=factory.getOrCreateTerminologyIndividual(entity);
				containers=myInd.getContainers(myInd.getLastVersion());
			}
			else if(factory.terminologySetExist(entity)) {
				TerminologySet mySet=factory.getOrCreateTerminologySet(entity);
				containers=mySet.getContainers(mySet.getLastVersion());
			}
			else throw new AuthException("Unknown: "+entity);
			Iterator<TerminologySet> contIter=containers.iterator();
			while(contIter.hasNext()) {
				answer=answer && can(agent,action,contIter.next().getURI(),server,factory);
			}
		}
		return false;
		
		
		
	}
}
