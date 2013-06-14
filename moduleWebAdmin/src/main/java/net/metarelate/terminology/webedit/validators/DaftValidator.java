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


package net.metarelate.terminology.webedit.validators;

import java.io.Serializable;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * We sidestep WicketValidator with this object in place of AbstractFormValidator,
 * as this seems to be too form field specific for what we need!
 * @author andreasplendiani
 *
 */
public interface DaftValidator extends Serializable{
	/**
	 * @param m the set of statements to validate (as a Model)
	 * @return true if the model is valid
	 */
	boolean validate(Model m);
	/**
	 * @return a validation message
	 */
	String getMessage();
}
