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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SystemsUtilities {
	//TODO should be replaced by String.format
	/**
	 * Returns a formatted timeStamp
	 * @return
	 */
	public static String getStandardTimeStamp() {
		Calendar myCalendar=new GregorianCalendar();
		//String timeStamp=Integer.toString(new GregorianCalendar().get(GregorianCalendar.YEAR));
		String timeStamp=Integer.toString(myCalendar.get(GregorianCalendar.YEAR))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.DAY_OF_YEAR))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.HOUR_OF_DAY))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.MINUTE))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.SECOND))+"-"+
				Integer.toString(myCalendar.get(GregorianCalendar.MILLISECOND));
		return timeStamp;
	}
}
