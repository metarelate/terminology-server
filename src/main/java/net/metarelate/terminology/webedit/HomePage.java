package net.metarelate.terminology.webedit;

import java.util.Arrays;
import java.util.List;

import net.metarelate.terminology.coreModel.TerminologySet;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		super(parameters);
		
		
		String rootsString="";
		TerminologySet[] termRoots=CommandWebConsole.myFactory.getRootCollections();
		List<TerminologySet> termRootsList=Arrays.asList(termRoots);
		
		ListView<TerminologySet> termRootsListView = new ListView<TerminologySet>("body2", termRootsList) {
		    protected void populateItem(ListItem<TerminologySet> item) {
		    	BookmarkablePageLink pageLink=new BookmarkablePageLink("link",EditPage.class);
		    	pageLink.getPageParameters().set("entity", item.getModelObject().getURI());
		    	pageLink.add(new Label("label",item.getModelObject().getLabel(item.getModelObject().getLastVersion())));
		        item.add(pageLink);
		    	//item.add(new Label("label", item.getModelObject().getLabel(item.getModelObject().getLastVersion())));
		    }
		};
		add(termRootsListView);
		
		add(new Footer("ft"));
		/*
		for(int i=0;i<termRoots.length;i++) {
			rootsString+=termRoots[i].getLabel(termRoots[i].getDefaultVersion())+"\n";
		}
		add(new MultiLineLabel("body2",rootsString));
		 */
		
		//String url = urlFor(EditPage.class, new PageParameters("id=" + registrationId))
		// TODO Add your page's components here

    }
}
