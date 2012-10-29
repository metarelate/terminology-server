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

package uk.gov.metoffice.terminology.utils;

import java.util.Comparator;

public class CodeComparator implements Comparator<String> {
	@Override
	public int compare(String code1, String code2) {
		//System.out.println("Compare");
		//System.out.println("1: "+code1);
		//System.out.println("2: "+code2);
		Integer intLeft=null;
		Integer intRight=null;
		boolean bothInts=false;
		try {
			intLeft=Integer.parseInt(code1);
			intRight=Integer.parseInt(code2);
			bothInts=true;
		} catch (Exception e) {
			
		};
		if(bothInts) {
			//System.out.println("Int :"+intLeft.compareTo(intRight));
			if(intLeft.compareTo(intRight)==0 && code1.length()!=code2.length()) {
				if(code1.length()<code2.length()) return 1;
				else return -1;
			}
			return(intLeft.compareTo(intRight));
		}
		else if(code1.contains(".") && code2.contains(".")){
			String first[]=code1.split("\\.");
			String second[]=code2.split("\\.");
			//System.out.println("Chopped in "+first.length+" , "+second.length);
			int i=0;
			intLeft=null;
			intRight=null;
			int result=0;
			while(i<first.length && i< second.length) {
				try {
					intLeft=new Integer(first[i]);
					intRight=new Integer(second[i]);
				} catch (Exception e) {
					//System.out.println("Not Ints! : "+first[i]+" and "+second[i]);
					return code1.compareTo(code2);
				}
				//System.out.println("dot iter("+i+") :"+intLeft.compareTo(intRight));
				result=intLeft.compareTo(intRight);
				if(result!=0) return result;
				i=i+1;
			}
			return code1.compareTo(code2);
		}
		else {
			//System.out.println("STR:"+code1.compareTo(code2));
			return code1.compareTo(code2);
			
		}
		
	}

}
