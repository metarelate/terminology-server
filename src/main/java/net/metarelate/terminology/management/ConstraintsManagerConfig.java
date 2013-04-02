package net.metarelate.terminology.management;

public class ConstraintsManagerConfig {
	//TODO maybe we should move all prefixes to a more general config
	//public static final String coreTypesPrefix="http://metarelate.net/core/types/";
	public static final String configPrefix="http://metarelate.net/config/";
	
	public static final String regValidationConstraintType=configPrefix+"RegisterValidationConstraint";
	public static final String regValidationCommandType= configPrefix+"RegisterValidationCommand";
	public static final String codeValidationCommandType= configPrefix+"CodeValidationCommand";
	public static final String codeValidationConstraintType= configPrefix+"CodeValidationConstraint";
	public static final String appliesTo=configPrefix+"appliesTo";	
	public static final String onObjectProperty=configPrefix+"onObjectProperty";
	public static final String onDataProperty=configPrefix+"onDataProperty";
	public static final String pseudoOrder=configPrefix+"pseudoOrder";
	public static final String minCardinality=configPrefix+"minCardinality";
	public static final String maxCardinality=configPrefix+"maxCardinality";
	public static final String oneOf=configPrefix+"oneOf";
	public static final String language=configPrefix+"language";
	public static final String hasSymmetric=configPrefix+"hasSymmetric";
	public static final String stringType=configPrefix+"String";
	public static final String numericType=configPrefix+"Numeric";
	public static final String hasValidationCommand=configPrefix+"validationCommand";
	public static final String clearCommand= configPrefix+"clear";
	public static final String inRegister = configPrefix+"inRegister";
	public static final String pattern = configPrefix+"pattern";
	public static String hasType=configPrefix+"hasType";
	public static final String allRegisters = configPrefix+"allRegisters";


}




