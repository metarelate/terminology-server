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
	
package net.metarelate.terminology.coreModel;

import java.util.Set;

import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface TerminologyEntity extends Visitee {
	//final int INDIVIDUAL_TYPE = 1;	// Note: these are not meant to be set! they are only constants to be used...
	//final int SET_TYPE=2;			// TODO  the design should be changed to avoid confusion
	/**
	 * Returns the URI representing the entity
	 * @return the URI string (not null and not by design)
	 */
	public String getURI();
	
	/**
	 * Returns a Jena resource representing the entity
	 * @return A Jena Resource (URIResource)
	 */
	public Resource getResource();
	
	/**
	 * @return the factory that generated this object.
	 */
	public TerminologyFactory getFactory();
	
	/**
	 * Returns the label for this entity, for a given version.
	 * The language tag is ignored, and the first (random) label, in any language, is returned.
	 * If no labels are found, null is returned.
	 * Note that the labels for the entity are handled differently from labels for properties and 
	 * "other" entities not defined in registers. These are in fact not part of the "curated" register information, and are handled 
	 * by the {@link uk.gov.metoffice.terminology.LabelManager}
	 * 
	 * @param version
	 * @return the label for this entity.
	 */
	public String getLabel(String version) ;

	
	/**
	 * Returns the label for this entity, for a given version and a give language.
	 * The first (random) label matching the language is returned.
	 * If no labels matching the language is found, the first (random) label matching
	 * the default language (defined in {@link uk.gov.metoffice.terminology.config.CoreConfig}) is returned.
	 * If no match is found, a search without language tag is performed.
	 * If it is not possible to find a local namespace, the method may return the empty string or whole URI.
	 * TODO this is possibly something to be fixed with a default convention. 
	 * @param version
	 * @param language (language tag as in RDF expressions)
	 * @return
	 */
	public String getLabel(String version, String language) ;
	
	
	/**
	 * Set the local namespace for this entity. This is the string that characterize this entity in the URI.
	 * For instance, if the entity represent the register C1, and the local namespace is set to "C-1", the URI for 
	 * the code 001 in this register would appear like: .../C-1/001. (In this example, the local namespace for the code would have been 001).
	 * The local namespace is consumed while the resource is being constructed by the RDF builder, and it is never stored.
	 * The corresponding get method extracts the local namespace from the URI of the entity, assuming that the local namespace is used in a 
	 * /localns/ pattern.
	 * This "asymmetry" may be more cleanly articulated in a different way. For instance by having a setter, but not a getter method.
	 * 
	 * @param lns
	 */
	public void setLocalNamespace(String lns);
	
	/**
	 * Returns the local namespace of this entity, as extracted from the URI.
	 * @see {@link uk.gov.metoffice.terminology.TerminologyEntity#setLocalNamespace}
	 * TODO what if null ?
	 * @return
	 */
	public String getLocalNamespace();
	
	String getLastURIBit() ;
	
	/**
	 * Returns the notation for this entity for a given version, null if the notation is not defined.
	 * The "notation" property is specified in {@link uk.gov.metoffice.terminology.config.MetaLanguage}
	 * @param version
	 * @return
	 */
	public String getNotation(String version) ;
	
	
	/**
	 * Returns the containers of this entity for a given version. 
	 * Containers are registers for entities, or super-registers for other registers (also referred to as "containers").
	 * While multiple containers are possible in the terminology design, the behavior of the system respect to rendering and permission management is undefined
	 * if more than one parent container are present.
	 * @param version
	 * @return 
	 * @throws ModelException 
	 */
	public abstract Set<TerminologySet> getContainers(String version) throws ModelException;
	
	

	/**
	 * Set the status (uri) for a given version.
	 * The status may in principle be any URI, but only the ones defined in {@link uk.gov.metoffice.terminology.config.MetaLanguage}
	 * guarantee a consistent state management respect to register operations. 
	 * States could be easily extended @see {@link RegistryPolicyManager.gov.metoffice.terminology.management.RegistryPolicyConfig} and @see {@link uk.gov.metoffice.terminology.config.MetaLanguage}
	 * @param uri a uri corresponding to a specific state.
	 * @param version
	 */
	public  abstract void setStateURI(String uri, String version);
	
	/**
	 * Return the uri for the state of this entity for a given version.
	 * @param version
	 * @return
	 */
	public  abstract String getStateURI(String version);
	
	/**
	 * Set the owner of this entity, that is specified via his/her/its uri. The owner of an entity is not depending on the version.
	 * Note that this aspect could be changed if the register is to support "transfer of ownership" across institutions.
	 * @param uri
	 */
	public abstract void setOwnerURI(String uri);
	
	/**
	 * Returns the owner of a an entity (version independant).
	 * Returns null if know owner is set.
	 * @param uri the uri representing the owner of the entity
	 * @return
	 */
	public abstract String getOwnerURI() ;

	/**
	 * Set the action that lead to the given version (the action is identified by its uri).
	 */
	public abstract void setActionURI(String actionURI, String version);
	
	/**
	 * Returns the uri representing the action that lead to the given version, null if this is unknown.
	 * @param version
	 * @return
	 */
	public abstract String getActionURI(String version);
	
	/**
	 * Set the author (agent) of the action that lead the given version (the actor is identified by his/her/its uri).
	 * @param version
	 */
	public abstract void setActionAuthorURI(String actionAuthorURI, String version);
	
	/**
	 * Returns the uri representing the author (agent) that action that lead to the current version.
	 * Returns null if no author (agent) is set.
	 * @param actionAuthorURI
	 * @param version
	 * @return
	 */
	public abstract String getActionAuthorURI(String version);
	
	/**
	 * Set the date of the action that lead the given version.
	 * There is no commitment in the date format at this stage.
	 * @param version
	 */
	public abstract void setActionDate(String actionDate, String version);
	
	/**
	 * Returns the date of the action that lead to the current version.
	 * @param version
	 * @return
	 */
	public abstract String getActionDate(String version);
	
	/**
	 * Sets the description of the action that lead to the current version (free text for human consumption).
	 * Returns null if no date is set.
	 * @param actionDescription
	 * @param version
	 */
	public abstract void setActionDescription(String actionDescription, String version); 
	
	/**
	 * A text description of the action that lead to the current version.
	 * Returns null if no description is set.
	 * @param version
	 * @return
	 */
	public abstract String getActionDescription(String version) ; 

	/**
	 * Sets the default version for this entity.
	 * The default version is used throughout the system whenever a version is not specified.
	 * The main intended usage is to facilitate a set of operations on the same version.
	 * Often confusion may arise between the "last version" and the "default version", especially
	 * after operations that generate a new versions: the last version generated is not always the default version!
	 * TODO what if it doesn't exist ?
	 * @param version
	 */
	//public void setDefaultVersion(String version) ;
	
	/**
	 * Gets the "default" version for this entity, that is the version set as such!
	 * This information is held in memory and doesn't necessarily last across opearations on the terminology server.
	 * @see {@link uk.gov.metoffice.TerminologyEntity.TerminologyEntityInterf}
	 * TODO what if it was not set ?
	 * 
	 * @return
	 */
	//public String getDefaultVersion() ;
	
	/**
	 * Activates versioning for this entity. The system is designed so that entities are either under versioning or not.
	 * Non-versioned entities corresponds basically to sets of registers that are immutable, or whose changes don't carry a particular value.
	 * The system is not designed to switch on/off versioning repeatedly, and there may be bugs arising if the system is used as such.
	 * @return the default version (string, not URI) or null if no default version is set "in memory".
	 */
	public abstract void setIsVersioned(boolean isVersioned);
	
	/**
	 * whether the entity state is versioned or not.
	 * @see {@link uk.gov.metoffice.terminology.TerminologyEntityInfer#setIsVersioned}
	 * @return
	 */
	public abstract boolean isVersioned();
	
	
	/**
	 * Returns an array containing all the versions (strings, not URIs).
	 * The versions returned are not necessarily in order. 
	 * TODO to verify
	 * @return
	 */
	public abstract String[] getVersions();
	
	/**
	 * Returns an array containing version and all its preceding versions
	 * (inverse chronological order)
	 * @return
	 */
	public abstract String[] getVersionsChainFor(String version);
	
	/**
	 * Return an URI representation of the entity for a given version.
	 * @param version
	 * @return
	 */
	public String getVersionURI(String version) ;
	
	/**
	 * Assigns a tag to a version of this entity. More tags can be associated to the same versions.
	 * Agent/Action/Description for a tag action are not recorded in this prototype. 
	 * @param version
	 * @param tag
	 */
	public abstract void tagVersion(String version,String tag);
	
	/**
	 * Returns an array of versions associated to the given tag.
	 * @param tag
	 * @return
	 */
	public abstract String[] getVersionsForTag(String tag);
	
	/**
	 * returns an array of versions (not uris) associated to the given tag.
	 * TODO Note: this is unimplemented in this prototype! It always returns null.
	 * @param tag
	 * @return
	 */
	public abstract String[] getTagsForVersion(String tag);
	
	/**
	 * get the number of version. Equivalent to asking to the size of the array returned by 
	 * {@link uk.gov.metoffice.terminology.TerminologyEntity#getVersions}
	 * @return
	 */
	public abstract int getNumberOfVersions();
	
	
	/**
	 * Add a new version to the current entities. Each version is considered as an independent container of statements, and links between versions
	 * have to be stated explicitly.
	 * @param version
	 */
	public abstract void registerVersion(String version) ;
	
	/**
	 * return true if the given version is the last version known by the system.
	 * For more information on how the "last" version is computed, refer to 
	 * {@link uk.gov.metoffice.terminology.Versioner}
	 * @param version
	 * @return
	 */
	public boolean isLastVersion(String version) ;
	
	/**
	 * returns the version perceding the given version.
	 * For more information on how the "preceding" version is computed, refer to 
	 * {@link uk.gov.metoffice.terminology.Versioner}
	 * @param version the version string (not URI)
	 * @return The version string (not URI), null if this is the first version TODO verify
	 */
	public abstract String getPreviousVersion(String version) ;
	
	/**
	 * returns the version following the given version.
	 * For more information on how the "following" version is computed, refer to 
	 * {@link uk.gov.metoffice.terminology.Versioner}
	 * @param version the version string (not URI)
	 * @return The version string (not URI), null if this is the last version TODO verify
	 */
	public abstract String getNextVersion(String version) ;

	/**
	 * returns true if a version preceding this version is known, false otherwise.
	 */
	public boolean hasPreviousVersion(String version);

	/**
	 * returns true if no version following this version are known, false otherwise.
	 */
	public String getLastVersion() ;

	/**
	 * returns the date of the "first" version known.
	 * For more information on how the "first" version is computed, refer to 
	 * {@link uk.gov.metoffice.terminology.Versioner}
	 * @return
	 */
	public String getGenerationDate() ;
	
	/**
	 * returns the date of the "last" version known.
	 * For more information on how the "last" version is computed, refer to 
	 * {@link uk.gov.metoffice.terminology.Versioner}
	 * @return
	 */
	public String getLastUpdateDate();
	
	/**
	 * Establish a link between two version: <b>newVersion</b> is recorded as following <b>lastVersion</b> (that is considered preceding <b>newVersion</b>).
	 * This linkage is (potentially) used to compute relations among versions (e.g.: first, latest).
	 * For more information @see {@link uk.gov.metoffice.terminology.Versioner}.
	 * @param lastVersion
	 * @param newVersion
	 */
	void linkVersions(String lastVersion, String newVersion) ;
	
	/**
	 * Replaces all the statements known for a given version with a new statement block.
	 * @param statementsAsModel a Jena Model containing the set of statement replacing the pevious ones.
	 * @param version the version affected.
	 */
	public abstract void replaceStatements(Model statementsAsModel, String version);
	
	/**
	 * Add statements to the statements known for a given version with a new statement block.
	 * @param statementsAsModel a Jena Model containing the statements to be added.
	 * @param version the version affected.
	 */
	public abstract void addStatements(Model statementsAsModel, String version);
	
	/**
	 * Return all statements that were known at a given version.
	 * @param version
	 * @return a Jena Model containing all statements known at "version"
	 */
	public abstract Model getStatements(String version);
		
	String getGenericVersionSpecificStringValueObjectByLanguage(Property property, String version, String Langauge) ;
	String getGenericVersionSpecificStringValueObject(Property property, String version);
	String getGenericVersionSpecificURIObject(Property property, String version);
	String getGenericEndurantStringValueObject(Property property);
	String getGenericEndurantURIObject(Property property);

	public abstract void synch();

	
	public boolean isSet();
	
	public boolean isIndividual();

	public Set<Resource> getGenericVersionSpecificURIObjects(Property typeProperty, String version);
	
}
