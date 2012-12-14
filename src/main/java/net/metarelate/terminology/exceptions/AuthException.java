package net.metarelate.terminology.exceptions;
/**
 * An exception representing an authorization violation.
 * 
 * This is thrown when a problem in the authorization server is encountered.
 * For instance, if the Authorization server is unavailable.
 * This is not en exception related to the lack of privileges to perform an operation.
 * 
 * 
 * @author andreasplendiani
 *
 */
public class AuthException extends Exception {

	public AuthException(String actionAgent,
			String actionURI, String entityURI) {
		super(actionAgent+" is not allowed to perform "+actionURI+" on "+entityURI);
	}
	
	public AuthException(String message) {
		super(message);
	}

}
