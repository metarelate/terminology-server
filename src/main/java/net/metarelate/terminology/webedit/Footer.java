package net.metarelate.terminology.webedit;

import java.util.GregorianCalendar;

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

public final class Footer extends Panel {
	public Footer(String id) {
		super(id);
		// URL
		// Content negotiation
		// Synchs
		
	    add(new Label("year", "" + new GregorianCalendar().get(GregorianCalendar.YEAR)));
	    add(new Label("version", getApplication().getFrameworkSettings().getVersion()));
	    add(new Label("url",getRequest().getUrl().toString()));
	    
	    Request rq=getRequest();
	    WebRequest wrq=(WebRequest)rq;
	    HttpServletRequest srq=(HttpServletRequest)wrq.getContainerRequest();
	    String fullURL=srq.getRequestURL().toString();
	    String contentType=wrq.getHeader("Accept");
        
	    add(new Label("fullurlrequest",fullURL));
	    //add(new Label("fullurlgenerated",RequestCycle.get().getUrlRenderer().renderFullUrl(
	    //		   Url.parse(urlFor(HomePage.class,null).toString()))));
	    add(new Label("contenttype",contentType));
	    add(new Label("locale",getSession().getLocale().getDisplayName()));
	    try {
	    	InetAddress myAddress=InetAddress.getLocalHost();
	    	add(new Label("localhost",myAddress.getHostName()+" ("+myAddress.getHostAddress()+")"));
	    }
	    catch (UnknownHostException e) {
	    	add(new Label("localhost","Unable to identify local address"));
	    }
	    add(new Label("user",Initializer.defaultUserName));
	  }
}


