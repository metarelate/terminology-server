package net.metarelate.terminology.exceptions;

/**
 * Represents an inconsistency of the terminology model (e.g.: multiple fathers)
 * @author andrea_splendiani
 *
 */
public class ModelException extends Exception {
	public ModelException(String message) {
		super(message);
	}

	

}
