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

package uk.gov.metoffice.terminology.exceptions;
/**
 * Non Conform RDF
 *
 * The exception is risen when some expected RDF constructs are not present.
 * This exception is currently rarely used, with the only primary reference in {@link uk.metoffice.terminology.utils.SimpleQueriesProcessor} which is scheduled to be obsolete.
 * However, this exception may be used through the code where relevant queries to the RDF model fail.
 * It is therefore maintained.
 * 
 * @author andreasplendiani
 *
 */
public class NonConformantRDFException extends Exception{
	public NonConformantRDFException(String message) {
		super(message);
	}
}
