/* 
 (C) British Crown Copyright 2011 - 2012, Met Office

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

package net.metarelate.terminology.exceptions;
/**
 * An exception representing an authorization violation.
 * 
 * This is thrown when a problem in the authorization server is encountered.
 * For instance, if the Authorization server is unavailable.
 * This is not en exception related to the lack of privileges to perform an operation.
 * 
 * 
 * @author andrea_splendiani
 *
 */
public class AuthException extends ImpossibleOperationException {

	public AuthException(String actionAgent,
			String actionURI, String entityURI) {
		super(actionAgent+" is not allowed to perform "+actionURI+" on "+entityURI);
	}
	
	public AuthException(String message) {
		super(message);
	}

}
