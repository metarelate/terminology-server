package net.metarelate.terminology.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.instanceManager.Initializer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class StatementsOrganizer {
	Initializer myInitializer=null;
	public StatementsOrganizer(Initializer initializer) {
		myInitializer=initializer;
	}
	
	public static Model filterModelForWeb(Model statements) {
		return MetaLanguage.filterForWeb(statements);
	}
	
	public static Model filterModelForData(Model statements) {
		return MetaLanguage.filterForData(statements);
	}
	
	
	public ArrayList<Statement> orderStatements(Model statementsModel) {
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

	private ArrayList<Statement> subOrder(Set<Property> propertySet, Model statements,
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

}
