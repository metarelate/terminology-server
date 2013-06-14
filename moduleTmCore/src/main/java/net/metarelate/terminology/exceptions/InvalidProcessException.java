package net.metarelate.terminology.exceptions;
/**
 * Represents an attempts to make an operations for which no valid pre-conditions are found
 * @author andrea_splendiani
 *
 */
public class InvalidProcessException extends ImpossibleOperationException{


		public InvalidProcessException(String actionURI, String thisState, String upState, String downState, String auxState) {
			super("Impossible transistion for action: "+actionURI+" for states: \n"
					+"this: "+thisState+"\n"
					+"container: "+upState+"\n"
					+"contained: "+downState+"\n"
					+"extra: "+auxState+"\n"
					);
		}
		
		public InvalidProcessException(String message) {
			super(message);
		}

	


}
