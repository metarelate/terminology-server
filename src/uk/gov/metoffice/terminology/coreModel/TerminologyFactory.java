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
	
package uk.gov.metoffice.terminology.coreModel;

import java.util.Collection;

/**
 * Factory responsible for the creation of terminology objects (individuals and sets).
 * The responsibility of the factory is to create objects, serve objects already created or 
 * test their existence. In perspective, it provides a single extension point for a range of optimizations: 
 * this is the only class knowing all objects and their order of creation.
 * This factory is not static as several factories may requires a parameter dependent initialization.
 * For convenience, a static method getDefaultFactory() is provided. What this returns is specified by the implementation.
 *  
 * @author Andrea Splendiani
 *
 */
public interface TerminologyFactory {
	
	/**
	 * Returns true if a Terminology Set with the specified URI already exist.
	 * @param uri
	 * @return
	 */
	public abstract boolean terminologySetExist(String uri) ;
	
	/**
	 * Returns "the" terminology set object associated to this URI, if it has been created, or creates and returns a new one.
	 * 
	 * @param uri
	 * @return
	 */
	public abstract TerminologySet getOrCreateTerminologySet(String uri) ;
	
	/**
	 * Returns "the" terminology set object associated to this URI, if it has been created, or creates and returns a new one.
	 * In additions registers the version provided and sets it as default.
	 * @param uri
	 * @param version
	 * @return
	 */
	public abstract TerminologySet getOrCreateTerminologySet(String uri, String version) ;

	/**
	 * Returns true if a Terminology Individual with the specified URI already exist.
	 * @param uri
	 * @return
	 */
	public abstract boolean terminologyIndividualExist(String uri) ;
	
	/**
	 * Returns "the" terminology individual object associated to this URI, if it has been created, or creates and returns a new one.
	 * 
	 * @param uri
	 * @return
	 */
	public abstract TerminologyIndividual getOrCreateTerminologyIndividual(String uri) ;
	
	/**
	 * Returns "the" terminology individual object associated to this URI, if it has been created, or creates and returns a new one.
	 * In additions registers the version provided and sets it as default.
	 * @param uri
	 * @param version
	 * @return
	 */
	public abstract TerminologyIndividual getOrCreateTerminologyIndividual(String uri, String version) ;

	/**
	 * Returns all terminology sets known (created) by this terminology factory.
	 * Note that once created, a terminology entity is never deleted.
	 * @return
	 */
	public abstract Collection<TerminologySet> getAllSets();
	
	/**
	 * Returns all terminology individuals known (created) by this terminology factory.
	 * Note that once created, a terminology entity is never deleted.
	 * @return
	 */
	public abstract Collection<TerminologyIndividual> getAllIndividuals();
	
	/**
	 * Returns a list of collections (sets) that are not contained in other collections.
	 * In other words, these are the top registers.
	 * In this prototype, the completeness of collections is not guaranteed unless synchRootCollections 
	 * {@link TerminologyFactory#synchRootCollections()} is called after the last method that could alter containment among registers.
	 * (Note that is usually happens only at "creation" through a builder, in the current prototype.
	 * @return
	 */
	public abstract TerminologySet[] getRootCollections() ;
	
	/**
	 * Computes which collections (set, registers) are the top collections.
	 */
	public abstract void synchRootCollections();
	
	/**
	 * Returns the label manager
	 * @return
	 */
	public LabelManager getLabelManager() ;
	
	/**
	 * Returns the background knowledge manager
	 * @return
	 */
	public BackgroundKnowledgeManager getBackgroundKnowledgeManager() ;
	
	
	
}
