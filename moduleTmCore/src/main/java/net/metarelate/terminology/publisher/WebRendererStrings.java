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

// TODO this class should be obsoleted and is left for some references only
package net.metarelate.terminology.publisher;

public final class WebRendererStrings {
	public static final int ALSO_AVAILABLE_IN_LANG=0;
	public static final int HAS_TYPE = 1;
	public static final int DOWNLOAD_IN=2;
	public static final int META_INFO_LABEL_REGISTER=3;
	public static final int META_INFO_LABEL_CODE=4;
	public static final int STATUS=5;
	public static final int UNDEFINED=6;
	public static final int OWNER=7;
	public static final int GENERATION_DATE=8;
	public static final int LAST_UPDATE=9;
	public static final int IN_TAGS=10;
	public static final int VERSION=11;
	public static final int IS_LATEST_VERSION=12;
	public static final int HAS_LATEST_VERSION=13;
	public static final int THIS_VERSION_DATE=14;
	public static final int VERSION_HISTORY=15;
	public static final int DATE=16;
	public static final int ACTION=17;
	public static final int ACTOR=18;
	public static final int DESCRIPTION=19;
	public static final int DEFINED_IN_REGISTER=20;
	public static final int SUBREGISTERS=21;
	public static final int CODES_IN_REGISTER=22;
	public static final int REGISTER_DESCRIPTION=23;
	public static final int CODE_DESCRIPTION=24;
	public static final int FOCUS_CODE_REGISTRY=25;
	public static final int FOCUS_CODE_CODE=26;
	public static final int FOCUS_CONCEPT_REGISTRY=27;
	public static final int FOCUS_CONCEPT_CODE=28;
	public static final int FOCUS_UNDEF=29;
	public static final int ATTRIBUTE=30;
	public static final int VALUE=31;
	public static final int NOCOMMENTPROVIDED=32;
	
	
	private static final String[] enStrings=new String[100];
	private static final String[] itStrings=new String[100];
	
	static {
		enStrings[ALSO_AVAILABLE_IN_LANG]="This page is also available in the following languages";
		itStrings[ALSO_AVAILABLE_IN_LANG]="Questa pagine e' disponibile anche nelle seguenti lingue";
		enStrings[HAS_TYPE]="has type(s)";
		itStrings[HAS_TYPE]="di tipo/i";
		enStrings[DOWNLOAD_IN]="Download this in";
		itStrings[DOWNLOAD_IN]="Scarica queste informazioni in";
		enStrings[META_INFO_LABEL_REGISTER]="Meta Information on the register";
		itStrings[META_INFO_LABEL_REGISTER]="Meta informazioni sul registro";
		enStrings[META_INFO_LABEL_CODE]="Meta Information on this register's entry";
		itStrings[META_INFO_LABEL_CODE]="Meta informazioni su questa voce di registro";
		enStrings[STATUS]="Status";
		itStrings[STATUS]="Stato";
		enStrings[UNDEFINED]="Undefined";
		itStrings[UNDEFINED]="Indefinito";
		enStrings[OWNER]="Manager";			// TODO note as "OWNER" is misleading throughout the system
		itStrings[OWNER]="Responsabile";
		enStrings[GENERATION_DATE]="Generation date";
		itStrings[GENERATION_DATE]="Data di creazione";
		enStrings[LAST_UPDATE]="Last update";
		itStrings[LAST_UPDATE]="Ultimo aggiornamento";
		enStrings[IN_TAGS]="In releases";
		itStrings[IN_TAGS]="Presente nelle releases";
		enStrings[VERSION]="Version";
		itStrings[VERSION]="Versione";
		enStrings[IS_LATEST_VERSION]="This is the latest version";
		itStrings[IS_LATEST_VERSION]="Questa e' la versione piu' recente";
		enStrings[HAS_LATEST_VERSION]="The latest version available is";
		itStrings[HAS_LATEST_VERSION]="L'ultima versione disponibile e'";
		enStrings[THIS_VERSION_DATE]="This version date";
		itStrings[THIS_VERSION_DATE]="Data della versione";
		enStrings[VERSION_HISTORY]="Version history";
		itStrings[VERSION_HISTORY]="Versioni precedenti";
		enStrings[DATE]="Date";
		itStrings[DATE]="Data";
		enStrings[ACTION]="Operation";
		itStrings[ACTION]="Operazione";
		enStrings[ACTOR]="Agent";
		itStrings[ACTOR]="Esecutore";
		enStrings[DESCRIPTION]="Description";
		itStrings[DESCRIPTION]="Descrizione";
		enStrings[DEFINED_IN_REGISTER]="Defined in register";
		itStrings[DEFINED_IN_REGISTER]="Super-registro";
		enStrings[SUBREGISTERS]="Sub registers";
		itStrings[SUBREGISTERS]="Sottoregistri";
		enStrings[CODES_IN_REGISTER]="Codes in this register";
		itStrings[CODES_IN_REGISTER]="Codici in questo registro";
		enStrings[REGISTER_DESCRIPTION]="Register description";
		itStrings[REGISTER_DESCRIPTION]="Descrizione del registro";
		enStrings[CODE_DESCRIPTION]="Code description";
		itStrings[CODE_DESCRIPTION]="Descrizione del codice";
		enStrings[ATTRIBUTE]="Attribute";
		itStrings[ATTRIBUTE]="Attributo";
		enStrings[VALUE]="Value";
		itStrings[VALUE]="Valore";
		enStrings[FOCUS_CODE_REGISTRY]="About register";
		itStrings[FOCUS_CODE_REGISTRY]="Del registro";
		enStrings[FOCUS_CODE_CODE]="About code";
		itStrings[FOCUS_CODE_CODE]="Del codice";
		enStrings[FOCUS_CONCEPT_REGISTRY]="About this set of concepts";
		itStrings[FOCUS_CONCEPT_REGISTRY]="Dell'insieme dei concetti";
		enStrings[FOCUS_CONCEPT_CODE]="About the represented concept";
		itStrings[FOCUS_CONCEPT_CODE]="Del concetto rappresentato";
		enStrings[FOCUS_UNDEF]="";
		itStrings[FOCUS_UNDEF]="";
		enStrings[NOCOMMENTPROVIDED]="";
		itStrings[NOCOMMENTPROVIDED]="";
		
		
	}
	//enStrings[ALSO_AVAILABLE_IN_LANG] = "This page is also available in the following languages";

	public static String getValueFor(int stringNumber, String lang){
		if(lang.equals("en")) return enStrings[stringNumber];
		else if(lang.equals("it")) return itStrings[stringNumber];
		else return null;
		
	}
	

	public static String getLangKeyExpanded(String langKey) {
		if(langKey.equals("en")) return "English";
		else if (langKey.equals("it")) return "Italiano";
		else return null;
	}
}

