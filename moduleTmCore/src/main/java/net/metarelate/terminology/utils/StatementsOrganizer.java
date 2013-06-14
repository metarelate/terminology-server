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

package net.metarelate.terminology.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.instanceManager.Initializer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class StatementsOrganizer {
	//Initializer myInitializer=null;
	//public StatementsOrganizer(Initializer initializer) {
	//	myInitializer=initializer;
	//}
	
	/**
	 * @see MetaLanguage#filterForWeb(Model)
	 * @param statements
	 * @return
	 */
	public static Model filterModelForWeb(Model statements) {
		return MetaLanguage.filterForWeb(statements);
	}
	
	/**
	 * @see MetaLanguage#filterForData(Model)
	 * @param statements
	 * @return
	 */
	public static Model filterModelForData(Model statements) {
		return MetaLanguage.filterForData(statements);
	}
	
	/**
	 * Returns the statements in the model sorted as follows:
	 * First all properties with focus on the concept natture of a code
	 * Then all properties with focus on the "code" nature of the code
	 * The all other properties
	 * For each group, multiple assertions of the same property are sorted by the object (if literal),
	 * following some heuristics for codes (@see CodeComparator)
	 * @param statementsModel
	 * @param myInitializer
	 * @return
	 */
	public static ArrayList<Statement> orderStatements(Model statementsModel,Initializer myInitializer) {
		Set<Property> codeSet=new HashSet<Property>();
		Set<Property> conceptSet=new HashSet<Property>();
		Set<Property> otherSet=new HashSet<Property>();
		
		StmtIterator statements=statementsModel.listStatements();
		while (statements.hasNext()) {
			Statement stat=statements.nextStatement();
			if(myInitializer.myFactory.getBackgroundKnowledgeManager().getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyCodeFocus)) {
				codeSet.add(stat.getPredicate());
			}
			else if(myInitializer.myFactory.getBackgroundKnowledgeManager().getModel().contains(stat.getPredicate().asResource(),MetaLanguage.propertyHasFocus,MetaLanguage.propertyConceptFocus)) {
				conceptSet.add(stat.getPredicate());
			}
			else otherSet.add(stat.getPredicate());

		}
		ArrayList<Statement> orderedStatementsList=new ArrayList<Statement>();
		orderedStatementsList=subOrder(codeSet,statementsModel,orderedStatementsList);
		orderedStatementsList=subOrder(conceptSet,statementsModel,orderedStatementsList);
		orderedStatementsList=subOrder(otherSet,statementsModel,orderedStatementsList);
		return orderedStatementsList;
	}

	private static ArrayList<Statement> subOrder(Set<Property> propertySet, Model statements,
			ArrayList<Statement> orderedStatementsList) {
			TreeMap<String,TreeMap<String,Statement>> orderedStatements=new TreeMap<String,TreeMap<String,Statement>>();
			Iterator<Property> propIter=propertySet.iterator();
			while(propIter.hasNext()) {
				Property property=propIter.next();
				TreeMap<String,Statement> subOrder=new TreeMap<String,Statement>(new CodeComparator());
				StmtIterator subStats=statements.listStatements(null,property,(RDFNode)null);
				while(subStats.hasNext()) {
					Statement subStat=subStats.nextStatement();
					if(subStat.getObject().isResource()) subOrder.put(subStat.getObject().asResource().getURI(), subStat);
					else if(subStat.getObject().isLiteral()) subOrder.put(subStat.getObject().asLiteral().getValue().toString(), subStat); // TODO to check for URI/Data
					else subOrder.put(subStat.getObject().toString(), subStat);
				}
				orderedStatements.put(property.getURI(),subOrder);
				
			}
			Iterator<TreeMap<String,Statement>> firstOderIter=orderedStatements.values().iterator();
			while(firstOderIter.hasNext()) {
				TreeMap<String,Statement> subOrder=firstOderIter.next();
				Iterator<Statement> orderedStats=subOrder.values().iterator();
				while(orderedStats.hasNext()) {
					orderedStatementsList.add(orderedStats.next());
				}
				
			}
			return orderedStatementsList;
			
	}
	/**
	 * Returns the statements in the model sorted by the skos:notation of the object, when this is a resource.
	 * This is assuming that the skos:notation is unique and makes use of the heuristics in @see CodeComparator
	 * @param statSet
	 * @param tf
	 * @return
	 */
	public static  Collection<Statement> orderStatementsByLiteralObject(Set<Statement> statSet, TerminologyFactory tf) {
		int counter=0;
		Comparator<String> codeComparator= new CodeComparator();
		TreeMap<String,Statement> orderedCodeStatements=new TreeMap<String,Statement>(codeComparator);
		for(Statement stat:statSet) {
			RDFNode object=stat.getObject();
			String seed=null;
			if(object.isURIResource()) {
				TerminologyEntity objectEntity=null;
				if(tf.terminologyIndividualExist(object.asResource().getURI())) {
					objectEntity=tf.getUncheckedTerminologyIndividual(object.asResource().getURI());
					seed=objectEntity.getNotation(objectEntity.getLastVersion());
				}
					
			}
			if(seed!=null) {
				if(orderedCodeStatements.containsKey(seed)) {
					orderedCodeStatements.put(seed+counter, stat);
					counter=counter+1;
				}
				else {
					orderedCodeStatements.put(seed, stat);
				}
					
				
			}
			else orderedCodeStatements.put(stat.getPredicate().getURI()+counter,stat );
			counter+=1;
		}
		return orderedCodeStatements.values();
		
		
	}
	//TODO the logic of the above is very weak! to revise.

}
