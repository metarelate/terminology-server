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
	
package net.metarelate.terminology.coreModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.metarelate.terminology.utils.Loggers;


/**
 * Provides services relative to versioning. 
 * A versioner is an object that is attached to a terminology entity.
 * TODO this class could probably be converted to static, saving footprint. 
 * @author andreasplendiani
 *
 */

public class Versioner {
	private TerminologyEntity vEntity=null;
	private static Pattern versionNumberPattern=Pattern.compile("(\\w+)(\\d+)");
	
	Versioner(TerminologyEntity entity) {
		vEntity=entity;
	}
	
	/**
	 * Return true if the specified version is the last version for the entity this versioner is relative to.
	 * "Last" means that no subsequent version is "linked". Multiple version could be the "last version" if multiple versionig threads are implemented. 
	 * @param version
	 * @return
	 */
	public boolean isLastVersion(String version) {
		if(vEntity.getNextVersion(version)==null) return true;
		else return false;
		
	}
	
	/**
	 * Return true if the specified version is the newest version for the entity this versioner is relative to.
	 * "Newest" means that no precedent version is "linked". Multiple version could be the "newest version" if multiple versionig threads are implemented. 
	 * @param version
	 * @return
	 */
	public boolean hasPreviousVersion(String version) {
		if(vEntity.getPreviousVersion(version)==null) return false;
		else return true;
	}
	
	/**
	 * Returns the last version for the entity this versioner is relative to.
	 * TODO note that in the current implementation a random versioning thread is taken by chance.
	 * In other words, this method is not compatible with multi-version threads
	 * @return
	 */
	public String getNewestVersion() {
		String[] versions=vEntity.getVersions();
		String newest=versions[0];
		for(int i=1;i<versions.length;i++) {
			if(vEntity.getNextVersion(versions[i])==null) newest=versions[i];
		}
		return newest;
	}
	
	/**
	 * Returns the first version for the entity this versioner is relative to.
	 * TODO note that in the current implementation a random versioning thread is taken by chance.
	 * In other words, this method is not compatible with multi-version threads
	 * @return
	 */
	public String getFirstVersion() {
		String[] versions=vEntity.getVersions();
		String first=versions[0];
		for(int i=1;i<versions.length;i++) {
			if(vEntity.getPreviousVersion(versions[i])==null) first=versions[i];
		}
		return first;
	}

	/**
	 * Returns the generation date of the entity this versioner is attached to.
	 * In the current implementation, this is the first date recorded for any version.
	 * TODO note as date ordering could be made more robust by using date objects, instead of strings.
	 * @return
	 */
	public String getGenerationDate() {
		String[] versions=vEntity.getVersions();
		Set<String> dates=new HashSet<String>();
		for(int i=0;i<versions.length;i++) {
			if(vEntity.getActionDate(versions[i])!=null) {
				dates.add(vEntity.getActionDate(versions[i]));
			}
		}
		Iterator<String> dateS=dates.iterator();
		if(!dateS.hasNext()) return null;
		String min=dateS.next();
		while(dateS.hasNext()) {
			String curr=dateS.next();
			if(curr.compareToIgnoreCase(min)<0) min=curr;
		}
		return min;
	}
	
	/**
	 * Returns the last update date of the entity this versioner is attached to.
	 * In the current implementation, this is the last date recorded for any version.
	 * TODO note as date ordering could be made more robust by using date objects, instead of strings.
	 * TODO obsolete ?
	 * @return
	 */
	public String getLastUpdateDate() {
		String[] versions=vEntity.getVersions();
		Set<String> dates=new HashSet<String>();
		for(int i=0;i<versions.length;i++) {
			if(vEntity.getActionDate(versions[i])!=null) {
				dates.add(vEntity.getActionDate(versions[i]));
			}
		}
		Iterator<String> dateS=dates.iterator();
		if(!dateS.hasNext()) return null;
		String max=dateS.next();
		while(dateS.hasNext()) {
			String curr=dateS.next();
			if(curr.compareToIgnoreCase(max)>0) max=curr;
		}
		return max;
	}
	
	/**
	 * Create a string for the next version.
	 * @param version the current version (assumed to follow a pattern: alphanumeric-numeric (e.g. v0).
	 * @return the string for the next version (e.g.: v1)
	 */
	public static String createNextVersion(String version) {
		Loggers.coreLogger.trace("Generating next version for "+version);
		int currentVersionNumber=-1;
		try {
			currentVersionNumber=Integer.parseInt(version);
		} catch (NumberFormatException e) {};
		if(currentVersionNumber>=0) {
			currentVersionNumber++;
			return new Integer(currentVersionNumber).toString();
		}
		//If we are here, version was not an int, and we fall back to the previous approach
		Matcher m=versionNumberPattern.matcher(version);
		if(!m.matches()) {
			Loggers.coreLogger.error("Unable to understand version String "+version);
		}
		String root=m.group(1);
		String no=m.group(2);
		int noInt=Integer.parseInt(no);
		noInt++;
		return root+noInt;
	}
	
}
