package net.metarelate.terminology.webedit;


import net.metarelate.terminology.config.CoreConfig;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class SuperPage extends WebPage {
	private static final long serialVersionUID = 1L;
	public String getPageTitle() {
		return "Terminology Manager";
	}
	public String getPageSubTitle() {
		return "v. "+CoreConfig.VERSION_NUMBER+" ("+CoreConfig.VERSION_CODENAME+")";
	}
	abstract String getSubPage();
	abstract String getCoreMessage();
	public SuperPage(PageParameters parameters) {
		super(parameters);
		// More to do..
		
		
		add(new Label("title",getPageTitle()));
		add(new Label("subTitle",getPageSubTitle()));
		add(new NavigationBar("termNavBar",CommandWebConsole.LOCAL_CONFIGURATION_PAGE));
		add(new Label("subPage",getSubPage()));
		add(new Label("coreMessage",getCoreMessage()));
		add(new Footer("termFooter"));
	

    }
}
