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
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

public class RegistryPolicyManager {
	private final static int PRE_THIS=0;
	private final static int PRE_UP=1;
	private final static int PRE_DOWN=2;
	private final static int PRE_AUX=3;
	private final static int POST_THIS=4;
	private final static int POST_UP=5;
	private final static int POST_DOWN=6;
	private final static int POST_AUX=7;
	
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
	public static final String validateAction=MetaLanguage.terminologyValidateAction.getURI();
	public static final String invalidateAction=MetaLanguage.terminologyInvalidateAction.getURI();
	
	
	//TODO Remove (and remove from MetaLanguage)
	static final String illegalState = MetaLanguage.statusIllegalResource.getURI();
	public static final String nullState = MetaLanguage.statusNullResource.getURI();
	
	
	//TODO Action without state transition! (but with permissions... where should it go?)
	public static String tagAction=MetaLanguage.tagAction.getURI();
	
	
	//TODO to remove
	//public static RegistryPolicyManager tm=new RegistryPolicyManager(null);
	
	
	//TODO to remove once the new map is ready
	Map<String,String> updateTransitions=new Hashtable<String,String>();
	Map<String,String> addTransitions=new Hashtable<String,String>();
	Map<String,String> delTermTransitions=new Hashtable<String,String>();
	Map<String,String> delRegTransitions=new Hashtable<String,String>();
	Map<String,String> superseederTransitions=new Hashtable<String,String>();
	Map<String,String> superseededTransitions=new Hashtable<String,String>();
	Map<String,String> validateTransitions=new Hashtable<String,String>();
	Map<String,String> invalidateTransitions=new Hashtable<String,String>();
	
	private String[] allActions=null;
	private String[] extraActions=null;
	private String[] allStates=null;
	private String[] extraStates=null;
	
	private Map<String,ArrayList<String[]>> registerTransitions=new Hashtable<String,ArrayList<String[]>>();
	private Map<String,ArrayList<String[]>> codeTransitions=new Hashtable<String,ArrayList<String[]>>();

	
	
	public RegistryPolicyManager(Model allConfig) {
		SSLogger.log("Looking for built in actions and transitions");
		Set<Resource> actions=new HashSet<Resource>();
		Set<Resource> states=new HashSet<Resource>();
		/*
		 * Looking for actions
		 */
		SSLogger.log("Looking for actions");
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
		
		SSLogger.log("Found actions: ",SSLogger.DEBUG);
		for(String actionString:allActions) {
			SSLogger.log(actionString,SSLogger.DEBUG);
		}
		
		/*
		 * Looking for states
		 */
		SSLogger.log("Looking for states");
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
		
		SSLogger.log("Found states: ",SSLogger.DEBUG);
		for(String stateString:allStates) {
			SSLogger.log(stateString,SSLogger.DEBUG);
		}
		
		
		/*
		 * Reading transition map
		 */
		for(Resource action:actions) {
			SSLogger.log("Reading transition map for: "+action.getURI());
			SSLogger.log("Register transitions");
			StmtIterator registerActionsIter=allConfig.listStatements(action,ResourceFactory.createProperty(TerminologyManagerConfig.confEffectOnReg),(Resource)null);
			while(registerActionsIter.hasNext()) {
				Statement effectStat=registerActionsIter.nextStatement();
				if(effectStat.getObject().isResource()) {
					//TODO note that we could check for the type being actionRole, but we keep less pedantic for the time being.
					SSLogger.log("Found transition: "+effectStat.getObject().asResource().getURI(),SSLogger.DEBUG);
					String[] transitionBlock=new String[8];
					transitionBlock[PRE_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreThis), allConfig);
					transitionBlock[PRE_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreUp), allConfig);
					transitionBlock[PRE_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreDown), allConfig);
					transitionBlock[PRE_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreAux), allConfig);
					transitionBlock[POST_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostThis), allConfig);
					transitionBlock[POST_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostUp), allConfig);
					transitionBlock[POST_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostDown), allConfig);
					transitionBlock[POST_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectStat.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostAux), allConfig);
					SSLogger.log("Map:"+SSLogger.DEBUG);
					for(String val:transitionBlock) SSLogger.log(val,SSLogger.DEBUG);
					
					if(!registerTransitions.containsKey(effectStat.getObject().asResource().getURI())) {
						registerTransitions.put(effectStat.getObject().asResource().getURI(),new ArrayList<String[]>());
					}
					registerTransitions.get(effectStat.getObject().asResource().getURI()).add(transitionBlock);
					
				
				}
			}
			SSLogger.log("Code transitions");
			//TODO proceed for codes
			StmtIterator codeActionsIter=allConfig.listStatements(action,ResourceFactory.createProperty(TerminologyManagerConfig.confEffectOnCode),(Resource)null);
			while(codeActionsIter.hasNext()) {
				Statement effectCode=codeActionsIter.nextStatement();
				if(effectCode.getObject().isResource()) {
					//TODO note that we could check for the type being actionRole, but we keep less pedantic for the time being.
					SSLogger.log("Found transition: "+effectCode.getObject().asResource().getURI(),SSLogger.DEBUG);
					String[] transitionBlock=new String[8];
					transitionBlock[PRE_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreThis), allConfig);
					transitionBlock[PRE_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreUp), allConfig);
					transitionBlock[PRE_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreDown), allConfig);
					transitionBlock[PRE_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPreAux), allConfig);
					transitionBlock[POST_THIS]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostThis), allConfig);
					transitionBlock[POST_UP]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostUp), allConfig);
					transitionBlock[POST_DOWN]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostDown), allConfig);
					transitionBlock[POST_AUX]=SimpleQueriesProcessor.getOptionalResourceObjectAsString(effectCode.getObject().asResource(), ResourceFactory.createProperty(TerminologyManagerConfig.confPostAux), allConfig);
					SSLogger.log("Map:"+SSLogger.DEBUG);
					for(String val:transitionBlock) SSLogger.log(val,SSLogger.DEBUG);
					
					if(!codeTransitions.containsKey(effectCode.getObject().asResource().getURI())) {
						codeTransitions.put(effectCode.getObject().asResource().getURI(),new ArrayList<String[]>());
					}
					codeTransitions.get(effectCode.getObject().asResource().getURI()).add(transitionBlock);
					
				
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
		
		SSLogger.log("Extra actions:",SSLogger.DEBUG);
		for(String action:extraActions) SSLogger.log(action,SSLogger.DEBUG);
		
		SSLogger.log("Extra states:",SSLogger.DEBUG);
		for(String state:extraStates) SSLogger.log(state,SSLogger.DEBUG);
		
		//allConfig.listObjectsOfProperty(TerminologyManagerConfig.); 
		//TODO list states (override/replace)
		//TODO list actions (override/replace)
		//TODO load transitions (default in default file).
	
		//TODO here we need to read transitions from file!
		// Note: need to record only chaging states
		updateTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		updateTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOValidationResource.getURI());
		updateTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		updateTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);
		updateTransitions.put(MetaLanguage.statusOutOfRegistry.getURI(),MetaLanguage.statusOutOfRegistry.getURI());

		//updateTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOValidationResource.getURI());
	
		addTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		addTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),illegalState);
		addTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		addTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);


		
		delTermTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMORetiredResource.getURI());
		delTermTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),illegalState);
		delTermTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		delTermTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);

		
		
		delRegTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		delRegTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		delRegTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		delRegTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);

		
		
		
		superseederTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),RegistryPolicyManager.illegalState);
		superseederTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOValidationResource.getURI());
		superseederTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		superseederTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);
		

		superseededTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOSupersededResource.getURI());
		superseededTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOSupersededResource.getURI());
		superseededTransitions.put(MetaLanguage.statusWMOSupersededResource.getURI(),illegalState);
		superseededTransitions.put(MetaLanguage.statusWMORetiredResource.getURI(),illegalState);

		
		validateTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		validateTransitions.put(MetaLanguage.statusOutOfRegistry.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		validateTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),MetaLanguage.statusWMOValidResource.getURI());
		validateTransitions.put(nullState,MetaLanguage.statusWMOValidResource.getURI());

		
		invalidateTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),MetaLanguage.statusWMOValidationResource.getURI());
		invalidateTransitions.put(MetaLanguage.statusOutOfRegistry.getURI(),illegalState);
		invalidateTransitions.put(MetaLanguage.statusWMOValidationResource.getURI(),illegalState);
		invalidateTransitions.put(nullState,illegalState);

		
	
	}
	

}
