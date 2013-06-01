package net.metarelate.terminology.webedit;

import java.util.Arrays;
import java.util.List;

import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ModelException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends SuperPage {
	private static final long serialVersionUID = 1L;

	public HomePage(PageParameters parameters) throws ModelException {
		super(parameters);
		
		
		String rootsString="";
		TerminologySet[] termRoots=CommandWebConsole.myInitializer.myFactory.getRootCollections(); //TODO presumibly, something doesn't look in the right place
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
		
		/*
		for(int i=0;i<termRoots.length;i++) {
			rootsString+=termRoots[i].getLabel(termRoots[i].getDefaultVersion())+"\n";
		}
		add(new MultiLineLabel("body2",rootsString));
		 */
		
		//String url = urlFor(EditPage.class, new PageParameters("id=" + registrationId))
		// TODO Add your page's components here

    }

	@Override
	String getSubPage() {
		return "Search";
	}

	@Override
	String getPageStateMessage() {
		return "Just searching";
	}
}
