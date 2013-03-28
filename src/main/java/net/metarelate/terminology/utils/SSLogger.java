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

package net.metarelate.terminology.utils;
/**
 * Less than an extremely simple logger. Almost a place-holder for something better.
 * (as soon as there is some time for that!)
 * @author andreasplendiani
 *
 */
public class SSLogger {
	public static int DEBUG=1;
	public static int WARNING=1;
	private static boolean showDebug=false;
	private static boolean showWarning=false;
	
	public static void showDebug(boolean show) {
		showDebug=show;
	}
	public static void showWarning(boolean show) {
		showWarning=show;
	}
	
	public static void log(String message) {
		System.out.println(message);
	}
	public static void log(String message, int level) {
		if(level==DEBUG && showDebug) System.out.println("DEBUG: "+message);
		if(level==WARNING && showWarning) System.out.println("WARNNG: "+message);
		//else System.out.println(message);
	}

}
