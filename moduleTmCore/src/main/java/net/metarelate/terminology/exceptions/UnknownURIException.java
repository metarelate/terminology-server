package net.metarelate.terminology.exceptions;
/**
 * Attempt to operate on an entity which is unknown to the system
 * @author andrea_splendiani
 *
 */
public class UnknownURIException extends RegistryAccessException {

	public UnknownURIException(String message) {
		super("URI unknown: "+message);
		
	}

}
