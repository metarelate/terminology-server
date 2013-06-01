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

import java.util.Collection;
import java.util.Set;

import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;

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
	 * Returns true if a Terminology Individual with the specified URI already exist.
	 * @param uri
	 * @return
	 */
	public abstract boolean terminologyIndividualExist(String uri) ;
	
	public abstract boolean terminologyEntityExist(String uri);
	
	/**
	 * Returns "the" terminology set object associated to this URI, if it has been created, or creates and returns a new one.
	 * 
	 * @param uri
	 * @return
	 */
	//public abstract TerminologySet getOrCreateUnversionedTerminologySet(String uri) ;
	
	public abstract TerminologySet getCheckedTerminologySet(String uri) throws UnknownURIException ;
	public abstract TerminologySet getUncheckedTerminologySet(String uri) ;
	
	public abstract TerminologySet createNewVersionedTerminologySet(String uri) throws ImporterException;
	
	public abstract TerminologySet createNewUnversionedTerminologySet(String uri) throws ImporterException;
	
	
	
	public abstract TerminologyIndividual getCheckedTerminologyIndividual(String uri) throws UnknownURIException ;
	public abstract TerminologyIndividual getUncheckedTerminologyIndividual(String uri)  ;

	//public abstract TerminologySet createNewVersionedTerminologySet() ;
	public abstract TerminologyIndividual createNewVersionedTerminologyIndividual(String uri) throws ImporterException ;
	
	public abstract TerminologyIndividual createNewUnversionedTerminologyIndividual(String uri) throws ImporterException ;

	/**
	 * Returns "the" terminology set object associated to this URI, if it has been created, or creates and returns a new one.
	 * In additions registers the version provided and sets it as default.
	 * @param uri
	 * @param version
	 * @return
	 */
	//public abstract TerminologySet getOrCreateTerminologySet(String uri, String version) ;


	
	/**
	 * Returns "the" terminology individual object associated to this URI, if it has been created, or creates and returns a new one.
	 * 
	 * @param uri
	 * @return
	 */
	//public abstract TerminologyIndividual getOrCreateUnversionedTerminologyIndividual(String uri) ;
	
	/**
	 * Returns "the" terminology individual object associated to this URI, if it has been created, or creates and returns a new one.
	 * In additions registers the version provided and sets it as default.
	 * @param uri
	 * @param version
	 * @return
	 */
	//public abstract TerminologyIndividual getOrCreateTerminologyIndividual(String uri, String version) ;

	/**
	 * Returns all terminology sets known (created) by this terminology factory.
	 * Note that once created, a terminology entity is never deleted.
	 * @return
	 * @throws ModelException 
	 */
	public abstract Collection<TerminologySet> getAllSets() throws ModelException;
	
	/**
	 * Returns all terminology individuals known (created) by this terminology factory.
	 * Note that once created, a terminology entity is never deleted.
	 * @return
	 * @throws ModelException 
	 */
	public abstract Collection<TerminologyIndividual> getAllIndividuals() throws ModelException;
	
	/**
	 * Returns a list of collections (sets) that are not contained in other collections.
	 * In other words, these are the top registers.
	 * In this prototype, the completeness of collections is not guaranteed unless synchRootCollections 
	 * {@link TerminologyFactory#synchRootCollections()} is called after the last method that could alter containment among registers.
	 * (Note that is usually happens only at "creation" through a builder, in the current prototype.
	 * @return
	 * @throws ModelException 
	 */
	public abstract TerminologySet[] getRootCollections() throws ModelException ;
	
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

	public abstract Set<String> extractIndividualsWithMarchingValue(
			String textQueryString);
	public abstract Set<String> extractSetsWithMarchingValue(
			String textQueryString);

	public abstract void synch();

	public Set<TerminologySet> getRootsForURI(String uri) throws  ModelException, UnknownURIException;
	public TerminologyEntity getCheckedTerminologyEntity(String uri) throws UnknownURIException;
	public TerminologyEntity getUncheckedTerminologyEntity(String uri);

	
	
	
}
