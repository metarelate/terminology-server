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

import com.hp.hpl.jena.rdf.model.Model;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.utils.SSLogger;

public abstract class PragmaProcessor {
	TerminologyFactory myFactory=null;
	//Model globalConfigurationModel=null;
	
	public PragmaProcessor(TerminologyFactory factory) {
		this.myFactory = factory;
		//this.globalConfigurationModel = globalConfigurationModel;
	}

	public abstract void run();
	
	protected int getNumber(String uri) {
		//////
		int result=-1;
		int lastIndexOfSlah=uri.lastIndexOf('/');
		int lastIndexOfPound=uri.lastIndexOf('#');
		int lastIndexOfDot=uri.lastIndexOf('.');
		int beginLeft=-1;
		if(lastIndexOfPound>beginLeft) {
			beginLeft=lastIndexOfPound;
		}
	
		if(lastIndexOfSlah>beginLeft) {
			beginLeft=lastIndexOfSlah;
		}
		if(lastIndexOfDot>beginLeft) {
			beginLeft=lastIndexOfDot;
		}
		//System.out.println("begin left : "+beginLeft);
		if(beginLeft<0) {
			SSLogger.log("Cannot find a number for "+uri,SSLogger.DEBUG);
			return -1;
		}
		String numberS=uri.substring(beginLeft+1);
		try {
			result=Integer.parseInt(numberS);
		}
		catch(Exception e) {
			SSLogger.log("!!!! Error in parsing "+numberS,SSLogger.DEBUG);
			return -1;
		}
		return result;
	}
	
	
}
