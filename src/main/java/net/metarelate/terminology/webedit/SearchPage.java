package net.metarelate.terminology.webedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.metarelate.terminology.coreModel.TerminologySet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class SearchPage extends SuperPage {
	private static final long serialVersionUID = 1L;

	public SearchPage(PageParameters parameters) {
		super(parameters);
		
		final Label resultLabel=new Label("resultPanel","Nothing yet");
		resultLabel.setOutputMarkupId(true);
		add(resultLabel);
		
		
		/*
		AjaxLink openRegButton = new AjaxLink("openRegButton") {
		    public void onClick(final AjaxRequestTarget target) {
		        resultLabel.setDefaultModelObject("Test");
		        target.add(resultLabel);
		    }
		};
		add(openRegButton);
		*/
		

		TerminologySet[] termRoots=CommandWebConsole.myFactory.getRootCollections();
		List<String> termRootsURIList=new ArrayList<String>();
		for(int i=0;i<termRoots.length;i++) {
			termRootsURIList.add(termRoots[i].getURI());
		}
		//TODO presumibly, something doesn't look in the right place
		final ListView<String> termRootsListView=new RegisterListView("registerList", termRootsURIList);
		WebMarkupContainer listContainer=new WebMarkupContainer("listPanel");
		((RegisterListView) termRootsListView).registerView(termRootsListView);
		((RegisterListView) termRootsListView).registerPanel(listContainer);
		
		termRootsListView.setOutputMarkupId(true);
		listContainer.setOutputMarkupId(true);
		
		
		listContainer.add(termRootsListView);
		add(listContainer);
	
		
		
	}

	@Override
	String getSubPage() {
		return "Search";
	}

	@Override
	String getCoreMessage() {
		return "Just searching";
	}
	
	private class RegisterListView extends ListView<String> {
		ListView<String> termRootsListViewlistToUpdate=null;
		WebMarkupContainer viewPanel=null;
		public RegisterListView(String id,List<String> list) {
			super(id,list);
			
		}
		public void registerView(ListView<String> termRootsListView) {
			termRootsListViewlistToUpdate=termRootsListView;
		}
		public void registerPanel(WebMarkupContainer viewPanel) {
			this.viewPanel=viewPanel;
		}
		

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		protected void populateItem(final ListItem<String> item) {
			final TerminologySet thisSet=CommandWebConsole.myFactory.getOrCreateTerminologySet((String)item.getModelObject());
			BookmarkablePageLink pageUpLink=new BookmarkablePageLink("registerBackLink",EditPage.class);
	    	pageUpLink.getPageParameters().set("entity", thisSet.getURI());
	    	pageUpLink.add(new Label("registerBackLabel","<<"));
	        
	    	
	    	BookmarkablePageLink pageLink=new BookmarkablePageLink("registerLink",EditPage.class);
	    	pageLink.getPageParameters().set("entity", thisSet.getURI());
	    	pageLink.add(new Label("registerLabel",thisSet.getLabel(thisSet.getLastVersion())));
	        
	    	AjaxLink pageDownLink=new AjaxLink("registerDownLink") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					Iterator<TerminologySet> newListToBe=thisSet.getCollections().iterator();
					List<String> newList=new ArrayList<String>();
					while(newListToBe.hasNext()) newList.add(newListToBe.next().getURI());
					
					termRootsListViewlistToUpdate.setDefaultModelObject(newList);
					target.add(viewPanel);
				}

	    		
	    	};
	    	
	    	pageDownLink.add(new Label("registerDownLabel",">>"));
	        
	    	item.add(pageUpLink);
	    	item.add(pageLink);
	    	item.add(pageDownLink);
	    	//item.add(new Label("label", item.getModelObject().getLabel(item.getModelObject().getLastVersion())));
			
		}
		
	}
	
	/*
	private class TerminologySetDecoupler implements IModel {
		private String setURI=null;
		//TODO some type checking would be good...
		public void detach() {
			// TODO NOt sure what to do here but most likely we don't need it.
			
		}

		public Object getObject() {
			// TODO very dangerous, we are going to create things out of nothing is something is wrong
			return CommandWebConsole.myFactory.getOrCreateTerminologySet(setURI);
			
		}

		public void setObject(Object termSet) {
			setURI=((TerminologySet)termSet).getURI();
			
		}
	}
	*/
}
