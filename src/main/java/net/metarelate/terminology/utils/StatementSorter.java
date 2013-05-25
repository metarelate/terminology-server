package net.metarelate.terminology.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
// TODO this only sort on object and notation it should be improved!
public class StatementSorter {

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
}

