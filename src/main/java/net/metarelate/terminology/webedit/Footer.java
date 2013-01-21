package net.metarelate.terminology.webedit;

import net.metarelate.terminology.utils.SystemsUtilities;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public final class Footer extends Panel {
	public Footer(String id) {
		super(id);
		
	    add(new Label("timeStamp", SystemsUtilities.getStandardTimeStamp()));
	    add(new Label("localAddress",CommandWebConsole.myInitializer.getServerName()));
	    add(new Label("loggedInUser",CommandWebConsole.myInitializer.getDefaultUserURI()));
	    
	}
}


