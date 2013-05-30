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
	
package net.metarelate.terminology.coreModel;

import java.util.Set;

import net.metarelate.terminology.exceptions.ModelException;

public interface TerminologyIndividual extends TerminologyEntity{
	/**
	 * remove this individuals from the list of individuals known by the collection at the given version
	 * TODO note: in this prototype this method is unimplemented and has no effect.
	 * TODO possibly redundant: should this method be removed ?
	 * @param collection
	 * @param version
	 */
	void unregisterContainerCollection(TerminologySet collection, String version);
	
	/**
	 * Copy all known information about this individual, for all versions, to the new term
	 * TODO add detail. Can copy of statements be by reference ?
	 * @param toTerm
	 */
	public void cloneTo(TerminologyIndividual toTerm) ;
	
	/**
	 * get all registers (collections, sets) in which this individual is "defined" (or contained).
	 * While the system is designed so that an individual can have more than one containment,
	 * rendering and authority assume that a code is defined in only one register.
	 * @return
	 * @throws ModelException 
	 */
	public  Set<TerminologySet> getContainers() throws ModelException ;
	
}
