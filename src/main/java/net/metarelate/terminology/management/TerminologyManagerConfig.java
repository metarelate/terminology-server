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

import net.metarelate.terminology.config.MetaLanguage;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

// TODO ideally, these fields shuld have paxckage visibility.
public class TerminologyManagerConfig {
	/**
	 * Builtin states
	 */
	final public static String defaultStateURI="http://metarelate.net/states/default";
	final public static String obsoletedStateURI="http://metarelate.net/states/obsoleted";
	final public static String supersededStateURI="http://metarelate.net/states/superseded";
	
	/**
	 * Builtin actions
	 * 
	 */
	final public static String updateActionURI="http://metarelate.net/actions/update";
	final public static String obsoleteActionURI="http://metarelate.net/actions/obsolete";
	final public static String supersedActionURI="http://metarelate.net/actions/supersed";
	final public static String addActionURI="http://metarelate.net/actions/add";
	
	public static final Resource tagAction=ResourceFactory.createResource("http://metarelate.net/actions/tag");
	
	
	/**
	 * Transition definition language
	 */
	final public static String confActionType="http://metarelate.net/config/Action";
	final public static String confStateType="http://metarelate.net/config/State";
	final public static String confRoleType="http://metarelate.net/config/Role";
	
	final public static String confOverrides=MetaLanguage.overridesPropertyString; 
	final public static String confEffectOnReg="http://metarelate.net/config/hasEffectOnReg";
	final public static String confEffectOnCode="http://metarelate.net/config/hasEffectOnCode";
	
	final public static String confPreThis="http://metarelate.net/config/preThis";
	final public static String confPreUp="http://metarelate.net/config/preUp";
	final public static String confPreDown="http://metarelate.net/config/preDown";
	final public static String confPreAux="http://metarelate.net/config/preAux";
	
	final public static String confPostThis="http://metarelate.net/config/postThis";
	final public static String confPostUp="http://metarelate.net/config/postUp";
	final public static String confPostDown="http://metarelate.net/config/postDown";
	final public static String confPostAux="http://metarelate.net/config/postAux";

}
