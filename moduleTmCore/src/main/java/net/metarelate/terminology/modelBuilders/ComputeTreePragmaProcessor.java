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

package net.metarelate.terminology.modelBuilders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

class ComputeTreePragmaProcessor extends PragmaProcessor {
	TerminologySet rootSet;
	Resource schemeResource;
	Model globalConfigurationModel;


	public ComputeTreePragmaProcessor(TerminologyFactory myFactory, Model confModel,
			TerminologySet rootSet, Resource schemeResource) {
		super(myFactory);
		this.rootSet=rootSet;
		this.schemeResource=schemeResource;
		this.globalConfigurationModel=confModel;
	}

	@Override
	public void run() throws UnknownURIException, ModelException {
		if(rootSet==null) return;
		Loggers.pragmaLogger.info("Starting Pragma Computation: Tree detection");
		Loggers.pragmaLogger.debug("Root collection : "+rootSet.getURI());
		if(schemeResource!=null) Loggers.pragmaLogger.debug("Scheme resource : "+schemeResource.getURI());
		Loggers.pragmaLogger.trace("Extracting tree elements : "+rootSet.getURI());
		Set<TerminologyIndividual> nodesSet=rootSet.getAllKnownContainedInviduals(); // TODO note: we structure in a tree all contained individuals
		Iterator<TerminologyIndividual> nodeIter=nodesSet.iterator();
		Set<ExpandedURI> uriNodes=new HashSet<ExpandedURI>();
		while(nodeIter.hasNext()) {
			TerminologyIndividual tempNode=nodeIter.next();
			String nodeURI=tempNode.getURI();
			int lastIndexOfTo=nodeURI.lastIndexOf("to");
			if(lastIndexOfTo>0) {
				Loggers.pragmaLogger.trace("Found possible action for : "+nodeURI);
				String left=nodeURI.substring(0,lastIndexOfTo);
				String right=nodeURI.substring(lastIndexOfTo+2);
				int lastIndexOfSlah=left.lastIndexOf('/');
				int lastIndexOfPound=left.lastIndexOf('#');
				int lastIndexOfDot=left.lastIndexOf('.');
				//System.out.println("Last / : "+lastIndexOfSlah);
				//System.out.println("Last # : "+lastIndexOfPound);
				int beginLeft=-1;
				String lastSeparator="";
				if(lastIndexOfPound>beginLeft) {
					beginLeft=lastIndexOfPound;
					lastSeparator="#";
				}
				if(lastIndexOfTo>beginLeft) {
					beginLeft=lastIndexOfSlah;
					lastSeparator="/";
				}
				if(lastIndexOfDot>beginLeft) {
					beginLeft=lastIndexOfDot;
					lastSeparator=".";
				}
				//System.out.println("begin left : "+beginLeft);
				if(beginLeft<0) {
					Loggers.pragmaLogger.warn("Cannot find left index for dash expansion in "+left);
					return;
				}
				left=left.substring(beginLeft+1);	
				int minIndex;
				int maxIndex;
				try {
					minIndex=Integer.parseInt(left);
					maxIndex=Integer.parseInt(right);
				}
				catch(Exception e) {
					Loggers.pragmaLogger.warn("!!!! Error in parsing "+left+" or "+right);
					continue;
					//return;
				}
				uriNodes.add(new ExpandedURI(nodeURI,nodeURI.substring(0,beginLeft)+lastSeparator,minIndex,maxIndex));
				Loggers.pragmaLogger.debug("Found : "+nodeURI+"("+minIndex+","+maxIndex+")");
					
			}//if
				
		}//while
		Loggers.pragmaLogger.trace("Building Tree");
		Loggers.pragmaLogger.trace("1) Expanding containment relationships for nodes: "+myRenderer(uriNodes)+" size :"+uriNodes.size());
		Set<ExpandedURI> topURIs=getTopLayerAndReduce(uriNodes);
		Set<ExpandedURI> treeRoots=new HashSet<ExpandedURI>(topURIs);
		Loggers.pragmaLogger.trace("tree roots "+myRenderer(treeRoots)+" size :"+topURIs.size());
		//percolateSet(treeRoots,uriNodes);
		
		int iterCounter=1;
		while(uriNodes.size()>0) {
			Loggers.pragmaLogger.trace("Iteration "+iterCounter);
			iterCounter++;
			topURIs=getTopLayerAndReduce(uriNodes);
			Loggers.pragmaLogger.trace("Top set "+myRenderer(topURIs)+" size: "+topURIs.size());
			Loggers.pragmaLogger.trace("Rest "+myRenderer(uriNodes)+" size: "+uriNodes.size());
			percolateSet(treeRoots,topURIs);
			
			//TODO here percolate assignment
		}
			
			
		Loggers.pragmaLogger.trace("Registering scheme terminology entities");
		Iterator<ExpandedURI> expIter=treeRoots.iterator();
		while(expIter.hasNext()) {
			ExpandedURI tempRoot=expIter.next();
			int result=paintTreeSchemeTerms(tempRoot);
			Loggers.pragmaLogger.trace("For +"+tempRoot.uri+" -> "+result);
		}
			
		// 1) get Top concept
		Loggers.pragmaLogger.trace("Looking for top concept");
		Resource topConcept=SimpleQueriesProcessor.getOptionalResourceObject(schemeResource, MetaLanguage.skosTopConceptProperty, globalConfigurationModel);
		if(topConcept==null) {
			Loggers.pragmaLogger.trace("Unable to find top concept, end of pragma");
			return;
		}
		Loggers.pragmaLogger.trace("Found top concept: "+topConcept.getURI());
		TerminologyEntity topConceptEntity=myFactory.getCheckedTerminologyIndividual(topConcept.getURI());
		if(topConceptEntity==null) {
			Loggers.pragmaLogger.trace("Unknown top concept, end of pragma");
			return;
		}
			
		// 2) get list of entities "narrower" top concept
		Loggers.pragmaLogger.trace("Looking at all narrower concepts");
		NodeIterator narrIter=globalConfigurationModel.listObjectsOfProperty(topConceptEntity.getResource(), MetaLanguage.skosNarrowerProperty);
		Set<TerminologyIndividual> leafsSet=new HashSet<TerminologyIndividual>();
		int totalCounter=0;
		while(narrIter.hasNext()) {
			Resource narr=narrIter.nextNode().asResource();
			//System.out.println(">> "+narr.getURI());
			if(myFactory.terminologyIndividualExist(narr.getURI())) {
				leafsSet.add(myFactory.getUncheckedTerminologyIndividual(narr.getURI()));
			}
			totalCounter++;
			
		}
		Loggers.pragmaLogger.trace("Found "+totalCounter+" solutions, out which I know "+leafsSet.size()+" unique individuals");
		
		// 4) remove all narrower from Top concept
		Loggers.pragmaLogger.trace("Removing all narrower from top concept");
		//Model tempModel=ModelFactory.createDefaultModel();
		Model topConceptModel=topConceptEntity.getStatements(topConceptEntity.getLastVersion());
		Loggers.pragmaLogger.trace("Top concept had : "+topConceptModel.size()+" statements");
		StmtIterator resIter=topConceptModel.listStatements(topConceptEntity.getResource(),MetaLanguage.skosNarrowerProperty,(Resource)null);
		topConceptModel.remove(resIter);
		Loggers.pragmaLogger.trace("Now it has : "+topConceptModel.size()+" statements");
			
			
		// 5) for all entities, remove broader from statements
		Loggers.pragmaLogger.trace("Going to remove broader from entities");
		Iterator<TerminologyIndividual> termsIterator=leafsSet.iterator();
		int affectCount=0;
		while(termsIterator.hasNext()) {
			TerminologyIndividual term=termsIterator.next();
			Model termModel=term.getStatements(term.getLastVersion());
			long pre=termModel.size();
			StmtIterator toRemove=termModel.listStatements(term.getResource(),MetaLanguage.skosBroaderProperty,(Resource)null);
			termModel.remove(toRemove);
			if(termModel.size()<pre) affectCount++;
		}
		Loggers.pragmaLogger.trace("Broader concept removeed from : "+affectCount+" entities");
		
		
		// 3) remove tree elements from list of entities
		Loggers.pragmaLogger.trace("Removing tree elements from the list");
		Iterator<ExpandedURI> treeRootsIter=treeRoots.iterator();
		while(treeRootsIter.hasNext()) {
			//ExpandedURI tempURI=treeRootsIter.next();
			removeFromTree(treeRootsIter.next(),leafsSet);
		}
		Loggers.pragmaLogger.trace("Of which I can consider leafs: "+leafsSet.size());
			
			
			
			
		// 6) Assert root broader TopConcept/vice versa
		treeRootsIter=treeRoots.iterator();
		// 7) Propagate narrower/broader on tree
		while(treeRootsIter.hasNext()) {
			ExpandedURI uriItem=treeRootsIter.next();
			assertPair(uriItem.myIndividual,topConceptEntity);
			propAssert(uriItem);
		}
		// 8) for each element in the list of tree, find number, percolate (assert broader/narrower)
		// TODO DEBUG testing the rest for now
		
		Iterator<TerminologyIndividual> leafsIter=leafsSet.iterator();
		while(leafsIter.hasNext()) {
			TerminologyIndividual leaf=leafsIter.next();
			Loggers.pragmaLogger.trace("Trying "+leaf.getURI()+" value: "+getNumber(leaf.getURI()));
			treeRootsIter=treeRoots.iterator();
			while(treeRootsIter.hasNext()) {
				percolateAndConnectLeaf(treeRootsIter.next(),leaf);
			}
				
				
		}
			
			
			
		
			
			
			
	}//pragma

	
	
	
	 private  Set<ExpandedURI> getTopLayerAndReduce(Set<ExpandedURI> uriNodes) {
		HashSet<ExpandedURI> topSet=new HashSet<ExpandedURI>();
		ExpandedURI[] allForMatrix=uriNodes.toArray(new ExpandedURI[0]);
		for(int i=0;i<allForMatrix.length ;i++) {
			boolean isMax=true;
			for(int j=0;j<allForMatrix.length ;j++) {
				//System.out.println("testing: ("+allForMatrix[i].min+","+allForMatrix[i].max+") in ("+allForMatrix[j]+","+allForMatrix[j].max+")");
				if(allForMatrix[i].min>=allForMatrix[j].min && allForMatrix[i].max<=allForMatrix[j].max && i!=j) isMax=false;
			}
			if(isMax) topSet.add(allForMatrix[i]);
		}
		//System.out.println("top slice: "+topSet.size());
		uriNodes.removeAll(topSet);
		
		return topSet;
	}
	 
	 
	 private void percolateSet(Set<ExpandedURI> top, Set<ExpandedURI> tokens) {
			ExpandedURI[] topArray=top.toArray(new ExpandedURI[0]);
			ExpandedURI[] tokensArray=tokens.toArray(new ExpandedURI[0]);
			for(int tk=0;tk<tokensArray.length;tk++) {
				for(int to=0;to<topArray.length;to++) {
					//Does the token fit the hole ?
					//SSLogger.log("trying "+tokensArray[tk].getBracketName()+" in "+topArray[to].getBracketName());
					if(tokensArray[tk].min>=topArray[to].min && tokensArray[tk].max<=topArray[to].max) {
						//can it fit a child?
						//SSLogger.log("fit");
						Set<ExpandedURI> childrenOfTo=topArray[to].childrenSet;
						boolean wentTochildren=false;
						//We need a copy, not a reference here:
						
						Iterator<ExpandedURI> fixedChildrenOfToIter=childrenOfTo.iterator();
						while(fixedChildrenOfToIter.hasNext()) {
							ExpandedURI child=fixedChildrenOfToIter.next();
							if(tokensArray[tk].min>=child.min && tokensArray[tk].max<=child.max ) {
								Set<ExpandedURI> newTop=new HashSet<ExpandedURI>();	// TODO To be put in order! no back and forth data structure and recursivity is  on single element, not array
								Set<ExpandedURI> newTokens=new HashSet<ExpandedURI>();
								newTop.add(child);
								newTokens.add(tokensArray[tk]);
								percolateSet(newTop,newTokens);
								wentTochildren=true;
								//SSLogger.log("went to "+child.uri);
							}
						
						}
						if(!wentTochildren) {
							topArray[to].childrenSet.add(tokensArray[tk]);
							Loggers.pragmaLogger.trace("Registered "+tokensArray[tk].getBracketName()+" under "+topArray[to].getBracketName());
						}
						
						
					}
				}
			}
			
			
			Iterator<ExpandedURI> expandedURIIter=tokens.iterator();
			while(expandedURIIter.hasNext()) {
				ExpandedURI token=expandedURIIter.next();
				
			}
			
		}
	 
	 class ExpandedURI {
		String uri;
		public int min;
		public int max;
		Set<ExpandedURI> childrenSet;
		TerminologySet mySet=null;
		TerminologyEntity myIndividual=null;
		Set<String> contained=null;
		public ExpandedURI(String uri,String rootURI,int min,int max) {
			this.uri=uri;
			this.min=min;
			this.max=max;
			childrenSet=new HashSet<ExpandedURI>();
			contained=new HashSet(); // Not really used
		}
		public String getBracketName() {
			return "("+min+","+max+")";
		}
		
	}
	 
	 
	private String myRenderer(Set<ExpandedURI> toRender) {
		String result="";
		Iterator<ExpandedURI> expIter=toRender.iterator();
		while(expIter.hasNext()) {
			result=result+" "+expIter.next().getBracketName();
		}
		
		return result;	
	}
		
	private int paintTreeSchemeTerms(ExpandedURI tempRoot) {
		// TODO to fix!
		int result=0;
		if(myFactory.terminologyIndividualExist(tempRoot.uri)) {
			tempRoot.myIndividual=myFactory.getUncheckedTerminologyIndividual(tempRoot.uri);
			Loggers.pragmaLogger.trace("Found ind for :"+tempRoot.uri);
			result+=1;
		}
			
		if(tempRoot.childrenSet.size()==0) {
			return result;
		}
		else{
			Iterator<ExpandedURI> chlidrenIter=tempRoot.childrenSet.iterator();
			while(chlidrenIter.hasNext()) {
				ExpandedURI tempChild= chlidrenIter.next();
				result+=paintTreeSchemeTerms(tempChild);
			}
			return result;
		}
		
	}
		
	private void removeFromTree(ExpandedURI treeNode,Set<TerminologyIndividual> toReduceSet) {
		if(treeNode.childrenSet.size()>0) {
			Iterator<ExpandedURI> childrenIter=treeNode.childrenSet.iterator();
			while(childrenIter.hasNext()) {
				removeFromTree(childrenIter.next(),toReduceSet);
			}
		}
		if(treeNode.myIndividual!=null) toReduceSet.remove(treeNode.myIndividual);
	}
		
	private void assertPair(TerminologyEntity child, TerminologyEntity parent) {
		child.getStatements(child.getLastVersion()).add(ResourceFactory.createStatement(child.getResource(), MetaLanguage.skosBroaderProperty, parent.getResource()));
		parent.getStatements(parent.getLastVersion()).add(ResourceFactory.createStatement(parent.getResource(),MetaLanguage.skosNarrowerProperty,child.getResource()));
	}
		
	private void propAssert(ExpandedURI uriItem) {
		if(uriItem.childrenSet.size()>0) {
			Iterator<ExpandedURI> childrenIter=uriItem.childrenSet.iterator();
			while(childrenIter.hasNext()) {
				ExpandedURI child=childrenIter.next();
				assertPair(child.myIndividual,uriItem.myIndividual);
				propAssert(child);
			}
			
		}
	} 
	 
	 
	private boolean percolateAndConnectLeaf(ExpandedURI node,TerminologyIndividual leaf) {
		int leafN=getNumber(leaf.getURI());
		Loggers.pragmaLogger.trace("trying "+node.getBracketName());
		if(leafN<node.min || leafN>node.max) {
			Loggers.pragmaLogger.trace("No fit");
			return false;
		}
		else {
			if(node.childrenSet.size()==0) {
				//it's me!
				assertPair(leaf,node.myIndividual);
				Loggers.pragmaLogger.trace("Attached (c) to "+node.getBracketName());
				return true;
			}
			else {
				Iterator<ExpandedURI> childrenIter=node.childrenSet.iterator();
				boolean found=false;
				while(childrenIter.hasNext()) {
					boolean result= percolateAndConnectLeaf(childrenIter.next(),leaf);
					found=result || found;
				}
					
				if(!found) {
					assertPair(leaf,node.myIndividual);
					Loggers.pragmaLogger.trace("Attached (i) to "+node.getBracketName());
					return true;
				} 
				else return true;
				
				//return true;
			}
		}
			
	}
		 

}
