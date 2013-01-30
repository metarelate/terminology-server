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

//TODO Add synch calls throught! (or prblems if the server is killed!!!)

package net.metarelate.terminology.management;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.auth.AuthRegistryManager;
import net.metarelate.terminology.auth.AuthServer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDB;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.coreModel.Versioner;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

public class TerminologyManager {
	private static final int MODE_REPLACE = 1;
	private static final int MODE_ADD = 2;
	private static final int MODE_REMOVE = 3;

	
	TerminologyFactory myFactory;
	AuthRegistryManager myAuthManager;
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public TerminologyManager(TerminologyFactory factory,AuthRegistryManager myAuthManager) {
		myFactory=factory;
		this.myAuthManager=myAuthManager;
	}
	
	public void replaceEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryAccessException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_REPLACE);
	}
	public void addToEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryAccessException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_ADD);

	}
	public void removeFromEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description) throws AuthException, RegistryAccessException {
		amendEntityInformation(entityURI, statsToReplace, actionAuthor, description, MODE_REMOVE);		
	}
	
	public void amendEntityInformation(String entityURI, Model statsToReplace, String actionAuthor, String description, int mode) throws AuthException, RegistryAccessException {
		if(!myAuthManager.can(actionAuthor,RegistryPolicyConfig.terminologyAmendedActionURI,entityURI))
			throw new AuthException(actionAuthor,RegistryPolicyConfig.terminologyAmendedActionURI,entityURI);
		TerminologyEntity myEntity=checkEntityExistance(entityURI);
		if(myEntity==null) throw new RegistryAccessException("Unable to amend "+entityURI+" (entity does not exist)");
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		String postStatus=preStatus;
		if(preStatus!=null)
			if(RegistryPolicyConfig.tm.updateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.updateTransitions.get(preStatus);
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
			
		if(mode==MODE_ADD) 
			myEntity.getStatements(newVersion).add(myEntity.getStatements(lastVersion)).add(statsToReplace);
		if(mode==MODE_REMOVE)
			myEntity.getStatements(newVersion).add((myEntity.getStatements(lastVersion)).remove(statsToReplace));
		myEntity.setDefaultVersion(newVersion);
		if(postStatus!=null) myEntity.setStateURI(postStatus, newVersion);
		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.setActionURI(RegistryPolicyConfig.terminologyAmendedActionURI,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		myEntity.synch();
	}
	
	public void validate(String entityURI, String actionAuthor, String description, boolean validate) throws AuthException, RegistryAccessException {
		if(validate) {
			if(!myAuthManager.can(actionAuthor,RegistryPolicyConfig.validateAction,entityURI))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.validateAction,entityURI);
		}
		else {
			if(!myAuthManager.can(actionAuthor,RegistryPolicyConfig.invalidateAction,entityURI))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.invalidateAction,entityURI);
		}
		
		TerminologyEntity myEntity=checkEntityExistance(entityURI);
		if(myEntity==null) throw new RegistryAccessException("Unable to change status to "+entityURI+" (entity does not exist)");
		String lastVersion=myEntity.getLastVersion();
		String preStatus=myEntity.getStateURI(lastVersion);
		System.out.println(">>>PRE: "+preStatus);
		if(preStatus==null) preStatus=RegistryPolicyConfig.nullState;
		String postStatus=null;
		if(validate) {
			if(RegistryPolicyConfig.tm.validateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.validateTransitions.get(preStatus);
		} 
		if(!validate) {
			if(RegistryPolicyConfig.tm.invalidateTransitions.containsKey(preStatus))
				postStatus=RegistryPolicyConfig.tm.invalidateTransitions.get(preStatus);
		}
		
		if(postStatus==null) throw new RegistryAccessException("Unable to change status to "+entityURI+" (validation resulted didn't yield a state)");
		if(postStatus.equals(RegistryPolicyConfig.illegalState)) throw new RegistryAccessException("Unable to change status to "+entityURI+" (non viable transition)");

		String newVersion=Versioner.createNextVersion(lastVersion);
		myEntity.registerVersion(newVersion);
		myEntity.replaceStatements(myEntity.getStatements(lastVersion), newVersion);
		myEntity.setStateURI(postStatus,newVersion);
		Date date = new Date();
		myEntity.setActionDate(dateFormat.format(date),newVersion);
		myEntity.setActionAuthorURI(actionAuthor, newVersion);
		if(description!=null) myEntity.setActionDescription(description,newVersion);
		myEntity.linkVersions(lastVersion,newVersion);
		
		if(validate) 
			myEntity.setActionURI(RegistryPolicyConfig.validateAction,newVersion);

		else 
			myEntity.setActionURI(RegistryPolicyConfig.invalidateAction,newVersion);
		
		
	}
	

	

	
	public void addSubRegister() {
		
	}
	
	
	public void removeSubRegister() {
		
	}
	
	public void addTermToRegister(String codeURI, String registerURI, Model defaultEntityModel,String actionAuthor, String description, boolean isVersioned ) throws AuthException, RegistryAccessException {
	//////////////
		if(!myAuthManager.can(actionAuthor,RegistryPolicyConfig.addItemAction,registerURI))
				throw new AuthException(actionAuthor,RegistryPolicyConfig.addItemAction,registerURI);
		TerminologySet myRegister=checkSetExistance(registerURI);
		if(myRegister==null) throw new RegistryAccessException("Unable to modify "+registerURI+" (register does not exist)");
		if(myFactory.terminologyIndividualExist(codeURI)) {
			// Note this is only for addding! Not for changing obsolete/valid status. In other words, a delete operation
			throw new RegistryAccessException("Code "+codeURI+" exists. Use \"move\" to change register");
		}
			
		String lastRegisterVersion=myRegister.getLastVersion();
		String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
		String postRegisterStatus=preRegisterStatus;
		if(preRegisterStatus!=null)
			if(RegistryPolicyConfig.tm.addTransitions.containsKey(preRegisterStatus))
				postRegisterStatus=RegistryPolicyConfig.tm.addTransitions.get(preRegisterStatus);
		String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
			
		//Now we create the entity
		TerminologyIndividual newTerm=myFactory.getOrCreateTerminologyIndividual(codeURI);
		newTerm.setDefaultVersion(newTerm.getLastVersion());
		if(isVersioned) newTerm.setIsVersioned(true);	
		newTerm.setStateURI(RegistryPolicyConfig.DEFAULT_CREATION_STATE,newTerm.getDefaultVersion());
		newTerm.setOwnerURI(actionAuthor);
		newTerm.setActionURI(RegistryPolicyConfig.addItemAction ,newTerm.getDefaultVersion());
		newTerm.setActionAuthorURI(actionAuthor,newTerm.getDefaultVersion());
		newTerm.setActionDescription("New term added to registry",newTerm.getDefaultVersion());
		newTerm.addStatements(defaultEntityModel,newTerm.getDefaultVersion());
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		newTerm.setActionDate(dateFormat.format(date),newTerm.getDefaultVersion());
		
		myRegister.registerVersion(newRegisterVersion);
		myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
			
		myRegister.registerContainedIndividual(newTerm, newRegisterVersion, newTerm.getDefaultVersion());
			
		if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
		//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
		myRegister.setActionAuthorURI(actionAuthor, newRegisterVersion);
		if(description!=null) myRegister.setActionDescription("Added term: "+newTerm.getURI(),newRegisterVersion);
		myRegister.setActionURI(RegistryPolicyConfig.addItemAction ,newRegisterVersion);
		myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
			
	}
		
		
	
	

	
	public void tagRelease(String authorURI, String tag, String description) throws AuthException {
		try {
			if(!myAuthManager.can(authorURI,RegistryPolicyConfig.tagAction,null))
				throw new AuthException(authorURI,RegistryPolicyConfig.tagAction,null);
		} catch (RegistryAccessException e) {
			// TODO this shouldn't really happen in this case
			e.printStackTrace();
		}
		TerminologySet[] roots=myFactory.getRootCollections();
		for(int i=0;i<roots.length;i++) myTag(roots[i],tag);
		//TODO we may want to register infos on tags somewhere.
		
	}
	


	private TerminologyEntity checkEntityExistance(String uri) {
		if(myFactory.terminologySetExist(uri)) return myFactory.getOrCreateTerminologySet(uri);
		if(myFactory.terminologyIndividualExist(uri)) return myFactory.getOrCreateTerminologyIndividual(uri);
		return null;
	}
	
	
	private TerminologySet checkSetExistance(String uri) {
		if(myFactory.terminologySetExist(uri)) return myFactory.getOrCreateTerminologySet(uri);
		return null;
	}
	
	
	private TerminologyIndividual checkIndividualExistance(String uri) {
		if(myFactory.terminologyIndividualExist(uri)) return myFactory.getOrCreateTerminologyIndividual(uri);
		return null;
	}
	
	private void myTag(TerminologySet set, String tag) {
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

	public void delTermFromRegister(String termURI, String regURI,
			String actionAuthorURI, String description) throws AuthException, RegistryAccessException {
		
		if(!myAuthManager.can(actionAuthorURI,RegistryPolicyConfig.delItemAction,regURI))
			throw new AuthException(actionAuthorURI,RegistryPolicyConfig.delItemAction,regURI);
	TerminologySet myRegister=checkSetExistance(regURI);
	if(myRegister==null) throw new RegistryAccessException("Unable to modify "+regURI+" (register does not exist)");
	TerminologyIndividual myTerm=checkIndividualExistance(termURI);
	if(myTerm==null) throw new RegistryAccessException("Code "+termURI+" does not exists.");
	if(!myRegister.getIndividuals(myRegister.getLastVersion()).contains(myTerm))
		throw new RegistryAccessException("Code "+termURI+" is not contained in the last version of "+regURI);
	// TODO maybe we should test the containment in the register here.
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	
	String postRegisterStatus=preRegisterStatus;
	if(preRegisterStatus!=null)
		if(RegistryPolicyConfig.tm.delRegTransitions.containsKey(preRegisterStatus))
			postRegisterStatus=RegistryPolicyConfig.tm.delRegTransitions.get(preRegisterStatus);
	
	String postTermStatus=preTermStatus;
	if(preTermStatus!=null)
		if(RegistryPolicyConfig.tm.delTermTransitions.containsKey(preTermStatus))
			postTermStatus=RegistryPolicyConfig.tm.delTermTransitions.get(preTermStatus);
	System.out.println(">>>pre Term Status: "+preTermStatus);
	System.out.println(">>>post Term Status: "+postTermStatus);
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description!=null) myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyConfig.delItemAction ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	myTerm.registerVersion(newTermVersion);
	
	

		
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyConfig.delItemAction ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	myTerm.setActionDescription("Term was removed from registry",newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion); // Note this overwrites old statements!
	myTerm.linkVersions(lastTermVersion,newTermVersion);

	myRegister.unregisterContainedIndividual(myTerm, newRegisterVersion, newTermVersion);
	
		//////
		
	}
	
	
	
	public void superseedTermInRegister(String termURI,
			String superseedingTermURI, String regURI, 
			String actionAuthorURI, String description) throws AuthException, RegistryAccessException {
		// TODO Auto-generated method stub
		
		if(!myAuthManager.can(actionAuthorURI,RegistryPolicyConfig.superseedAction,regURI))
			throw new AuthException(actionAuthorURI,RegistryPolicyConfig.superseedAction,regURI);
	TerminologySet myRegister=checkSetExistance(regURI);
	if(myRegister==null) throw new RegistryAccessException("Unable to modify "+regURI+" (register does not exist)");
	
	TerminologyIndividual myTerm=checkIndividualExistance(termURI);
	if(myTerm==null) throw new RegistryAccessException("Code "+termURI+" does not exists.");
	
	TerminologyIndividual superseedingTerm=checkIndividualExistance(superseedingTermURI);
	if(superseedingTerm==null) throw new RegistryAccessException("Code "+superseedingTermURI+" does not exist. Add it first.");
	
	
	
	String lastRegisterVersion=myRegister.getLastVersion();
	String lastTermVersion=myTerm.getLastVersion();
	String lastSuperseedingsTermVersion=superseedingTerm.getLastVersion();
	
	String preRegisterStatus=myRegister.getStateURI(lastRegisterVersion);
	String preTermStatus=myTerm.getStateURI(lastTermVersion);
	String preSuperseedingTermStatus=superseedingTerm.getStateURI(lastSuperseedingsTermVersion);

	
	String newRegisterVersion=Versioner.createNextVersion(lastRegisterVersion);
	String newTermVersion=Versioner.createNextVersion(lastTermVersion);
	String newSuperseedingVersion=Versioner.createNextVersion(lastSuperseedingsTermVersion);

	String postRegisterStatus=preRegisterStatus;
	if(preRegisterStatus!=null)
		if(RegistryPolicyConfig.tm.delRegTransitions.containsKey(preRegisterStatus))
			postRegisterStatus=RegistryPolicyConfig.tm.delRegTransitions.get(preRegisterStatus);

	String postTermStatus=preTermStatus;
	if(preTermStatus!=null)
		if(RegistryPolicyConfig.tm.superseededTransitions.containsKey(preTermStatus))
			postTermStatus=RegistryPolicyConfig.tm.superseededTransitions.get(preTermStatus);

	String postSuperseedingTermStatus=preSuperseedingTermStatus;
	if(preSuperseedingTermStatus!=null)
		if(RegistryPolicyConfig.tm.superseederTransitions.containsKey(preSuperseedingTermStatus))
			postSuperseedingTermStatus=RegistryPolicyConfig.tm.superseederTransitions.get(preSuperseedingTermStatus);
	if(postSuperseedingTermStatus.equals(RegistryPolicyConfig.illegalState)) throw new RegistryAccessException(superseedingTermURI+" is not in a viable state for superseeding "+termURI);

	
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();
	
	myRegister.registerVersion(newRegisterVersion);
	myRegister.getStatements(newRegisterVersion).add((myRegister.getStatements(lastRegisterVersion)));
	if(postRegisterStatus!=null) myRegister.setStateURI(postRegisterStatus, newRegisterVersion);
	myRegister.setActionDate(dateFormat.format(date),newRegisterVersion);
	myRegister.setActionAuthorURI(actionAuthorURI, newRegisterVersion);
	if(description!=null) myRegister.setActionDescription("Deleted term: "+myTerm.getURI(),newRegisterVersion);
	myRegister.setActionURI(RegistryPolicyConfig.delItemAction ,newRegisterVersion);
	myRegister.linkVersions(lastRegisterVersion,newRegisterVersion);
	
	
	
	myTerm.registerVersion(newTermVersion);
	myTerm.setStateURI(postTermStatus,newTermVersion);
	myTerm.setOwnerURI(actionAuthorURI);
	myTerm.setActionURI(RegistryPolicyConfig.superseedAction ,newTermVersion);
	myTerm.setActionAuthorURI(actionAuthorURI,newTermVersion);
	myTerm.setActionDescription("Term was removed from registry and superseeded by "+superseedingTermURI,newTermVersion);
	myTerm.addStatements(myTerm.getStatements(lastTermVersion),newTermVersion);
	myTerm.getStatements(newTermVersion).add(myTerm.getResource(),MetaLanguage.superseededBy,superseedingTerm.getResource());
	myTerm.setActionDate(dateFormat.format(date),newTermVersion);
	myTerm.linkVersions(lastTermVersion,newTermVersion);


	superseedingTerm.registerVersion(newSuperseedingVersion);
	superseedingTerm.setStateURI(postTermStatus,newSuperseedingVersion);
	superseedingTerm.setActionURI(RegistryPolicyConfig.superseedAction ,newSuperseedingVersion);
	superseedingTerm.setActionAuthorURI(actionAuthorURI,newSuperseedingVersion);
	superseedingTerm.setActionDescription("Term superseeded "+termURI,newSuperseedingVersion);
	superseedingTerm.addStatements(superseedingTerm.getStatements(lastSuperseedingsTermVersion),newSuperseedingVersion);
	superseedingTerm.getStatements(newSuperseedingVersion).add(superseedingTerm.getResource(),MetaLanguage.superseeds,myTerm.getResource());
	superseedingTerm.setActionDate(dateFormat.format(date),newSuperseedingVersion);
	superseedingTerm.linkVersions(lastSuperseedingsTermVersion,newSuperseedingVersion);
	
	
	
	myRegister.unregisterContainedIndividual(myTerm, newRegisterVersion, newTermVersion);
	
		//////	
	
	
	
	
	}


	
}
