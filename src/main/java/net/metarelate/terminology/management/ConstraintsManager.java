package net.metarelate.terminology.management;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ConstraintsManager {
	Initializer myInitializer=null;
	Model inputConfig=null;
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
		SSLogger.log("Found generic constraints for registers: ",SSLogger.DEBUG);
		for(Resource c:defaultRegConstraints) SSLogger.log(c.getURI(),SSLogger.DEBUG);
		SSLogger.log("Found generic constraints for codes: ",SSLogger.DEBUG);
		for(Resource c:defaultCodeConstraints) SSLogger.log(c.getURI(),SSLogger.DEBUG);
		SSLogger.log("Found specific constraints for registers: ",SSLogger.DEBUG);
		for(String c:uriRegConstraints.keySet()) {
			SSLogger.log("for: "+c,SSLogger.DEBUG);
			for(Resource c2:uriRegConstraints.get(c)) SSLogger.log("\t"+c2,SSLogger.DEBUG);
		}
		SSLogger.log("Found specific constraints for codes: ",SSLogger.DEBUG);
		for(String c:uriCodeConstraints.keySet()) {
			SSLogger.log("for: "+c,SSLogger.DEBUG);
			for(Resource c2:uriCodeConstraints.get(c)) SSLogger.log("\t"+c2,SSLogger.DEBUG);
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
	
	
	public String[] getSortedConstraintsForReg(String uri) throws ConfigurationException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getExistingTerminologyEntity(uri);
		Set<TerminologySet> containers=entity.getContainers(entity.getLastVersion());
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultRegConstraints, 
				uriRegConstraints,
				ConstraintsManagerConfig.regValidationCommandType);
		return makeSortedProperties(results);
		
	}
	public String[] getSortedConstraintsForCode(String uri) throws ConfigurationException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getExistingTerminologyEntity(uri);
		Set<TerminologySet> containers=entity.getContainers(entity.getLastVersion());
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultCodeConstraints, 
				uriCodeConstraints,
				ConstraintsManagerConfig.codeValidationCommandType);
		return makeSortedProperties(results);
	}
	public String[] getSortedConstraintsForNewReg(String baseRegURI) throws ConfigurationException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getExistingTerminologyEntity(baseRegURI);
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultRegConstraints, 
				uriRegConstraints,
				ConstraintsManagerConfig.regValidationCommandType);
		return makeSortedProperties(results);
	}
	public String[] getSortedConstraintsForNewCode(String baseRegURI) throws ConfigurationException {
		ArrayList<Resource> results=new ArrayList<Resource>();
		TerminologyEntity entity=myInitializer.myFactory.getExistingTerminologyEntity(baseRegURI);
		getConstraintsForEntityRecursively(
				entity.getResource(),
				results, 
				defaultCodeConstraints, 
				uriCodeConstraints,
				ConstraintsManagerConfig.codeValidationCommandType);
		return makeSortedProperties(results);
	}
	
	
	private String[] makeSortedProperties(ArrayList<Resource> results) throws ConfigurationException {
		TreeMap<String,String> orderedResults =new TreeMap<String,String>();
		int i=0;
		for(Resource res:results) {
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
				
			}
			while(!orderedResults.containsKey(order)) {order=order+"_";}
			orderedResults.put(order,res.getURI()); //Note: we return resources, not properties!
		}
		return orderedResults.values().toArray(new String[0]);
	}


	private void getConstraintsForEntityRecursively(Resource entity,ArrayList<Resource>currentResult, Set<Resource> defaultConstraints, Map<String,Set<Resource>> specificConstraints,String breakCommand) {
		Set<TerminologySet>roots=myInitializer.myFactory.getRootsForURI(entity.getURI());
		System.out.println("Resolving for "+entity.getURI());
		if(specificConstraints.containsKey(entity.getURI())) {
			System.out.println("Got specifics ");
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
				System.out.println("break!");
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


	public int getMinCardinalityForRegProperty(String property) {
		return 0;
	}
	public int getMaxCardinalityForRegProperty(String property) {
		return 0;
	}
	public boolean isStringRegProperty(String property) {
		return false;
	}
	public boolean isNumericRegProperty(String property) {
		return false;
	}
	public boolean isPlainRegProperty(String property) {
		return false;
	}
	public boolean isDataRegProperty(String property) {
		return false;
	}
	public boolean isObjectRegProperty(String property) {
		return false;
	}
	public String[] getRegPropertyRange(String property) {
		return null;
	}
	public String getLanguageRegProperty(String property) {
		return null;
	}
	public String getSymmetricRegProperty(String property) {
		return null;
	}
	
}
