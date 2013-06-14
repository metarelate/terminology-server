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
/**
 * TODO validation should be moved in its own package
 */
package net.metarelate.terminology.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.PropertyConstraintException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Manages validation rules
 * @author andrea_splendiani
 *
 */
public class ConstraintsManager {
	private Initializer myInitializer=null;
	private Model inputConfig=null;
	private Map<String,Set<Resource>> uriRegConstraints=null;
	private Map<String,Set<Resource>> uriCodeConstraints=null;
	private Set<Resource> defaultRegConstraints=null;
	private Set<Resource> defaultCodeConstraints=null;
	
	public ConstraintsManager(Initializer initializer) throws ConfigurationException {
		myInitializer=initializer;
		inputConfig=initializer.getConfigurationGraph();
		uriRegConstraints=new Hashtable<String,Set<Resource>>();
		uriCodeConstraints=new Hashtable<String,Set<Resource>>();
		defaultRegConstraints=new HashSet<Resource>();
		defaultCodeConstraints=new HashSet<Resource>();
		parseInput();
	}
	
	
	private void parseInput() throws ConfigurationException {
		parseInputPerSpecies(ConstraintsManagerConfig.regValidationConstraintType,defaultRegConstraints,uriRegConstraints);
		parseInputPerSpecies(ConstraintsManagerConfig.codeValidationConstraintType,defaultCodeConstraints,uriCodeConstraints);
		for(Resource c:defaultRegConstraints) Loggers.validationLogger.info("Found generic constraint for register: "+c.getURI());
		for(Resource c:defaultCodeConstraints) Loggers.validationLogger.info("Found generic constraint for register: "+c.getURI());
		for(String c:uriRegConstraints.keySet()) {
			for(Resource c2:uriRegConstraints.get(c)) Loggers.validationLogger.info("specific constraint for reg: "+c+" : "+c2);
		}
		for(String c:uriCodeConstraints.keySet()) {
			for(Resource c2:uriCodeConstraints.get(c)) Loggers.validationLogger.info("specific constraint for code: "+c+" : "+c2);
		}
	}
	
	
	private void parseInputPerSpecies(String regOrCodeType, Set<Resource> defaultConstraints, Map<String,Set<Resource>> specificConstraints) throws ConfigurationException {
		ResIterator genericConstrainsIter=inputConfig.listSubjectsWithProperty(
				ResourceFactory.createProperty(MetaLanguage.typeProperty.getURI()),
				ResourceFactory.createResource(regOrCodeType)
				); //TODO a bit redundant, but Properties should disappear from Config Files at some point.
		while(genericConstrainsIter.hasNext()) {
			Resource currentGenericConstraint=genericConstrainsIter.next();
			NodeIterator appliesToIter=inputConfig.listObjectsOfProperty(
					currentGenericConstraint,
					ResourceFactory.createProperty(ConstraintsManagerConfig.appliesTo)
					);
			if(!appliesToIter.hasNext()) defaultConstraints.add(currentGenericConstraint);
			else {
				RDFNode tentativeBind=appliesToIter.nextNode();
				if(tentativeBind.isResource()) {
					if(!specificConstraints.containsKey(tentativeBind.asResource().getURI())) {
						specificConstraints.put(tentativeBind.asResource().getURI(), new HashSet<Resource>());
						
					}
					specificConstraints.get(tentativeBind.asResource().getURI()).add(currentGenericConstraint);
				}
				else throw new ConfigurationException("Something is wrong in the configuration for "+currentGenericConstraint.getURI());
			}
			
		}
	}
	
	/**
	 * Returns a sorted array of constraints (identified by their URI) for the register with the specified URI
	 * @param uri
	 * @return
	 * @throws ConfigurationException
	 * @throws UnknownURIException
	 * @throws ModelException
	 */
	public String[] getSortedConstraintsForReg(String uri) throws ConfigurationException, UnknownURIException, ModelException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(uri);
		Set<TerminologySet> containers=entity.getContainers(entity.getLastVersion());
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultRegConstraints, 
				uriRegConstraints,
				ConstraintsManagerConfig.regValidationCommandType);
		Loggers.validationLogger.info("For edit register found #rules: "+results.size());
		return makeSortedProperties(results);
		
	}
	
	/**
	 * returns a sorted array of constraints (identified by their URI) for the codes in the register with the specified URI
	 * @param uri
	 * @return
	 * @throws ConfigurationException
	 * @throws UnknownURIException
	 * @throws ModelException
	 */
	public String[] getSortedConstraintsForCode(String uri) throws ConfigurationException, UnknownURIException, ModelException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(uri);
		Set<TerminologySet> containers=entity.getContainers(entity.getLastVersion());
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultCodeConstraints, 
				uriCodeConstraints,
				ConstraintsManagerConfig.codeValidationCommandType);
		Loggers.validationLogger.info("For edit code found #rules: "+results.size());
		return makeSortedProperties(results);
	}
	
	/**
	 * Returns a list of sorted constraints for a new register (not yet created)
	 * @param baseRegURI the register in which this (sub)register will be defined
	 * @return
	 * @throws ConfigurationException
	 * @throws UnknownURIException
	 * @throws ModelException
	 */
	public String[] getSortedConstraintsForNewReg(String baseRegURI) throws ConfigurationException, UnknownURIException, ModelException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(baseRegURI);
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultRegConstraints, 
				uriRegConstraints,
				ConstraintsManagerConfig.regValidationCommandType);
		Loggers.validationLogger.info("For new regsiter found #rules: "+results.size());
		return makeSortedProperties(results);
	}
	
	/**
	 * Returns a list of sorted constraints for a new code (not yet created)
	 * @param baseRegURI the register in which this code will be defined
	 * @return
	 * @throws ConfigurationException
	 * @throws UnknownURIException
	 * @throws ModelException
	 */
	public String[] getSortedConstraintsForNewCode(String baseRegURI) throws ConfigurationException, UnknownURIException, ModelException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getCheckedTerminologyEntity(baseRegURI);
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultCodeConstraints, 
				uriCodeConstraints,
				ConstraintsManagerConfig.codeValidationCommandType);
		Loggers.validationLogger.info("For new code found #rules: "+results.size());
		return makeSortedProperties(results);
	}
	
	/**
	 * Given a list of propertis, returns an array where these are sorted following the pseudoOrder property of the corresponding constraint
	 * @param props a list of properties
	 * @return
	 * @throws ConfigurationException
	 */
	private String[] makeSortedProperties(ArrayList<Resource> props) throws ConfigurationException {
		TreeMap<String,String> orderedResults =new TreeMap<String,String>();
		int i=0;
		for(Resource res:props) {
			//System.out.println("Now: "+res.getURI());
			NodeIterator propIterator=inputConfig.listObjectsOfProperty(res, ResourceFactory.createProperty(ConstraintsManagerConfig.onDataProperty));
			if(!propIterator.hasNext()) {
				propIterator=inputConfig.listObjectsOfProperty(res, ResourceFactory.createProperty(ConstraintsManagerConfig.onObjectProperty));
				if(!propIterator.hasNext()) throw new ConfigurationException("Cannot find property for "+res.getURI());
			}
			//else throw new ConfigurationException("Cannot find property for "+res.getURI());
			RDFNode putativeProperty=propIterator.next();
			if(!putativeProperty.isURIResource()) throw new ConfigurationException("Invalid property for "+res.getURI());
			String propertyURI=putativeProperty.asResource().getURI();
			String order="ZZZ";
			NodeIterator orderNodes=inputConfig.listObjectsOfProperty(res, ResourceFactory.createProperty(ConstraintsManagerConfig.pseudoOrder));
			if(orderNodes.hasNext()) {
				RDFNode putativeOrder=orderNodes.next();
				if(!putativeOrder.isLiteral()) throw new ConfigurationException("Invalid pseudo order for "+res.getURI());
				order=putativeOrder.asLiteral().getValue().toString();
			}
			else {
				order=order+i;
				i++;
				
			}
			while(orderedResults.containsKey(order)) {order=order+"_";}
			orderedResults.put(order,res.getURI()); //Note: we return resources, not properties!
		}
		return orderedResults.values().toArray(new String[0]);
	}


	private void getConstraintsForEntityRecursively(Resource entity,ArrayList<Resource>currentResult, Set<Resource> defaultConstraints, Map<String,Set<Resource>> specificConstraints,String breakCommand) throws UnknownURIException, ModelException {
		Set<TerminologySet>roots=myInitializer.myFactory.getRootsForURI(entity.getURI());
		Loggers.validationLogger.debug("Resolving for "+entity.getURI());
		if(specificConstraints.containsKey(entity.getURI())) {
			//System.out.println("Got specifics ");
			for(Resource r:specificConstraints.get(entity.getURI())) currentResult.add(r);
			if(!isBlocking(entity,ResourceFactory.createResource(breakCommand)))
				 {
						
						if(roots.size()==0) {
							for(Resource r:defaultConstraints) currentResult.add(r);
						}
						else {
							
							//We should only have one container, so we pick one at random...
							getConstraintsForEntityRecursively(roots.iterator().next().getResource(),currentResult,defaultConstraints,specificConstraints,breakCommand);
						}
			}
			else {
				//System.out.println("break!");
			}
		}
		else {
		
	
			if(roots.size()==0) {
				for(Resource r:defaultConstraints) currentResult.add(r);
			}
			else {
				getConstraintsForEntityRecursively(roots.iterator().next().getResource(),currentResult,defaultConstraints,specificConstraints,breakCommand);
			}
		}
		
		
	}
	
	

	private boolean isBlocking(Resource entity, Resource clearCommandType) {
		ResIterator possibleSubjects=inputConfig.listSubjectsWithProperty(ResourceFactory.createProperty(ConstraintsManagerConfig.appliesTo),entity);
		while(possibleSubjects.hasNext()) {
			Resource possibleSubject=possibleSubjects.next();
			if(
				(inputConfig.contains(possibleSubject,
					ResourceFactory.createProperty(ConstraintsManagerConfig.hasValidationCommand),
					ResourceFactory.createResource(ConstraintsManagerConfig.clearCommand))
				) 
				&&
				(inputConfig.contains(possibleSubject,
					MetaLanguage.typeProperty,
					clearCommandType))
				
				) return true;
		}	
		return false;	
					
		
	}

	/**
	 * returns true if the constraint applies to a data property
	 * @param cons
	 * @return
	 */
	public boolean isOnDataProperty(String cons) {
		if(inputConfig.contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.onDataProperty)
				)) return true;
		else return false;
	}
	
	/**
	 * Returns true if the constraint applies to an object property
	 * @param cons
	 * @return
	 */
	public boolean isOnObjectProperty(String cons) {
		if(inputConfig.contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.onObjectProperty)
				)) return true;
		else return false;
	}
	
	/**
	 * Returns true if the constraint is of type isNumeric
	 * @param cons
	 * @return
	 */
	public boolean isNumeric(String cons) {
		if(inputConfig.contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.hasType),
				ResourceFactory.createResource(ConstraintsManagerConfig.numericType)
				)) return true;
		else return false;
	}
	
	/**
	 * Returns the language for a language constraint. Null if this value canot be found
	 * @param cons
	 * @return
	 */
	public String getForConstraintLanguage(String cons) {
		String result=null;
		NodeIterator possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.language));
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isLiteral()) return tentativeResult.asLiteral().getValue().toString();
		}
		return result;
	}
	
	/**
	 * Returns the property which is the focus of the given constraint
	 * @param cons the constraint URI
	 * @return
	 * @throws PropertyConstraintException
	 * @throws ConfigurationException
	 */
	public String getPropertyForConstraint(String cons) throws PropertyConstraintException, ConfigurationException {
		String result=null;
		NodeIterator possibleResultsIter=null;
		if(isDataConstraint(cons))
			possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.onDataProperty));
		else if(isObjectConstraint(cons)) 
			possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.onObjectProperty));
		else throw new PropertyConstraintException("Inconsistent type for "+cons);
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isResource()) return tentativeResult.asResource().getURI();
		}
		throw new ConfigurationException("Unable to find property for constraint: "+cons);
	}
	
	/**
	 * Returns the min cardinality value for the given constraint. Returns -1 if this value cannot be found.
	 * @param cons
	 * @return
	 */
	public int getMinCardinalityForConstr(String cons) {
		int result=-1;
		NodeIterator possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.minCardinality));
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isLiteral()) result= tentativeResult.asLiteral().getInt();
		}
				
		return result;
	}

	/**
	 * Returns the max cardinality value for the given constraint. Returns -1 if this value cannot be found.
	 * @param cons
	 * @return
	 */
	public int getMaxCardinalityForConstr(String cons) {
		int result=-1;
				NodeIterator possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.maxCardinality));
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isLiteral()) result= tentativeResult.asLiteral().getInt();
		}
				
		return result;
	}

	/**
	 * Returns true if the constraint applies to a data property
	 * @param cons
	 * @return
	 */
	private boolean isDataConstraint(String cons) {
		if(inputConfig.
				contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.onDataProperty)
				))
		return true;
		else return false;
	}

	/**
	 * Returns true if the constraint applies to an object property
	 * @param cons
	 * @return
	 */
	private boolean isObjectConstraint(String cons) {
		if(inputConfig.
				contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.onObjectProperty)
				))
		return true;
		else return false;
	}

	/**
	 * Returns true if the constraint is of type isInRegister
	 * @param cons
	 * @return
	 */
	public boolean isInRegisterForConstr(String cons) {
		if(inputConfig.
				contains(
				ResourceFactory.createResource(cons),
				ResourceFactory.createProperty(ConstraintsManagerConfig.inRegister)
				))
		return true;
		else return false;
	}
	
	/* Unimplemented
	public boolean isStringRegProperty(String property) {
		return false;
	}
	*/
	/* Unimplemented
	public boolean isPlainRegProperty(String property) {
		return false;
	}
	*/
	/* Unimplemented
	public String[] getRegPropertyRange(String property) {
		return null;
	}
	*/
	/* Unimplemented
	public String getSymmetricRegProperty(String property) {
		return null;
	}
	*/
	/**
	 * Returns an array of option for the oneOf constraint. Null if such constraint values cannot be found.
	 * @param cons
	 * @return
	 */
	public String[] getOptionsForConstraints(String cons) {
		ArrayList<String>values=new ArrayList<String>();
		NodeIterator possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.oneOf));
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isResource()) values.add(tentativeResult.asResource().getURI());
		}
		if(values.size()>0) return values.toArray(new String[0]);
		else return null;
	}

	/**
	 * Only ask if a register constraint is detected, or it will rise an exception if no register constraint is present
	 * @param cons
	 * @return
	 * @throws PropertyConstraintException
	 * @throws ConfigurationException
	 */
	public String getRegisterTargetForConstr(String cons) throws PropertyConstraintException, ConfigurationException {
		// TODO Auto-generated method stub
		NodeIterator possibleResultsIter=null;
		if(isObjectConstraint(cons)) 
			possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.inRegister));
		else throw new PropertyConstraintException("Register constraint not defined for an object property in: "+cons);
		while(possibleResultsIter.hasNext()) {
			RDFNode tentativeResult=possibleResultsIter.nextNode();
			if(tentativeResult.isResource()) return tentativeResult.asResource().getURI();
		}
		throw new ConfigurationException("Unable to find targer for inRegister restriction for constraint: "+cons);
		
		////
		//return null;
	}

	/**
	 * We return null if no valid pattern constraint is specified
	 * @param cons
	 * @return
	 * @throws ConfigurationException 
	 */
	public String getPatternForConstr(String cons) throws ConfigurationException {
		String result=null;
		NodeIterator possibleResultsIter=null;
		if(isDataConstraint(cons)) {
			possibleResultsIter=inputConfig.listObjectsOfProperty(ResourceFactory.createResource(cons), ResourceFactory.createProperty(ConstraintsManagerConfig.pattern));
			while(possibleResultsIter.hasNext()) {
				RDFNode tentativeResult=possibleResultsIter.nextNode();
				if(tentativeResult.isLiteral()) {
					result=tentativeResult.asLiteral().getValue().toString();
					try {
						Pattern.compile(result);
					} catch (PatternSyntaxException e) {
						throw new ConfigurationException("Invalid pattern syntax in constraint : "+cons);
					}
					return result;
				}
				
			}
		}
			
		return result;
	}




	
}
