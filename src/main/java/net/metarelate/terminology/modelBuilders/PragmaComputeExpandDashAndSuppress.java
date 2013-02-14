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

package net.metarelate.terminology.modelBuilders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.utils.SSLogger;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class PragmaComputeExpandDashAndSuppress extends PragmaProcessor {
	TerminologySet terminologySet=null;
	boolean toSuppress;
	ArrayList<Property> overrideProps;
	int maxLimit;
	boolean hardLimitCut;
	int pad;
	
	public PragmaComputeExpandDashAndSuppress(
			TerminologyFactory factory,
			TerminologySet set, 
			boolean toSuppress,
			ArrayList<Property> overrideProps, 
			int maxLimit,
			boolean hardLimitCut, 
			int pad) {
		super(factory);
		terminologySet=set;
		this.toSuppress=toSuppress;
		this.overrideProps=overrideProps;
		this.maxLimit=maxLimit;
		this.hardLimitCut=hardLimitCut;
		this.pad=pad;
	}

	@Override
	public void run() {
		SSLogger.log("*** PRAGMA Computation ***",SSLogger.DEBUG);
		SSLogger.log("On collection : "+terminologySet.getURI(),SSLogger.DEBUG);
		SSLogger.log("To suppress : "+toSuppress,SSLogger.DEBUG);
		Iterator<Property> propIter=overrideProps.iterator();
		while(propIter.hasNext()) {
			Property prop=propIter.next();
			SSLogger.log("Override : "+prop,SSLogger.DEBUG);
		}
			
		SSLogger.log("Action : expanding individuals with x-y range",SSLogger.DEBUG);
		if(maxLimit>0) {
			SSLogger.log("Max expansion: "+maxLimit,SSLogger.DEBUG);
			if(hardLimitCut) SSLogger.log("Dropping all if exceeding "+maxLimit,SSLogger.DEBUG);
			else SSLogger.log("Expanding only max number if exceeding "+maxLimit,SSLogger.DEBUG);
		}
			
		Set<TerminologyIndividual> terms=terminologySet.getIndividuals();
		SSLogger.log("Version (default) :"+terminologySet.getDefaultVersion(),SSLogger.DEBUG);
		SSLogger.log("Individuals count :"+terms.size(),SSLogger.DEBUG);
		Set<TerminologyIndividual> termsToRemove=new HashSet<TerminologyIndividual>();
		Set<TerminologyIndividual> termsToAdd=new HashSet<TerminologyIndividual>();
		Iterator<TerminologyIndividual> termIterator=terms.iterator();
		while(termIterator.hasNext()) {
			TerminologyIndividual term=termIterator.next();
			String currentTermURI=term.getURI();
			int lastIndexOfDash=currentTermURI.lastIndexOf("-");
			if(lastIndexOfDash>0) {
				SSLogger.log("Found possible action for : "+currentTermURI,SSLogger.DEBUG);
				String left=currentTermURI.substring(0, lastIndexOfDash);
				String right=currentTermURI.substring(lastIndexOfDash+1);
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
				if(lastIndexOfDash>beginLeft) {
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
				if(toSuppress) termsToRemove.add(term);
				String root=currentTermURI.substring(0,beginLeft);
				if(maxLimit>0) {
					if(maxIndex-minIndex>maxLimit) {
						if(hardLimitCut) {
							SSLogger.log("Too long, skipping expansion",SSLogger.DEBUG);
							continue;
						}
						else {
							maxIndex=minIndex+maxLimit;
							SSLogger.log("Expanding only from "+minIndex+" to "+maxIndex,SSLogger.DEBUG);
						}
					}
				}
					
				for (int curr=minIndex;curr<=maxIndex;curr++) {
					String numberS=curr+"";							//TODO maybe to make cleaner
					if(pad>0) {
						int padDiff=pad-numberS.length();
						if(padDiff>0) {
							for(int i=0;i<padDiff;i++)
									numberS="0"+numberS;
						}
							
					}
					String newURI=root+lastSeparator+numberS;
					SSLogger.log("Pragma: geration of "+newURI,SSLogger.DEBUG);
					TerminologyIndividual newTerm=myFactory.getOrCreateTerminologyIndividual(newURI,term.getLastVersion());
					term.cloneTo(newTerm);
					String newNS=""+curr;
					newTerm.setLocalNamespace(newNS);
					termsToAdd.add(newTerm);
					//Now the overrideProp bit
					if(overrideProps!=null && overrideProps.size()>0) {
						Model newTermModel=newTerm.getStatements(newTerm.getDefaultVersion());
						propIter=overrideProps.iterator();
						while(propIter.hasNext()) {
							Property ovProp=propIter.next();
								
							Model tempModel=ModelFactory.createDefaultModel(); // TODO this could just be a list of statements
							StmtIterator matchingIter=newTermModel.listStatements(null,ovProp,(Literal)null);
							tempModel.add(matchingIter);
							SSLogger.log("Pragma: stats before override ("+ovProp+")"+newTermModel.size());
							newTermModel.remove(tempModel);
							SSLogger.log("Pragma: after pruning "+newTermModel.size());
							StmtIterator toBeGeneratedIter=tempModel.listStatements();
							while(toBeGeneratedIter.hasNext()) {
								Statement toBeGenStat=toBeGeneratedIter.next();
								RDFNode oldValueNode=toBeGenStat.getObject();
								String oldValueString=oldValueNode.asLiteral().getValue().toString();
								//System.out.println("DEBUG >> "+oldValueString);
								String candidateNewValue=newNS;
								String prefixForComplexCode="";
								
								if(oldValueString.length()>candidateNewValue.length()) {
									//We need to pad the code? from the top, or from the last dot ?
									int lastIndexOfDotInNewCode=oldValueString.lastIndexOf(".");
									if(lastIndexOfDotInNewCode>=0) {
										prefixForComplexCode =oldValueString.substring(0,oldValueString.lastIndexOf("."));
										System.out.println(">>"+prefixForComplexCode);
										System.out.println(">>"+candidateNewValue);
										System.out.println(">>"+oldValueString);
									}
									// Still need padding ?
									int toPad=oldValueString.length()-oldValueString.lastIndexOf("-")-1;
									String filler="0";
									while(candidateNewValue.length()<toPad) {
										candidateNewValue=filler+candidateNewValue;
									}
									if(!prefixForComplexCode.equals("")) 
										candidateNewValue=prefixForComplexCode+"."+candidateNewValue;
										
										
										/*
										if(prefixForComplexCode.length()+candidateNewValue.length()+1<oldValueString.length()) {
											String oldValueRest=oldValueString.substring(oldValueString.lastIndexOf(".")+1);
											System.out.println(">>NewValue: "+candidateNewValue);
											System.out.println(">>OldValueRest: "+oldValueRest);
											//String filler=oldValueRest.substring(0,1);
											String filler="0";
											while(candidateNewValue.length()+prefixForComplexCode.length()+1<oldValueRest.length()) {
												candidateNewValue=filler+candidateNewValue;
											}
											candidateNewValue=prefixForComplexCode+lastSeparator+candidateNewValue;
										}
									}
									else {
										System.out.println(">>NewValue: "+candidateNewValue);
										System.out.println(">>OldValueRest: "+oldValueRest);
										//String filler=oldValueRest.substring(0,1);
										String filler="0";
										while(candidateNewValue.length()<oldValueRest.length()) {
											candidateNewValue=filler+candidateNewValue;
										}
										
									}
									*/
									/*
									if(oldValueString.startsWith("0")) {
									System.out.println("is 0");
									int paddingTo=oldValueString.indexOf('-');
									if(paddingTo>0) {
										if(candidateNewValue.length()<paddingTo) {
											int nOfLoops=paddingTo-candidateNewValue.length();
											System.out.println("To add "+nOfLoops);
											for(int i=0;i<nOfLoops;i++) {
												candidateNewValue="0"+candidateNewValue;
												System.out.println("DEBUG ("+i+")>> old newNS "+candidateNewValue);
											}
												
										}
										
									}
									*/
								}
								//System.out.println("DEBUG >> newNS "+candidateNewValue);
								//System.out.println("Prop >> newNS "+toBeGenStat.getPredicate());
								newTermModel.add(ResourceFactory.createStatement(toBeGenStat.getSubject(), toBeGenStat.getPredicate(), ResourceFactory.createPlainLiteral(candidateNewValue)));
							}
							SSLogger.log("Pragma: after generation "+newTermModel.size());
						}
						
						
						
						
						
							
							
							// Note: the model is a pointer, so we don't need to set anything back
							
					}
						
						
				}
					
					
			}			
		}
		// TODO this is now redundant as copy of container references is implicit in the model
		Iterator<TerminologyIndividual> toAddIter=termsToAdd.iterator();
		while(toAddIter.hasNext()) {
			TerminologyIndividual toAdd=toAddIter.next();
			terminologySet.registerContainedIndividual(toAdd);
		}
		SSLogger.log("Individuals count (after adding) :"+terminologySet.getIndividuals().size(),SSLogger.DEBUG);
			
		Iterator<TerminologyIndividual> toRemoveIter=termsToRemove.iterator();
		while(toRemoveIter.hasNext()) {
			TerminologyIndividual toRemove=toRemoveIter.next();
			terminologySet.unregisterContainedEntity(toRemove);
			// TODO Remove the triples! Not only the reference...
		}
		SSLogger.log("Individuals count (after pruning) :"+terminologySet.getIndividuals().size(),SSLogger.DEBUG);
			
	}


	

}
