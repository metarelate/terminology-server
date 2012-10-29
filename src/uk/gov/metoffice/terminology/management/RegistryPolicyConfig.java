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

package uk.gov.metoffice.terminology.management;

import java.util.Hashtable;
import java.util.Map;
import uk.gov.metoffice.terminology.config.MetaLanguage;

public class RegistryPolicyConfig {
	public static final String terminologyAmendedActionURI=MetaLanguage.terminologyUpdateAction.getURI();
	public static final String addItemAction = MetaLanguage.terminologyAddItemAction.getURI();
	public static final String delItemAction = MetaLanguage.terminologyDelItemAction.getURI();
	public static final String superseedAction = MetaLanguage.terminologySupersedAction.getURI();
	public static final String validateAction=MetaLanguage.terminologyValidateAction.getURI();
	public static final String invalidateAction=MetaLanguage.terminologyInvalidateAction.getURI();
	
	
	
	public static final String DEFAULT_CREATION_STATE = MetaLanguage.statusWMOValidationResource.getURI();
	static final String illegalState = MetaLanguage.statusIllegalResource.getURI();
	public static final String nullState = MetaLanguage.statusNullResource.getURI();
	public static String tagAction=MetaLanguage.tagAction.getURI();
	
	public static RegistryPolicyConfig tm=new RegistryPolicyConfig();
	
	
	
	Map<String,String> updateTransitions=new Hashtable<String,String>();
	Map<String,String> addTransitions=new Hashtable<String,String>();
	Map<String,String> delTermTransitions=new Hashtable<String,String>();
	Map<String,String> delRegTransitions=new Hashtable<String,String>();
	Map<String,String> superseederTransitions=new Hashtable<String,String>();
	Map<String,String> superseededTransitions=new Hashtable<String,String>();
	Map<String,String> validateTransitions=new Hashtable<String,String>();
	Map<String,String> invalidateTransitions=new Hashtable<String,String>();
	
	
	RegistryPolicyConfig() {
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

		
		
		
		superseederTransitions.put(MetaLanguage.statusWMOValidResource.getURI(),RegistryPolicyConfig.illegalState);
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
