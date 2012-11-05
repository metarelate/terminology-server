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

package uk.gov.metoffice.terminology.modelBuilders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.gov.metoffice.terminology.config.MetaLanguage;
import uk.gov.metoffice.terminology.coreModel.TerminologyEntity;
import uk.gov.metoffice.terminology.coreModel.TerminologyFactory;
import uk.gov.metoffice.terminology.coreModel.TerminologyIndividual;
import uk.gov.metoffice.terminology.coreModel.TerminologySet;
import uk.gov.metoffice.terminology.utils.SSLogger;
import uk.gov.metoffice.terminology.utils.SimpleQueriesProcessor;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ComputeTreePragmaProcessor extends PragmaProcessor {
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
	public void run() {
		if(rootSet==null) return;
		SSLogger.log("*** PRAGMA Computation ***",SSLogger.DEBUG);
		SSLogger.log("Root collection : "+rootSet.getURI(),SSLogger.DEBUG);
		if(schemeResource!=null) SSLogger.log("Scheme resource : "+schemeResource.getURI(),SSLogger.DEBUG);
		SSLogger.log("Extracting tree elements : "+rootSet.getURI(),SSLogger.DEBUG);
		Set<TerminologyIndividual> nodesSet=rootSet.getAllKnownContainedInviduals(); // TODO note: we structure in a tree all contained individuals
		Iterator<TerminologyIndividual> nodeIter=nodesSet.iterator();
		Set<ExpandedURI> uriNodes=new HashSet<ExpandedURI>();
		while(nodeIter.hasNext()) {
			TerminologyIndividual tempNode=nodeIter.next();
			String nodeURI=tempNode.getURI();
			int lastIndexOfTo=nodeURI.lastIndexOf("to");
			if(lastIndexOfTo>0) {
				SSLogger.log("Found possible action for : "+nodeURI,SSLogger.DEBUG);
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
					SSLogger.log("Cannot find left index",SSLogger.DEBUG);
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
					SSLogger.log("!!!! Error in parsing "+left+" or "+right,SSLogger.DEBUG);
					continue;
					//return;
				}
				uriNodes.add(new ExpandedURI(nodeURI,nodeURI.substring(0,beginLeft)+lastSeparator,minIndex,maxIndex));
				SSLogger.log("Found : "+nodeURI+"("+minIndex+","+maxIndex+")",SSLogger.DEBUG);
					
			}//if
				
		}//while
		SSLogger.log("Building Tree",SSLogger.DEBUG);
		SSLogger.log("1) Expanding containement relationships",SSLogger.DEBUG);
		SSLogger.log("Starting point",SSLogger.DEBUG);
		SSLogger.log("Total nodes"+myRenderer(uriNodes)+" size :"+uriNodes.size(),SSLogger.DEBUG);
		Set<ExpandedURI> topURIs=getTopLayerAndReduce(uriNodes);
		Set<ExpandedURI> treeRoots=new HashSet<ExpandedURI>(topURIs);
		SSLogger.log("tree roots "+myRenderer(treeRoots)+" size :"+topURIs.size(),SSLogger.DEBUG);
		//percolateSet(treeRoots,uriNodes);
		
		int iterCounter=1;
		while(uriNodes.size()>0) {
			SSLogger.log("Iteration "+iterCounter+SSLogger.DEBUG);
			iterCounter++;
			topURIs=getTopLayerAndReduce(uriNodes);
			SSLogger.log("Top set "+myRenderer(topURIs)+" size: "+topURIs.size(),SSLogger.DEBUG);
			SSLogger.log("Rest "+myRenderer(uriNodes)+" size: "+uriNodes.size(),SSLogger.DEBUG);
			percolateSet(treeRoots,topURIs);
			
			//TODO here percolate assignment
		}
			
			
		SSLogger.log("Registering scheme terminology entities",SSLogger.DEBUG);
		Iterator<ExpandedURI> expIter=treeRoots.iterator();
		while(expIter.hasNext()) {
			ExpandedURI tempRoot=expIter.next();
			int result=paintTreeSchemeTerms(tempRoot);
			SSLogger.log("For +"+tempRoot.uri+" -> "+result,SSLogger.DEBUG);
		}
			
		// 1) get Top concept
		SSLogger.log("Looking for top concept",SSLogger.DEBUG);
		Resource topConcept=SimpleQueriesProcessor.getOptionalResourceObject(schemeResource, MetaLanguage.skosTopConceptProperty, globalConfigurationModel);
		if(topConcept==null) {
			SSLogger.log("Unable to find top concept, end of pragma",SSLogger.DEBUG);
			return;
		}
		SSLogger.log("Found top concept: "+topConcept.getURI(),SSLogger.DEBUG);
		TerminologyEntity topConceptEntity=myFactory.getOrCreateTerminologyIndividual(topConcept.getURI());
		if(topConceptEntity==null) {
			SSLogger.log("Unknown top concept, end of pragma",SSLogger.DEBUG);
			return;
		}
			
		// 2) get list of entities "narrower" top concept
		SSLogger.log("Looking at all narrower concepts",SSLogger.DEBUG);
		NodeIterator narrIter=globalConfigurationModel.listObjectsOfProperty(topConceptEntity.getResource(), MetaLanguage.skosNarrowerProperty);
		Set<TerminologyIndividual> leafsSet=new HashSet<TerminologyIndividual>();
		int totalCounter=0;
		while(narrIter.hasNext()) {
			Resource narr=narrIter.nextNode().asResource();
			//System.out.println(">> "+narr.getURI());
			if(myFactory.terminologyIndividualExist(narr.getURI())) {
				leafsSet.add(myFactory.getOrCreateTerminologyIndividual(narr.getURI()));
			}
			totalCounter++;
			
		}
		SSLogger.log("Found "+totalCounter+" solutions, out which I know "+leafsSet.size()+" unique individuals",SSLogger.DEBUG);
		
		// 4) remove all narrower from Top concept
		SSLogger.log("Removing all narrower from top concept",SSLogger.DEBUG);
		//Model tempModel=ModelFactory.createDefaultModel();
		Model topConceptModel=topConceptEntity.getStatements(topConceptEntity.getDefaultVersion());
		SSLogger.log("Top concept had : "+topConceptModel.size()+" statements",SSLogger.DEBUG);
		StmtIterator resIter=topConceptModel.listStatements(topConceptEntity.getResource(),MetaLanguage.skosNarrowerProperty,(Resource)null);
		topConceptModel.remove(resIter);
		SSLogger.log("Now it has : "+topConceptModel.size()+" statements",SSLogger.DEBUG);
			
			
		// 5) for all entities, remove broader from statements
		SSLogger.log("Going to remove broader from entities",SSLogger.DEBUG);
		Iterator<TerminologyIndividual> termsIterator=leafsSet.iterator();
		int affectCount=0;
		while(termsIterator.hasNext()) {
			TerminologyIndividual term=termsIterator.next();
			Model termModel=term.getStatements(term.getDefaultVersion());
			long pre=termModel.size();
			StmtIterator toRemove=termModel.listStatements(term.getResource(),MetaLanguage.skosBroaderProperty,(Resource)null);
			termModel.remove(toRemove);
			if(termModel.size()<pre) affectCount++;
		}
		SSLogger.log("Affected : "+affectCount+" entities",SSLogger.DEBUG);
		
		
		// 3) remove tree elements from list of entities
		SSLogger.log("Removing tree elements from the list",SSLogger.DEBUG);
		Iterator<ExpandedURI> treeRootsIter=treeRoots.iterator();
		while(treeRootsIter.hasNext()) {
			//ExpandedURI tempURI=treeRootsIter.next();
			removeFromTree(treeRootsIter.next(),leafsSet);
		}
		SSLogger.log("Of which I can consider leafs: "+leafsSet.size(),SSLogger.DEBUG);
			
			
			
			
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
			SSLogger.log("Trying "+leaf.getURI()+" value: "+getNumber(leaf.getURI()));
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
					//SSLogger.log("trying "+tokensArray[tk].getBracketName()+" in "+topArray[to].getBracketName(),SSLogger.DEBUG);
					if(tokensArray[tk].min>=topArray[to].min && tokensArray[tk].max<=topArray[to].max) {
						//can it fit a child?
						//SSLogger.log("fit",SSLogger.DEBUG);
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
								//SSLogger.log("went to "+child.uri,SSLogger.DEBUG);
							}
						
						}
						if(!wentTochildren) {
							topArray[to].childrenSet.add(tokensArray[tk]);
							SSLogger.log("Registered "+tokensArray[tk].getBracketName()+" under "+topArray[to].getBracketName(),SSLogger.DEBUG);
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
		if(myFactory.getOrCreateTerminologyIndividual(tempRoot.uri)!=null) {
			tempRoot.myIndividual=myFactory.getOrCreateTerminologyIndividual(tempRoot.uri);
			SSLogger.log("Found ind for :"+tempRoot.uri);
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
		child.getStatements(child.getDefaultVersion()).add(ResourceFactory.createStatement(child.getResource(), MetaLanguage.skosBroaderProperty, parent.getResource()));
		parent.getStatements(parent.getDefaultVersion()).add(ResourceFactory.createStatement(parent.getResource(),MetaLanguage.skosNarrowerProperty,child.getResource()));
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
		SSLogger.log("trying "+node.getBracketName(),SSLogger.DEBUG);
		if(leafN<node.min || leafN>node.max) {
			SSLogger.log("No fit",SSLogger.DEBUG);
			return false;
		}
		else {
			if(node.childrenSet.size()==0) {
				//it's me!
				assertPair(leaf,node.myIndividual);
				SSLogger.log("Attached (c) to "+node.getBracketName(),SSLogger.DEBUG);
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
					SSLogger.log("Attached (i) to "+node.getBracketName(),SSLogger.DEBUG);
					return true;
				} 
				else return true;
				
				//return true;
			}
		}
			
	}
		 

}
