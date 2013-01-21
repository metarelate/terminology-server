package net.metarelate.terminology.webedit;

import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ViewPage extends SuperPage {
	String urlToView="";
	public ViewPage(final PageParameters parameters) {
		super(parameters);
		urlToView=parameters.get("entity").toString();
	}
	
	@Override
	String getSubPage() {
		return "Entity details and operations";
	}

	@Override
	String getCoreMessage() {
		return "no message";
	}

}
