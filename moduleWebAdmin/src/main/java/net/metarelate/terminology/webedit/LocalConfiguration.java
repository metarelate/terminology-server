package net.metarelate.terminology.webedit;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public final class LocalConfiguration extends SuperPage {
	public LocalConfiguration(final PageParameters parameters) {
		super(parameters);
		
	    add(new Label("wicketVersion", getApplication().getFrameworkSettings().getVersion()));
	    add(new Label("locale",getSession().getLocale().getDisplayName()));
	    try {
	    	InetAddress myAddress=InetAddress.getLocalHost();
	    	add(new Label("localAddress",myAddress.getHostName()+" ("+myAddress.getHostAddress()+")"));
	    }
	    catch (UnknownHostException e) {
	    	add(new Label("localAddress","Unable to identify local address"));
	    }
	    
	    /* Part of this is going to be moved somewhere else (TODO)
	    add(new Label("url",getRequest().getUrl().toString()));
	    
	    Request rq=getRequest();
	    WebRequest wrq=(WebRequest)rq;
	    HttpServletRequest srq=(HttpServletRequest)wrq.getContainerRequest();
	    String fullURL=srq.getRequestURL().toString();
	    String contentType=wrq.getHeader("Accept");
        
	    add(new Label("fullurlrequest",fullURL));
	   
	    add(new Label("contenttype",contentType));
	    
	    
	    add(new Label("user",Initializer.defaultUserName));
	    */
	  }


	@Override
	String getSubPage() {
		return "Local configuration";
	}

	@Override
	String getPageStateMessage() {
		return "";
	}
}
