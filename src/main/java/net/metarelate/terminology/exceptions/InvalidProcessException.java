package net.metarelate.terminology.exceptions;

public class InvalidProcessException extends ImpossibleOperationException{


		public InvalidProcessException(String actionURI, String thisState, String upState, String downState, String auxState) {
			super("Impossible tranistion for action: "+actionURI+" for states: \n"
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
