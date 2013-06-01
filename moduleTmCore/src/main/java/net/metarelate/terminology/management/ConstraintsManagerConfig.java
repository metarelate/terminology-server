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




