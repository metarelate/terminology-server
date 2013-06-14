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
 * TODO Check Synch calls
 * TODO add log requests
 * TODO perhaps a bit of refactoring ?
 */
package net.metarelate.terminology.management;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.coreModel.Versioner;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.ImpossibleOperationException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Performs operations on registers
 * @author andrea_splendiani
 *
 */
public class TerminologyManager {
	public static final int MODE_REPLACE = 1;	//TODO thes should be all private and the only method calling them wrapped
	private static final int MODE_ADD = 2;
	private static final int MODE_REMOVE = 3;
	private static final int MODE_SOBSTITUTE = 4;
	
	private final int INDIVIDUAL_TYPE = 10;	// Note: these are not meant to be set! they are only constants to be used...
	private final int SET_TYPE=11;			// TODO  the design should be changed to avoid confusion

	private Initializer myInitializer=null;
	//TerminologyFactory myFactory;
	//AuthRegistryManager myAuthManager;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	public TerminologyManager(Initializer initializer) {
		myInitializer=initializer;
	}
	
	/**
	 * For the statements in the model, if a statement with the same subject and property is present in statsToReplace,
	 * the old statement is removed before all statsToReplace are added.
	 * @param entityURI
	 * @param statsToReplace
	 * @param actionAuthor
	 * @param description
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void replaceEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_REPLACE);
	}
	
	/**
	 * Remove all current statements and replaces them with the ones provided
	 * @param entityURI
	 * @param statsToReplace
	 * @param actionAuthor
	 * @param description
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void sobstituteEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_SOBSTITUTE);
	}
	
	/**
	 * Adds a set of statement to an entity
	 * @param entityURI
	 * @param statsToAdd the set of statements to be added
	 * @param actionAuthor
	 * @param description
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void addToEntityInformation(String entityURI, Model statsToAdd, String actionAuthor, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		amendEntityInformation(entityURI, statsToAdd, actionAuthor, description, MODE_ADD);

	}
	
	/**
	 * Removes a set of statements from an entity
	 * @param entityURI
	 * @param statsToRemove the statements to be removed
	 * @param actionAuthor
	 * @param description
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void removeFromEntityInformation(String entityURI, Model statsToRemove, String actionAuthor, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		amendEntityInformation(entityURI, statsToRemove, actionAuthor, description, MODE_REMOVE);		
	}
	
	/**
	 * Adds a new term to a register
	 * @param codeURI
	 * @param registerURI
	 * @param defaultEntityModel the statements for the new code
	 * @param actionAuthor
	 * @param description
	 * @param isVersioned
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 * @throws ImporterException
	 */
	public void addTermToRegister(String codeURI, String registerURI, Model defaultEntityModel,String actionAuthor, String description, boolean isVersioned ) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException, ImporterException {
		addEntityToRegister( codeURI,  registerURI,  defaultEntityModel, actionAuthor,  description,  isVersioned, INDIVIDUAL_TYPE);
	}
	
	/**
	 * Adds a new sub register to a register
	 * @param codeURI
	 * @param registerURI
	 * @param defaultEntityModel the statements for the new register
	 * @param actionAuthor
	 * @param description
	 * @param isVersioned
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 * @throws ImporterException
	 */
	public void addSubRegister(String codeURI, String registerURI, Model defaultEntityModel,String actionAuthor, String description, boolean isVersioned ) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException, ImporterException {
		addEntityToRegister( codeURI,  registerURI,  defaultEntityModel, actionAuthor,  description,  isVersioned, SET_TYPE);

	}
	
	/**
	 * Add a new entity to a register
	 * @param codeURI the uri of the new entity
	 * @param registerURI the register
	 * @param defaultEntityModel the statements for the new entity
	 * @param actionAuthor the agent
	 * @param description a description of the action
	 * @param isVersioned true is versioning should be active
	 * @param entityType set o code (flag)
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 * @throws ImporterException
	 */
	private void addEntityToRegister(String codeURI, String registerURI, Model defaultEntityModel,String actionAuthor, String description, boolean isVersioned, int entityType ) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException, ImporterException {
			if(!myInitializer.myAuthManager.can(actionAuthor,RegistryPolicyManager.actionAddURI,registerURI))
					throw new AuthException(actionAuthor,RegistryPolicyManager.actionAddURI,registerURI);
			
			TerminologySet myRegister=null;
			if(!myInitializer.myFactory.terminologySetExist(registerURI)) throw new RegistryAccessException("Unable to modify "+registerURI+" (register does not exist)");
			myRegister=myInitializer.myFactory.getUncheckedTerminologySet(registerURI);
			if(myInitializer.myFactory.terminologyIndividualExist(codeURI)) {
				// Note this is only for adding! Not for changing obsolete/valid status. In other words, a delete operation
				throw new RegistryAccessException("Code "+codeURI+" exists. Use \"move\" to change register");
			}
			String lastRegisterVersion=myRegister.getLastVersion();
			String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
			
			// TODO note that we assume one parent only, so we pick one at random.
			String fatherState=null;
			TerminologySet oneFather=null;
			//System.out.println("Getting containers");
			Set<TerminologySet>containers= myRegister.getContainers(lastRegisterVersion);
			//System.out.println("Containers size: "+containers.size());
			Loggers.processLogger.trace("Checking state on container");
			
			Iterator<TerminologySet> containersIter=containers.iterator();
			System.out.println("Iterator has next: "+containersIter.hasNext());
			if(containersIter.hasNext()) {
				Loggers.processLogger.trace("Found: "+containers.size());
				//System.out.println(codeURI);
				//System.out.println(registerURI);
				//System.out.println(containers.size());
				oneFather=containersIter.next();
				Loggers.processLogger.trace("father: "+oneFather.getURI());
				Loggers.processLogger.trace("version: "+oneFather.getLastVersion());
				fatherState=oneFather.getStateURI(oneFather.getLastVersion());
			}
			String results[]=null;
			results=myInitializer.myRegistryPolicyManager.nextRegState(RegistryPolicyManager.actionAddURI, preRegisterStatus, fatherState, null, null);
			String postRegisterStatus=results[RegistryPolicyManager.POST_THIS];
			//TODO here we should propagte state transition up...
			
			String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
				
			//Now we create the entity
			TerminologyEntity newTerm=null;
			//TODO dirty use of a type system!!!! should be a bit re-designed (e.g.: use entity more!)
			if(entityType==INDIVIDUAL_TYPE) {
				if(myInitializer.myFactory.terminologyEntityExist(codeURI)) throw new RegistryAccessException("Impossible to add individual "+codeURI+" as an entity with the same URI already exists");
				if(isVersioned) newTerm=myInitializer.myFactory.createNewVersionedTerminologyIndividual(codeURI);
				else newTerm=myInitializer.myFactory.createNewUnversionedTerminologyIndividual(codeURI);
			}
			else if(entityType==SET_TYPE) {
				if(myInitializer.myFactory.terminologyEntityExist(codeURI)) throw new RegistryAccessException("Impossible to add set "+codeURI+" as an entity with the same URI already exists");
				if(isVersioned) newTerm=myInitializer.myFactory.createNewVersionedTerminologySet(codeURI);
				else newTerm=myInitializer.myFactory.createNewUnversionedTerminologySet(codeURI);
			}
			else Loggers.processLogger.error("This is a private method, should never be invoked like that!");
			
			//newTerm.setDefaultVersion(newTerm.getLastVersion());
			//if(isVersioned) newTerm.setIsVersioned(true);	
			newTerm.setStateURI(RegistryPolicyManager.stateDefaultURI,newTerm.getLastVersion());
			newTerm.setOwnerURI(actionAuthor);
			newTerm.setActionURI(RegistryPolicyManager.actionAddURI ,newTerm.getLastVersion());
			newTerm.setActionAuthorURI(actionAuthor,newTerm.getLastVersion());
			if(description==null) description="New term added to registry";
			newTerm.setActionDescription(description,newTerm.getLastVersion()); //TODO default should be handled more coherently
			newTerm.addStatements(defaultEntityModel,newTerm.getLastVersion());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			newTerm.setActionDate(dateFormat.format(date),newTerm.getLastVersion());
			
			myRegister.registerVersion(newRegisterVersion);
			myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
			
			//TODO dirty use of a type system!!!! should be a bit re-designed (e.g.: use entity more!)
			if(entityType==INDIVIDUAL_TYPE)
				myRegister.registerContainedIndividual((TerminologyIndividual)newTerm, newRegisterVersion, newTerm.getLastVersion());
			else if(entityType==SET_TYPE)
				myRegister.registerContainedCollection((TerminologySet)newTerm, newRegisterVersion, newTerm.getLastVersion());	
			else Loggers.processLogger.error("This is a private method, should never be invoked like that!");
			if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
			//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
			myRegister.setActionAuthorURI(actionAuthor, newRegisterVersion);
			if(description!=null) myRegister.setActionDescription("Added term: "+newTerm.getURI(),newRegisterVersion);
			myRegister.setActionURI(RegistryPolicyManager.actionAddURI ,newRegisterVersion);
			myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
			
			newTerm.synch();
			myRegister.synch();
				
		}
	

	/**
	 * Update action
	 * @param entityURI the entity to be updated
	 * @param statsToReplace the new statements
	 * @param actionAuthor the agent
	 * @param description a description for this action
	 * @param mode the update modality (@see #MODE_ADD @see #MODE_REMOVE @see #MODE_SOBSTITUTE @see #MODE_REPLACE)
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void amendEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description, int mode) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		if(!myInitializer.myAuthManager.can(actionAuthor,RegistryPolicyManager.actionUpdateURI,entityURI))
			throw new AuthException(actionAuthor,RegistryPolicyManager.actionUpdateURI,entityURI);
		TerminologyEntity myEntity=null;
		if(!myInitializer.myFactory.terminologyEntityExist(entityURI)) throw new RegistryAccessException("Unable to amend "+entityURI+" (entity does not exist)");
		myEntity=myInitializer.myFactory.getUncheckedTerminologyEntity(entityURI);
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		
		//TODO we only consider thisState, but we should consider all!
		String[] result=null;
		if(myInitializer.myFactory.terminologySetExist(entityURI))
			result=myInitializer.myRegistryPolicyManager.nextRegState(RegistryPolicyManager.actionUpdateURI, preStatus, null, null, null);
		if(myInitializer.myFactory.terminologyIndividualExist(entityURI))
			result=myInitializer.myRegistryPolicyManager.nextCodeState(RegistryPolicyManager.actionUpdateURI, preStatus, null, null, null);
		if(result==null) throw new InvalidProcessException("Something went wrong while considering next state");
		
		String postStatus=result[RegistryPolicyManager.POST_THIS];
		
		String newVersion=Versioner.createNextVersion(lastVersion);
		myEntity.registerVersion(newVersion);
		if(mode==MODE_REPLACE) {
			// myEntity.replaceStatements(statsToReplace, newVersion);
			// Note: the above was not really making sense!
			Model oldStatements=ModelFactory.createDefaultModel();
			oldStatements.add(myEntity.getStatements(lastVersion));
			//Now we clear the old statements
			StmtIterator statsToReplaceIterator=statsToReplace.listStatements();
			while(statsToReplaceIterator.hasNext()) {
				Statement currentStatToReplace=statsToReplaceIterator.nextStatement();
				Model diffModel=ModelFactory.createDefaultModel();
				diffModel.add(oldStatements.listStatements(currentStatToReplace.getSubject(),currentStatToReplace.getPredicate(),(RDFNode)null));
				oldStatements.remove(diffModel);
			}
			oldStatements.add(statsToReplace);
			myEntity.getStatements(newVersion).add(oldStatements);
		}
		if(mode==MODE_SOBSTITUTE)	
			myEntity.replaceStatements(statsToReplace, newVersion);
		if(mode==MODE_ADD) 
			myEntity.getStatements(newVersion).add(myEntity.getStatements(lastVersion)).add(statsToReplace);
		if(mode==MODE_REMOVE)
			myEntity.getStatements(newVersion).add((myEntity.getStatements(lastVersion)).remove(statsToReplace));
		//myEntity.setDefaultVersion(newVersion);
		if(postStatus!=null) myEntity.setStateURI(postStatus, newVersion);
		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.setActionURI(RegistryPolicyManager.actionUpdateURI,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		myEntity.synch();
	}
	
	/**
	 * Performs a generic action (only results in change of states)
	 * @param actionURI
	 * @param entityURI
	 * @param actionAuthor
	 * @param description
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void performGenericAction(String actionURI, String entityURI, String actionAuthor, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		if(!myInitializer.myAuthManager.can(actionAuthor,actionURI,entityURI)) {
				throw new AuthException(actionAuthor,actionURI,entityURI);
		}
		TerminologyEntity myEntity=null;
		if(!myInitializer.myFactory.terminologyEntityExist(entityURI)) throw new RegistryAccessException("Unable to perform generic operation on "+entityURI+" (entity does not exist)");
		myEntity=myInitializer.myFactory.getUncheckedTerminologyEntity(entityURI);
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		
		//TODO we only consider thisState, but we should consider all!
		String[] result=null;
		if(myInitializer.myFactory.terminologySetExist(entityURI))
			result=myInitializer.myRegistryPolicyManager.nextRegState(actionURI, preStatus, null, null, null);
		if(myInitializer.myFactory.terminologyIndividualExist(entityURI))
			result=myInitializer.myRegistryPolicyManager.nextCodeState(actionURI, preStatus, null, null, null);
		if(result==null) throw new InvalidProcessException("Something went wrong while considering next state");
		
	
		String postStatus=result[RegistryPolicyManager.POST_THIS];
		
		
		String newVersion=Versioner.createNextVersion(lastVersion);
		myEntity.registerVersion(newVersion);
		myEntity.replaceStatements(myEntity.getStatements(lastVersion), newVersion);
		myEntity.setStateURI(postStatus,newVersion);
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		myEntity.setActionURI(actionURI,newVersion);
		myEntity.synch();

		
		
		
	}
	

	
	

	/**
	 * Tag a release
	 * @param authorURI the agent
	 * @param tag the tag value
	 * @param description
	 * @throws AuthException
	 * @throws ModelException
	 */
	public void tagRelease(String authorURI, String tag, String description) throws AuthException, ModelException {
		try {
			if(!myInitializer.myAuthManager.can(authorURI,RegistryPolicyManager.tagAction,null))
				throw new AuthException(authorURI,RegistryPolicyManager.tagAction,null);
		} catch (RegistryAccessException e) {
			// TODO this shouldn't really happen in this case
			e.printStackTrace();
		}
		TerminologySet[] roots=myInitializer.myFactory.getRootCollections();
		for(TerminologySet root: roots) {
			Loggers.processLogger.info("Tagging root: "+root.getURI());
			myTag(root,tag);
		}
		//TODO we may want to register infos on tags somewhere.
		
	}
	

	
	private void myTag(TerminologySet set, String tag) throws ModelException {
		set.tagVersion(set.getLastVersion(),tag);
		Set<TerminologyIndividual>terms=set.getIndividuals(set.getLastVersion());
		Iterator<TerminologyIndividual>termIter=terms.iterator();
		while(termIter.hasNext()) {
			TerminologyIndividual term=termIter.next();
			term.tagVersion(term.getLastVersion(),tag);
		}
		
		Set<TerminologySet> children=set.getCollections(set.getLastVersion());
		Iterator<TerminologySet>childrenIter=children.iterator();
		while(childrenIter.hasNext()) {
			TerminologySet child=childrenIter.next();
			myTag(child,tag);
		}
		
		
	}
	
	/**
	 * Obsolete a term
	 * @param urlToDelete the term to be obsoleted
	 * @param actionAuthorURI the agent
	 * @param description a description associated to this action
	 * @throws ModelException
	 * @throws RegistryAccessException
	 * @throws ImpossibleOperationException
	 */
	public void delTerm(String urlToDelete, String actionAuthorURI,
			String description) throws ModelException, RegistryAccessException, ImpossibleOperationException {
		Loggers.processLogger.debug("Going to obsolete "+urlToDelete+" by "+actionAuthorURI+" because : "+description);		

		if(myInitializer.myFactory.terminologySetExist(urlToDelete)) {
			Loggers.processLogger.trace("This is a set, checking if it's empty first");
			//This is a set, we can obsolete it only if in its last version it has not valid individuals
			TerminologySet setToDelete=myInitializer.myFactory.getUncheckedTerminologySet(urlToDelete);
			String lastVersion=setToDelete.getLastVersion();
			Set<TerminologyIndividual> childrendIndividuals=setToDelete.getIndividuals(lastVersion);
			Set<TerminologySet> setsIndividuals=setToDelete.getCollections(lastVersion);
			if(!(childrendIndividuals.size()==0 && setsIndividuals.size()==0)){
				throw new ImpossibleOperationException("Impossible to obsolete non empty register: "+urlToDelete+" (this is a built in constraint)");
			}
			propagateDeleteOverContainers(setToDelete,actionAuthorURI,description);
		}
		else if(myInitializer.myFactory.terminologyIndividualExist(urlToDelete)) {
			Loggers.processLogger.trace("This is a code, proceeding from deletion");
			TerminologyIndividual individualToDelete=myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToDelete);
			propagateDeleteOverContainers(individualToDelete,actionAuthorURI,description);
		}
		else {
			throw new UnknownURIException(urlToDelete);
		}
		
	}
	
	
	private void propagateDeleteOverContainers(TerminologyEntity entityToDelete, String actionAuthorURI, String description) throws AuthException, RegistryAccessException, ModelException, InvalidProcessException {
		Set<TerminologySet> containers=entityToDelete.getContainers(entityToDelete.getLastVersion());
		//Note: there should only be one container for the time being...
		if(containers.size()>1) throw new ModelException("More than one container defined for "+entityToDelete);
		Iterator<TerminologySet> containersIter=containers.iterator();
		while(containersIter.hasNext()) {
			TerminologySet container=containersIter.next();
			delTermFromRegister(entityToDelete.getURI(), container.getURI(),
					actionAuthorURI, description);
		}
	}
	
	//TODO generalize to set
	//TODO overall TerminologyManager is due a big overhaul!!!
	
	//TODO should be made private, other classes can call through the wrapping method
	/**
	 * Obsolete a term
	 * @param termURI the term to be obsoleted
	 * @param regURI the register where the term is contained
	 * @param actionAuthorURI the uri of the agent
	 * @param description a description associated to this action
	 * @throws AuthException
	 * @throws RegistryAccessException
	 * @throws InvalidProcessException
	 * @throws ModelException
	 */
	public void delTermFromRegister(String termURI, String regURI,
			String actionAuthorURI, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
	Loggers.processLogger.debug("Going to remove "+termURI+" from "+regURI+" by "+actionAuthorURI+" because : "+description);		
		if(!myInitializer.myAuthManager.can(actionAuthorURI,RegistryPolicyManager.actionObsoleteURI,regURI))
			throw new AuthException(actionAuthorURI,RegistryPolicyManager.actionObsoleteURI,regURI);

	TerminologySet myRegister=null;
	if(!myInitializer.myFactory.terminologySetExist(regURI)) throw new RegistryAccessException("Unable to modify "+regURI+" (register does not exist)");
	myRegister=myInitializer.myFactory.getUncheckedTerminologySet(regURI);
	TerminologyEntity myTerm=null;
	if(myInitializer.myFactory.terminologyIndividualExist(termURI)) {
		Loggers.processLogger.trace("Going to remove a code");
		myTerm=myInitializer.myFactory.getUncheckedTerminologyIndividual(termURI);
		if(myTerm==null) throw new RegistryAccessException("Code "+termURI+" does not exists.");
		if(!myRegister.containsEntity(myTerm)) {
			//TODO entity/set doesn't play well with this test!
			throw new RegistryAccessException("Code "+termURI+" is not contained in the last version of "+regURI);
		}
			
	}
	if(myInitializer.myFactory.terminologySetExist(termURI)) {
		Loggers.processLogger.trace("Going to remove a register");
		myTerm=myInitializer.myFactory.getUncheckedTerminologySet(termURI);
		if(myTerm==null) throw new RegistryAccessException("Code "+termURI+" does not exists.");
		if(!myRegister.getCollections(myRegister.getLastVersion()).contains(myTerm))
			throw new RegistryAccessException("Register "+termURI+" is not contained in the last version of "+regURI);
	}
	
	if(myTerm==null) throw new RegistryAccessException("Code "+termURI+" does not exists.");
	
	// TODO maybe we should test the containment in the register here.
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	
	//TODO we only consider thisState, but we should consider all!
	String[] result=null;
	if(myInitializer.myFactory.terminologySetExist(termURI))
		result=myInitializer.myRegistryPolicyManager.nextRegState(RegistryPolicyManager.actionObsoleteURI, preTermStatus, preRegisterStatus, null, null);
	if(myInitializer.myFactory.terminologyIndividualExist(termURI))
		result=myInitializer.myRegistryPolicyManager.nextCodeState(RegistryPolicyManager.actionObsoleteURI, preTermStatus, preRegisterStatus, null, null);
	if(result==null) throw new InvalidProcessException("Something went wrong while considering next state");
	String postTermStatus=result[RegistryPolicyManager.POST_THIS];
	String postRegisterStatus=result[RegistryPolicyManager.POST_UP];
	

	Loggers.processLogger.trace("pre Term Status: "+preTermStatus);
	Loggers.processLogger.trace("post Term Status: "+postTermStatus);
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description!=null) myRegister.setActionDescription(description+"(deleted term: "+myTerm.getURI()+")",newRegisterVersion);
	else myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyManager.actionObsoleteURI ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	myTerm.registerVersion(newTermVersion);
	
	

		
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyManager.actionObsoleteURI ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	myTerm.setActionDescription("Term was removed from registry",newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion); // Note this overwrites old statements!
	myTerm.linkVersions(lastTermVersion,newTermVersion);
	
	myRegister.unregisterContainedEntity(myTerm, newRegisterVersion, newTermVersion);
	
	myTerm.synch();
	myRegister.synch();
	Loggers.commandLogger.trace("Action completed");
		//////
		
	}
	
	/**
	 * Superseds a term
	 * @param urlToAction the uri of the superseded term
	 * @param urlSuperseder the uri of the superseded
	 * @param actionAuthorURI the uri of the author
	 * @param description a description associated to this action
	 * @throws ImpossibleOperationException
	 * @throws RegistryAccessException
	 * @throws ModelException
	 */
	public void superseedTerm(String urlToAction, String urlSuperseder,
			String actionAuthorURI, String description) throws ImpossibleOperationException, RegistryAccessException, ModelException {
		if(!myInitializer.myFactory.terminologySetExist(urlToAction)) {
			throw new ImpossibleOperationException("Impossible to supersed a register: "+urlToAction+" (this is a built in constraint)");
		}
		if(!myInitializer.myFactory.terminologySetExist(urlSuperseder)) {
			throw new ImpossibleOperationException("Impossible to supersed with a register: "+urlSuperseder+" (this is a built in constraint)");
		}
		if(!myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
			throw new UnknownURIException(urlToAction);
		}
		if(!myInitializer.myFactory.terminologyIndividualExist(urlSuperseder)) {
			throw new UnknownURIException(urlSuperseder);
		}
		propagateSupersedOverContainers(urlToAction,urlSuperseder,actionAuthorURI,description);
		
	}
	
	private void propagateSupersedOverContainers(String urlToSupersed, String supersedingURL, String actionAuthorURI, String description) throws AuthException, RegistryAccessException, ModelException, InvalidProcessException {
		TerminologyIndividual termToSupersed=myInitializer.myFactory.getCheckedTerminologyIndividual(urlToSupersed);
		Set<TerminologySet> containers=termToSupersed.getContainers(termToSupersed.getLastVersion());
		//Note: there should only be one container for the time being...
		if(containers.size()>1) throw new ModelException("More than one container defined for "+termToSupersed);
		Iterator<TerminologySet> containersIter=containers.iterator();
		while(containersIter.hasNext()) {
			TerminologySet container=containersIter.next();
			superseedTermInRegister(urlToSupersed, supersedingURL,container.getURI(),
					actionAuthorURI, description);
		}
	}

	

	private void superseedTermInRegister(String termURI,
			String superseedingTermURI, String regURI, 
			String actionAuthorURI, String description) throws AuthException, RegistryAccessException, InvalidProcessException, ModelException {
		// TODO Auto-generated method stub
		
		if(!myInitializer.myAuthManager.can(actionAuthorURI,RegistryPolicyManager.actionSupersedURI,regURI))
			throw new AuthException(actionAuthorURI,RegistryPolicyManager.actionSupersedURI,regURI);
	TerminologySet myRegister=null;
	if(!myInitializer.myFactory.terminologySetExist(regURI)) throw new RegistryAccessException("Unable to modify "+regURI+" (register does not exist)");
	myRegister=myInitializer.myFactory.getUncheckedTerminologySet(regURI);
	
	TerminologyIndividual myTerm=null;
	if(!myInitializer.myFactory.terminologyIndividualExist(termURI)) throw new RegistryAccessException("Code "+termURI+" does not exists.");
	myTerm=myInitializer.myFactory.getUncheckedTerminologyIndividual(termURI);
	
	TerminologyIndividual superseedingTerm=null;
	if(!myInitializer.myFactory.terminologyIndividualExist(superseedingTermURI)) throw new RegistryAccessException("Code "+superseedingTermURI+" does not exist. Add it first.");
	superseedingTerm=myInitializer.myFactory.getUncheckedTerminologyIndividual(superseedingTermURI);
	
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	String lastSuperseedingsTermVersion=superseedingTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	String preSuperseedingTermStatus=superseedingTerm.getStateURI(lastSuperseedingsTermVersion);

	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	String newSuperseedingVersion=Versioner.createNextVersion(lastSuperseedingsTermVersion);

	//TODO we only consider thisState, but we should consider all!
	String[] result=null;
	if(myInitializer.myFactory.terminologySetExist(termURI))
		result=myInitializer.myRegistryPolicyManager.nextRegState(RegistryPolicyManager.actionSupersedURI, preTermStatus, preRegisterStatus, null, preSuperseedingTermStatus);
	if(myInitializer.myFactory.terminologyIndividualExist(termURI))
		result=myInitializer.myRegistryPolicyManager.nextCodeState(RegistryPolicyManager.actionSupersedURI, preTermStatus, preRegisterStatus, null, preSuperseedingTermStatus);
	if(result==null) throw new InvalidProcessException("Something went wrong while considering next state");
	String postTermStatus=result[RegistryPolicyManager.POST_THIS];
	String postRegisterStatus=result[RegistryPolicyManager.POST_UP];
	String postSuperseedingTermStatus=result[RegistryPolicyManager.POST_AUX];
	
	
	
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description==null) myRegister.setActionDescription(description+" (deleted term: "+myTerm.getURI()+")",newRegisterVersion);
	else myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyManager.actionObsoleteURI ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	
	
	myTerm.registerVersion(newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion);
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyManager.actionSupersedURI ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	if(description==null) myTerm.setActionDescription("Term was removed from registry and superseeded by "+superseedingTermURI,newTermVersion);
	else myTerm.setActionDescription(description+ " (term was removed from registry and superseeded by "+superseedingTermURI+")",newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.getStatements(newTermVersion).add(myTerm.getResource(),MetaLanguage.superseededBy,superseedingTerm.getResource());
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.linkVersions(lastTermVersion,newTermVersion);


	superseedingTerm.registerVersion(newSuperseedingVersion);
	superseedingTerm.setStateURI(postTermStatus,newSuperseedingVersion);
	superseedingTerm.setActionURI(RegistryPolicyManager.actionSupersedURI ,newSuperseedingVersion);
	superseedingTerm.setActionAuthorURI(actionAuthorURI,newSuperseedingVersion);
	if(description!=null) superseedingTerm.setActionDescription("Term superseeded "+termURI,newSuperseedingVersion);
	else superseedingTerm.setActionDescription(description+" (term superseeded "+termURI+")",newSuperseedingVersion);
	superseedingTerm.addStatements(superseedingTerm.getStatements(lastSuperseedingsTermVersion),newSuperseedingVersion);
	superseedingTerm.getStatements(newSuperseedingVersion).add(superseedingTerm.getResource(),MetaLanguage.superseeds,myTerm.getResource());
	superseedingTerm.setActionDate(dateFormat.format(date),newSuperseedingVersion);
	superseedingTerm.linkVersions(lastSuperseedingsTermVersion,newSuperseedingVersion);
	
	
	//TODO to check versions are ok
	myRegister.unregisterContainedEntity(myTerm, newRegisterVersion, newTermVersion);
	
		//////	
	myRegister.synch();
	myTerm.synch();
	superseedingTerm.synch();
	
	
	
	}






	
}
