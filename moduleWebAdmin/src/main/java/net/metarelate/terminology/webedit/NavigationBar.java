package net.metarelate.terminology.webedit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;

public class NavigationBar extends Panel {
	public NavigationBar (String id, int selectedPanel) {
		super(id);
		
		BookmarkablePageLink managementLink=new BookmarkablePageLink("managementLink",SearchPage.class);
		if(selectedPanel==CommandWebConsole.MANAGEMENT_PAGE) managementLink.add(new Label("managementLabel","<strong>Management</strong>").setEscapeModelStrings(false));
		else managementLink.add(new Label("managementLabel","Management"));
		add(managementLink);
		
		BookmarkablePageLink localConfigurationLink=new BookmarkablePageLink("localConfigurationLink",LocalConfiguration.class);
		if(selectedPanel==CommandWebConsole.LOCAL_CONFIGURATION_PAGE) localConfigurationLink.add(new Label("localConfigurationLabel","<strong>Local configuration</strong>").setEscapeModelStrings(false));
		else localConfigurationLink.add(new Label("localConfigurationLabel","Local configuration"));
		add(localConfigurationLink);
		

		
	}
}

