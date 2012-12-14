package net.metarelate.terminology.auth;

/**
 * Controls permissions to operate the register.
 * 
 * 
 * @author andreasplendiani
 *
 */
 public abstract class AuthServer {
	/**
	 * Answers whether a given "agent" is authorized to perform a given "action" on the target entity, that could be a register or a code.
	 * The all URI {@link net.metarelate.terminology.auth.AuthConfig#allURI} can be used to specify generic authorizations (e.g.: agent-action-all).
	 * Not that AuthServer return true if the action is authorized and false if the action is not known to be authorized, that is different from being forbidden.
	 * For instance let's assume an agent A can perform operation P on registers X,Y, that registers X,Y are sub-registers of Z, and that the AuthServer "knows" that A-P-Z is allowed.
	 * The AuthServer would then return true for A-P-Z and false for A-P-X and A-P-Y.
	 * However, this doesn't imply that A-P-X is not allowed, but that it is not known A-P-X is allowed.
	 * The AuthManager instead would return true for A-P-X and A-P-Y and false for, example, A-P-W. In this case false means the operations is not allowed as authortization for A-P-W cannot be found in ant register or super-register.
	 *
	 * 
	 * @param agent the URI of the agent performing the action. Through the code the agent is also referred to as "actor" or "actorURI".
	 * @param action the URI of the action
	 * @param entity the URI of the target entity
	 * @return true if the operation is authorized, false if unknown.
	 */
	public abstract boolean contains(String agent,String action, String entity);
}
