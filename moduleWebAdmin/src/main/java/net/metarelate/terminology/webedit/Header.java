package net.metarelate.terminology.webedit;

import java.util.GregorianCalendar;

import net.metarelate.terminology.config.CoreConfig;
import net.metarelate.terminology.instanceManager.Initializer;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

public final class Header extends Panel {
	public Header(String id) {
		super(id);
		add(new Label("hdlabel","Terminology Server v."+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")"));
		// Nothing to do now, but eventually we will have the menu here
	}
	  
}


