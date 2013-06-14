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

package net.metarelate.terminology.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

//TODO visibility of most methods could be package-restricted
/**
 * Given a process configuration, checks if some actions is valid for the current state
 * and compute result states.
 * 
 * @author andrea_splendiani
 *
 */
public class RegistryPolicyManager {
	final static int PRE_THIS=0;
	final static int PRE_UP=1;
	final static int PRE_DOWN=2;
	final static int PRE_AUX=3;
	final static int POST_THIS=4;
	final static int POST_UP=5;
	final static int POST_DOWN=6;
	final static int POST_AUX=7;
	
	//TODO Remove from MetaLanguage
	//public static final String actionUpdateURI=MetaLanguage.terminologyUpdateAction.getURI();
	//public static  String addAction = MetaLanguage.terminologyAddItemAction.getURI();
	//public static  String obsoleteAction = MetaLanguage.terminologyDelItemAction.getURI();
	//public static  String supersedAction = MetaLanguage.terminologySupersedAction.getURI();
	
	//public static final String defaultState = MetaLanguage.statusWMOValidationResource.getURI();
	//public static final String obsoletedState = MetaLanguage.statusWMOValidationResource.getURI();
	//public static final String supersededState = MetaLanguage.statusWMOValidationResource.getURI();
	
	public static  String actionUpdateURI=TerminologyManagerConfig.updateActionURI;
	public static  String actionAddURI = TerminologyManagerConfig.addActionURI;
	public static  String actionObsoleteURI = TerminologyManagerConfig.obsoleteActionURI;
	public static  String actionSupersedURI = TerminologyManagerConfig.supersedActionURI;
	
	public static  String stateDefaultURI = TerminologyManagerConfig.defaultStateURI;
	public static  String stateObsoleteURI = TerminologyManagerConfig.obsoletedStateURI;	//TODO note that we not need to hardwire this
	public static  String stateSupersedURI = TerminologyManagerConfig.supersededStateURI;	//TODO note that we not need to hardwire this
	
	//TODO Remove (and remove from MetaLanguage)
	//public static final String validateAction=MetaLanguage.terminologyValidateAction.getURI();
	//public static final String invalidateAction=MetaLanguage.terminologyInvalidateAction.getURI();
	
	
	//TODO Remove (and remove from MetaLanguage)
	//static final String illegalState = MetaLanguage.statusIllegalResource.getURI();
	//public static final String nullState = MetaLanguage.statusNullResource.getURI();
	
	
	//TODO Action without state transition! (but with permissions... where should it go?)
	public static String tagAction=TerminologyManagerConfig.tagAction.getURI();
	
	
	
	private String[] allActions=null;
	private String[] extraActions=null;
	private String[] allStates=null;
	private String[] extraStates=null;
	
	private Map<String,ArrayList<String[]>> registerTransitions=new Hashtable<String,ArrayList<String[]>>();
	private Map<String,ArrayList<String[]>> codeTransitions=new Hashtable<String,ArrayList<String[]>>();

	
	/**
	 * Constructor
	 * @param allConfig the global configuration file
	 */
	public RegistryPolicyManager(Model allConfig) {
		Loggers.policyLogger.debug("Looking for built in actions and transitions");
		Set<Resource> actions=new HashSet<Resource>();
		Set<Resource> states=new HashSet<Resource>();
		/*
		 * Looking for actions
		 */
		StmtIterator statsIterator=allConfig.listStatements(null,MetaLanguage.typeProperty,ResourceFactory.createResource(TerminologyManagerConfig.confActionType));
		while(statsIterator.hasNext()) {
			Statement currentStat=statsIterator.nextStatement();
			Resource currentAction=currentStat.getSubject();
			actions.add(currentAction);
			Resource overridden=SimpleQueriesProcessor.getOptionalResourceObject(currentAction, ResourceFactory.createProperty(TerminologyManagerConfig.confOverrides), allConfig);
			if(overridden!=null) {
				if(overridden.getURI().equals(actionUpdateURI)) actionUpdateURI=overridden.getURI();
				if(overridden.getURI().equals(actionObsoleteURI)) actionObsoleteURI=overridden.getURI();
				if(overridden.getURI().equals(actionSupersedURI)) actionSupersedURI=overridden.getURI();
				if(overridden.getURI().equals(actionAddURI)) actionAddURI=overridden.getURI();
			}
		}
		if(!actions.contains(ResourceFactory.createResource(actionUpdateURI))) actions.add(ResourceFactory.createResource(actionUpdateURI));
		if(!actions.contains(ResourceFactory.createResource(actionObsoleteURI))) actions.add(ResourceFactory.createResource(actionObsoleteURI));
		if(!actions.contains(ResourceFactory.createResource(actionSupersedURI))) actions.add(ResourceFactory.createResource(actionSupersedURI));
		if(!actions.contains(ResourceFactory.createResource(actionAddURI))) actions.add(ResourceFactory.createResource(actionAddURI));
		
		allActions=new String[actions.size()];
		int i=0;
		for (Resource act:actions) {
			allActions[i++]=act.getURI();
		}
		
		for(String actionString:allActions) {
			Loggers.policyLogger.debug("Found action: "+actionString);
		}
		
		/*
		 * Looking for states
		 */
		Loggers.policyLogger.debug("Looking for states");
		StmtIterator statsIterator2=allConfig.listStatements(null,MetaLanguage.typeProperty,ResourceFactory.createResource(TerminologyManagerConfig.confStateType));
		while(statsIterator2.hasNext()) {
			Statement currentStat=statsIterator2.nextStatement();
			Resource currentState=currentStat.getSubject();
			states.add(currentState);
			Resource overridden=SimpleQueriesProcessor.getOptionalResourceObject(currentState, ResourceFactory.createProperty(TerminologyManagerConfig.confOverrides), allConfig);
			if(overridden!=null) {
				if(overridden.getURI().equals(stateDefaultURI)) stateDefaultURI=overridden.getURI();
				if(overridden.getURI().equals(stateObsoleteURI)) stateObsoleteURI=overridden.getURI();
				if(overridden.getURI().equals(stateSupersedURI)) stateSupersedURI=overridden.getURI();
			}
		}
		if(!states.contains(ResourceFactory.createResource(stateDefaultURI))) states.add(ResourceFactory.createResource(stateDefaultURI));
		if(!states.contains(ResourceFactory.createResource(stateObsoleteURI))) states.add(ResourceFactory.createResource(stateObsoleteURI));
		if(!states.contains(ResourceFactory.createResource(stateSupersedURI))) states.add(ResourceFactory.createResource(stateSupersedURI));
		
		allStates=new String[states.size()];
		i=0;
		for (Resource stat:states) {
			allStates[i++]=stat.getURI();
		}
		
		for(String stateString:allStates) {
			Loggers.policyLogger.debug("Found state: "+stateString);
		}
		
		
		/*
		 * Reading transition map
		 */
		for(Resource action:actions) {
			Loggers.policyLogger.debug("Reading transition map for: "+action.getURI());
			Loggers.policyLogger.trace("Register transitions");
			StmtIterator registerActionsIter=allConfig.listStatements(action,ResourceFactory.createProperty(TerminologyManagerConfig.confEffectOnReg),(Resource)null);
			while(registerActionsIter.hasNext()) {
				Statement effectStat=registerActionsIter.nextStatement();
				if(effectStat.getObject().isResource()) {
					//TODO note that we could check for the type being actionRole, but we keep less pedantic for the time being.
					Loggers.policyLogger.debug("Found transition: "+effectStat.getObject().asResource().getURI());
					String[] transitionBlock=new String[8];
					transitionBlock[PRE_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreThis), allConfig);
					transitionBlock[PRE_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreUp), allConfig);
					transitionBlock[PRE_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreDown), allConfig);
					transitionBlock[PRE_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreAux), allConfig);
					transitionBlock[POST_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostThis), allConfig);
					transitionBlock[POST_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostUp), allConfig);
					transitionBlock[POST_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostDown), allConfig);
					transitionBlock[POST_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostAux), allConfig);
					for(String val:transitionBlock) Loggers.policyLogger.debug("Map: "+val);
					
					if(!registerTransitions.containsKey(action.getURI())) {
						registerTransitions.put(action.getURI(),new ArrayList<String[]>());
					}
					registerTransitions.get(action.getURI()).add(transitionBlock);
					
				
				}
			}
			Loggers.policyLogger.trace("Code transitions");
			//TODO proceed for codes
			StmtIterator codeActionsIter=allConfig.listStatements(action,ResourceFactory.createProperty(TerminologyManagerConfig.confEffectOnCode),(Resource)null);
			while(codeActionsIter.hasNext()) {
				Statement effectCode=codeActionsIter.nextStatement();
				if(effectCode.getObject().isResource()) {
					//TODO note that we could check for the type being actionRole, but we keep less pedantic for the time being.
					Loggers.policyLogger.debug("Found transition: "+effectCode.getObject().asResource().getURI());
					String[] transitionBlock=new String[8];
					transitionBlock[PRE_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreThis), allConfig);
					transitionBlock[PRE_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreUp), allConfig);
					transitionBlock[PRE_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreDown), allConfig);
					transitionBlock[PRE_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreAux), allConfig);
					transitionBlock[POST_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostThis), allConfig);
					transitionBlock[POST_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostUp), allConfig);
					transitionBlock[POST_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostDown), allConfig);
					transitionBlock[POST_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostAux), allConfig);
					for(String val:transitionBlock) Loggers.policyLogger.debug("Map: "+val);
					
					if(!codeTransitions.containsKey(action.getURI())) {
						codeTransitions.put(action.getURI(),new ArrayList<String[]>());
					}
					codeTransitions.get(action.getURI()).add(transitionBlock);
					
				
				}
			}
		}
		
		/*
		 * "Extra" actions definition
		 */
		actions.remove(ResourceFactory.createResource(actionUpdateURI));
		actions.remove(ResourceFactory.createResource(actionObsoleteURI));
		actions.remove(ResourceFactory.createResource(actionSupersedURI));
		actions.remove(ResourceFactory.createResource(actionAddURI));
		
		states.remove(ResourceFactory.createResource(stateDefaultURI));
		states.remove(ResourceFactory.createResource(stateObsoleteURI));
		states.remove(ResourceFactory.createResource(stateSupersedURI));
		
		extraActions=new String[actions.size()];
		i=0;
		for(Resource action:actions) {
			extraActions[i++]=action.getURI();
		}
		extraStates=new String[states.size()];
		i=0;
		for(Resource state:states) {
			extraStates[i++]=state.getURI();
		}
		
		for(String action:extraActions) Loggers.policyLogger.debug("Extra action: "+action);
		
		for(String state:extraStates) Loggers.policyLogger.debug("Extra state: "+state);
		
		
		
		
	
	}
	
	/**
	 * Returns true is the specified action is viable for the state map, if the target is a code.
	 * @param actionURI the identifier of the action
	 * @param thisState the current state of the target of the action
	 * @param upState the current state of the container of the action
	 * @param downState the current state of the contained of the action
	 * @param auxState the current state of a third entity involved in the action
	 * @return
	 */
	public boolean isViableOperationOnReg(String actionURI, String thisState, String upState, String downState, String auxState) {
		return isViableOperation(registerTransitions, actionURI,  thisState,  upState,  downState,  auxState);
	}

	/**
	 * Returns true is the specified action is viable for the state map, if the target is a register.
	 * @param actionURI the identifier of the action
	 * @param thisState the current state of the target of the action
	 * @param upState the current state of the container of the action
	 * @param downState the current state of the contained of the action
	 * @param auxState the current state of a third entity involved in the action
	 * @return
	 */
	public boolean isViableOperationOnCode(String actionURI, String thisState, String upState, String downState, String auxState) {
		return isViableOperation(codeTransitions, actionURI,  thisState,  upState,  downState,  auxState);

	}
	
	private boolean isViableOperation(Map<String,ArrayList<String[]>> anyTransitions, String actionURI, String thisState, String upState, String downState, String auxState) {
		//if(thisState==null) thisState="";
		//if(upState==null) upState="";
		//if(downState==null) downState="";
		//if(auxState==null) auxState="";
		
		if(!anyTransitions.containsKey(actionURI)) return false;
		ArrayList<String[]> transitionsForAction=anyTransitions.get(actionURI);
		//SSLogger.showDebug(true); //TODO to remove!
		Loggers.policyLogger.debug("Checking for viability of action: "+actionURI+"\n"+
		"Pre_this: "+thisState+"\n"+
		"Pre_up: "+upState+"\n"+
		"Pre_down: "+downState+"\n"+
		"Pre_aux: "+auxState);
		for(String[] transitionForAction:transitionsForAction) {
			boolean score=true;
			if(transitionForAction[PRE_THIS]!=null)
					if(thisState!=null)
						if(!transitionForAction[PRE_THIS].equals(thisState)) {
							score=false;
							Loggers.policyLogger.debug("This violation: "+transitionForAction[PRE_THIS]);
						}
			if(transitionForAction[PRE_UP]!=null) 
				if(upState!=null)
					if(!transitionForAction[PRE_UP].equals(upState)) {
						score=false;
						Loggers.policyLogger.debug("Up violation: "+transitionForAction[PRE_UP]);

					}
			if(transitionForAction[PRE_DOWN]!=null) 
				if(downState!=null)
					if(!transitionForAction[PRE_DOWN].equals(downState)) {
						score=false;
						Loggers.policyLogger.debug("Down violation: "+transitionForAction[PRE_DOWN]);

					}
			if(transitionForAction[PRE_AUX]!=null) 
				if(auxState!=null)
					if(!transitionForAction[PRE_AUX].equals(auxState)) {
						score=false;
						Loggers.policyLogger.debug("Aux violation: "+transitionForAction[PRE_AUX]);

					}
			if(score==true) return true;
		}
		return false;
		
	}
	
	
	/**
	 * Computes the next state for the given action on a register
	 * @param actionURI the identifier of the action
	 * @param thisState the current state of the target of the action
	 * @param upState the current state of the container of the action
	 * @param downState the current state of the contained of the action
	 * @param auxState the current state of a third entity involved in the action
	 * @return an array containing the states for (in the specified order): target entity, container, contained, third party object
	 * @throws InvalidProcessException
	 */
	public String[] nextRegState(String actionURI, String thisState, String upState, String downState, String auxState) throws InvalidProcessException {
		Loggers.policyLogger.trace("nextRegCall");
		return nextAnyState(registerTransitions, actionURI,  thisState,  upState,  downState,  auxState);
	}
	
	/**
	 * Computes the next state for the given action on a code
	 * @param actionURI the identifier of the action
	 * @param thisState the current state of the target of the action
	 * @param upState the current state of the container of the action
	 * @param downState the current state of the contained of the action
	 * @param auxState the current state of a third entity involved in the action
	 * @return an array containing the states for (in the specified order): target entity, container, contained, third party object
	 * @throws InvalidProcessException
	 */
	public String[] nextCodeState(String actionURI, String thisState, String upState, String downState, String auxState) throws InvalidProcessException {
		Loggers.policyLogger.trace("nextCodeCall");
		return nextAnyState(codeTransitions, actionURI,  thisState,  upState,  downState,  auxState);
	}
	
	private String[] nextAnyState(Map<String,ArrayList<String[]>> anyTransitions, String actionURI, String thisState, String upState, String downState, String auxState) throws InvalidProcessException {
		if(thisState==null) thisState="";
		if(upState==null) upState="";
		if(downState==null) downState="";
		if(auxState==null) auxState="";
		if(!anyTransitions.containsKey(actionURI)) {
			Loggers.policyLogger.debug("Action not found in transition table (action URI): "+actionURI);
			for(String elem:anyTransitions.keySet()) Loggers.policyLogger.debug("Trans. table "+elem);
			throw new InvalidProcessException(actionURI,thisState,upState,downState,auxState);
		}
		ArrayList<String[]> transitionsForAction=anyTransitions.get(actionURI);
		for(String[] transitionForAction:transitionsForAction) {
			boolean score=true;
			Loggers.policyLogger.debug("Checking next action for: "+actionURI);
			Loggers.policyLogger.trace("Checking THIS");
			if(transitionForAction[PRE_THIS]!=null) {
				if(thisState!=null) {
					if(!transitionForAction[PRE_THIS].equals(thisState)) {
						Loggers.policyLogger.debug("Check THIS :"+transitionForAction[PRE_THIS]+" against :"+thisState+" Failed");
						score=false;
					}
					else {
						Loggers.policyLogger.debug("Check THIS :"+transitionForAction[PRE_THIS]+" against :"+thisState+" Passed");
					}
				}
			}
			Loggers.policyLogger.trace("Checking UP");	
			if(transitionForAction[PRE_UP]!=null) {
				if(upState!=null) {
					if(!transitionForAction[PRE_UP].equals(upState)) {
						Loggers.policyLogger.debug("Check UP "+transitionForAction[PRE_UP]+" against :"+upState+" Failed");
						score=false;
					}
					else {
						Loggers.policyLogger.debug("Check UP "+transitionForAction[PRE_UP]+" against :"+upState+" Passed");
					}
				}
			}
			Loggers.policyLogger.trace("Checking DOWN");	
			if(transitionForAction[PRE_DOWN]!=null) {
				if(downState!=null) {
					if(!transitionForAction[PRE_DOWN].equals(downState)) {
						Loggers.policyLogger.debug("Check DOWN :"+transitionForAction[PRE_DOWN]+" against :"+downState+" Failed");
						score=false;
					}
					else {
						Loggers.policyLogger.debug("Check DOWN :"+transitionForAction[PRE_DOWN]+" against :"+downState+" Passed");

					}
				}
			}
			Loggers.policyLogger.trace("Checking AUX");	
			if(transitionForAction[PRE_AUX]!=null) {
				if(auxState!=null) {
					if(!transitionForAction[PRE_AUX].equals(auxState)) {
						Loggers.policyLogger.debug("Check AUX :"+transitionForAction[PRE_AUX]+" against :"+auxState+" Failed");
						score=false;
					}
					else {
						Loggers.policyLogger.debug("Check AUX :"+transitionForAction[PRE_AUX]+" against :"+auxState+" Failed");
					}
				}
			}
				
			if(score==true) {
				Loggers.policyLogger.debug("Success!");
				return transitionForAction;
			}
		}
		Loggers.policyLogger.error("Failure!");
		throw new InvalidProcessException(actionURI,thisState,upState,downState,auxState);
	}

	public String[] getExtraActions() {
		return extraActions;
	}

}
