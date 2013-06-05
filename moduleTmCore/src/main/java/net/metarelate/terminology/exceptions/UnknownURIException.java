package net.metarelate.terminology.exceptions;

public class UnknownURIException extends RegistryAccessException {

	public UnknownURIException(String message) {
		super("URI unknown: "+message);
		
	}

}
