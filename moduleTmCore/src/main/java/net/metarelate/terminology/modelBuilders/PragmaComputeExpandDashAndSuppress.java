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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.utils.Loggers;

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
	public void run() throws ImporterException, ModelException {
		Loggers.pragmaLogger.info("Begin Pragma Computation: Dash expander");
		Loggers.pragmaLogger.debug("On collection : "+terminologySet.getURI());
		Loggers.pragmaLogger.debug("To suppress : "+toSuppress);
		Iterator<Property> propIter=overrideProps.iterator();
		while(propIter.hasNext()) {
			Property prop=propIter.next();
			Loggers.pragmaLogger.debug("Override : "+prop);
		}
			
		Loggers.pragmaLogger.trace("Action : expanding individuals with x-y range");
		if(maxLimit>0) {
			Loggers.pragmaLogger.trace("Max expansion: "+maxLimit);
			if(hardLimitCut) Loggers.pragmaLogger.trace("Dropping all if exceeding "+maxLimit);
			else Loggers.pragmaLogger.trace("Expanding only max number if exceeding "+maxLimit);
		}
			
		Set<TerminologyIndividual> terms=terminologySet.getIndividuals();
		Loggers.pragmaLogger.trace("Version (default) :"+terminologySet.getLastVersion());
		Loggers.pragmaLogger.trace("Individuals count :"+terms.size());
		Set<TerminologyIndividual> termsToRemove=new HashSet<TerminologyIndividual>();
		Set<TerminologyIndividual> termsToAdd=new HashSet<TerminologyIndividual>();
		Iterator<TerminologyIndividual> termIterator=terms.iterator();
		while(termIterator.hasNext()) {
			TerminologyIndividual term=termIterator.next();
			String currentTermURI=term.getURI();
			int lastIndexOfDash=currentTermURI.lastIndexOf("-");
			if(lastIndexOfDash>0) {
				Loggers.pragmaLogger.trace("Found possible action for : "+currentTermURI);
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
					Loggers.pragmaLogger.trace("Cannot find left index");
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
					Loggers.pragmaLogger.debug("!!!! Error in parsing "+left+" or "+right);
					continue;
					//return;
				}
				if(toSuppress) termsToRemove.add(term);
				String root=currentTermURI.substring(0,beginLeft);
				if(maxLimit>0) {
					if(maxIndex-minIndex>maxLimit) {
						if(hardLimitCut) {
							Loggers.pragmaLogger.trace("Too long, skipping expansion");
							continue;
						}
						else {
							maxIndex=minIndex+maxLimit;
							Loggers.pragmaLogger.trace("Expanding only from "+minIndex+" to "+maxIndex);
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
					Loggers.pragmaLogger.trace("Pragma: geration of "+newURI);
					TerminologyIndividual newTerm=myFactory.createNewVersionedTerminologyIndividual(newURI);
					newTerm.registerVersion(term.getLastVersion());
					term.cloneTo(newTerm);
					String newNS=""+curr;
					newTerm.setLocalNamespace(newNS);
					termsToAdd.add(newTerm);
					//Now the overrideProp bit
					if(overrideProps!=null && overrideProps.size()>0) {
						Model newTermModel=newTerm.getStatements(newTerm.getLastVersion());
						propIter=overrideProps.iterator();
						while(propIter.hasNext()) {
							Property ovProp=propIter.next();
								
							Model tempModel=ModelFactory.createDefaultModel(); // TODO this could just be a list of statements
							StmtIterator matchingIter=newTermModel.listStatements(null,ovProp,(Literal)null);
							tempModel.add(matchingIter);
							Loggers.pragmaLogger.debug("Pragma: stats before override ("+ovProp+")"+newTermModel.size());
							newTermModel.remove(tempModel);
							Loggers.pragmaLogger.debug("Pragma: after pruning "+newTermModel.size());
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
										Loggers.pragmaLogger.trace(">>"+prefixForComplexCode);
										Loggers.pragmaLogger.trace(">>"+candidateNewValue);
										Loggers.pragmaLogger.trace(">>"+oldValueString);
									}
									// Still need padding ?
									int toPad=oldValueString.length()-oldValueString.lastIndexOf("-")-1;
									String filler="0";
									while(candidateNewValue.length()<toPad) {
										candidateNewValue=filler+candidateNewValue;
									}
									if(!prefixForComplexCode.equals("")) 
										candidateNewValue=prefixForComplexCode+"."+candidateNewValue;
										
										
										
								}
								//System.out.println("DEBUG >> newNS "+candidateNewValue);
								//System.out.println("Prop >> newNS "+toBeGenStat.getPredicate());
								newTermModel.add(ResourceFactory.createStatement(toBeGenStat.getSubject(), toBeGenStat.getPredicate(), ResourceFactory.createPlainLiteral(candidateNewValue)));
							}
							Loggers.pragmaLogger.debug("Pragma: after generation "+newTermModel.size());
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
		Loggers.pragmaLogger.debug("Individuals count (after adding) :"+terminologySet.getIndividuals().size());
			
		Iterator<TerminologyIndividual> toRemoveIter=termsToRemove.iterator();
		while(toRemoveIter.hasNext()) {
			TerminologyIndividual toRemove=toRemoveIter.next();
			terminologySet.unregisterContainedEntity(toRemove);
			// TODO Remove the triples! Not only the reference...
		}
		Loggers.pragmaLogger.debug("Individuals count (after pruning) :"+terminologySet.getIndividuals().size());
			
	}


	

}
