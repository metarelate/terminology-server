package net.metarelate.terminology.webedit;


import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SuperPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public SuperPage(PageParameters parameters) {
		super(parameters);
		
		add(new Header("hd"));
		
		add(new Footer("ft"));
	

    }
}
