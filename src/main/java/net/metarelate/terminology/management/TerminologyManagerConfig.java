package net.metarelate.terminology.management;

// TODO ideally, these fields shuld have paxckage visibility.
public class TerminologyManagerConfig {
	/**
	 * Builtin states
	 */
	final public static String defaultStateURI="http://metarelate.net/core/core/states/default";
	final public static String obsoletedStateURI="http://metarelate.net/core/core/states/obsoleted";
	final public static String supersededStateURI="http://metarelate.net/core/core/states/superseded";
	
	/**
	 * Builtin actions
	 * 
	 */
	final public static String updateActionURI="http://metarelate.net/core/actions/update";
	final public static String obsoleteActionURI="http://metarelate.net/core/actions/obsolete";
	final public static String supersedActionURI="http://metarelate.net/core/actions/supersed";
	final public static String addActionURI="http://metarelate.net/core/actions/add";
	
	/**
	 * Transition definition language
	 */
	final public static String confActionType="http://metarelate.net/core/types/action";
	final public static String confStateType="http://metarelate.net/core/types/state";
	final public static String confRoleType="http://metarelate.net/core/types/role";
	
	final public static String confOverrides="http://metarelate.net/core/config/overrides";
	final public static String confEffectOnReg="http://metarelate.net/core/config/hasEffectOnReg";
	final public static String confEffectOnCode="http://metarelate.net/core/config/hasEffectOnCode";
	
	final public static String confPreThis="http://metarelate.net/core/config/preThis";
	final public static String confPreUp="http://metarelate.net/core/config/preUp";
	final public static String confPreDown="http://metarelate.net/core/config/preDown";
	final public static String confPreAux="http://metarelate.net/core/config/preAux";
	
	final public static String confPostThis="http://metarelate.net/core/config/postThis";
	final public static String confPostUp="http://metarelate.net/core/config/postUp";
	final public static String confPostDown="http://metarelate.net/core/config/postDown";
	final public static String confPostAux="http://metarelate.net/core/config/postAux";

}
