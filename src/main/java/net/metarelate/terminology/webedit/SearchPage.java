package net.metarelate.terminology.webedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologySet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.CheckFolder;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.StyledLinkLabel;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.tree.LinkIconPanel;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;

import java.lang.String;


public class SearchPage extends SuperPage {
	private static final long serialVersionUID = 1L;

	public SearchPage(PageParameters parameters) {
		super(parameters);
		
		// TODO this is the result panel, should get to be something different
		final Label resultLabel=new Label("resultPanel","Nothing yet");
		resultLabel.setOutputMarkupId(true);
		add(resultLabel);
		
		
		//TODO here we should add some form logic for search
		
		
		// TODO here we have the register tree
		/*
		final AbstractTree registerTree=new MyDefaultNestedTree("registerTree",createRegRootModel()) {

			@Override
			protected Component newContentComponent(String id, IModel model) {
				// TODO Auto-generated method stub
				return new Folder(id,this,model) ;
			}
			
		};
		*/
		final AbstractTree registerTree=new DefaultNestedTree("registerTree",createRegRootModel())  {
			@Override
			protected Component newContentComponent(final String id, final IModel model) {
				return new Folder(id, this, model) {
					private static final long serialVersionUID = 1L;

				            /**
				             * Always clickable.
				             */
				            @Override
				            protected boolean isClickable()
				            {
				                return true;
				            }

				            @Override
				            protected void onClick(final AjaxRequestTarget target)
				            {
				            	resultLabel.setDefaultModelObject(model.getObject().toString());
				   		        target.add(resultLabel);
				                // TODO ok, here we should link to search
				            	
				            }

				            @Override
				            protected boolean isSelected()
				            {
				                return false;
				            }

							@Override
							protected Component newLabelComponent(String id,
									IModel model) {
								String myLabel=model.getObject().toString();
								if(CommandWebConsole.myFactory.terminologySetExist(myLabel))
									myLabel=CommandWebConsole.myFactory.getOrCreateTerminologySet(myLabel).getLabel(CommandWebConsole.myFactory.getOrCreateTerminologySet(myLabel).getLastVersion());
									//TODO terrible!!!!! Must be cleaned up a bit
								return new Label("label",myLabel);
							}

			

							
				     
				
				};
			}
		};
		
		
		
		add(registerTree);
		

		
		
		
		
		
		

		 
		    
		/*
		AjaxLink openRegButton = new AjaxLink("openRegButton") {
		    public void onClick(final AjaxRequestTarget target) {
		        resultLabel.setDefaultModelObject("Test");
		        target.add(resultLabel);
		    }
		};
		add(openRegButton);
		*/
		
		/*
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
		*/
		
		
	}
	
	
	
	/*
	private abstract class TermRegStyledLinkLabel<T> extends StyledLinkLabel<T> {
		public TermRegStyledLinkLabel(String id,IModel<T> model) {
			super(id, model);
			 MarkupContainer link = newLinkComponent("link", model);
             link.add(STYLE_CLASS);
             add(link);

             link.add(newLabelComponent("label", model));
			// TODO Auto-generated constructor stub
		}
	}
	
	private  class TermRegFolder<T> extends TermRegStyledLinkLabel<T> {
		public TermRegFolder(String id, IModel<T> model) {
			super(id, model);
			
		}

		@Override
		protected java.lang.String getStyleClass() {
			// TODO Auto-generated method stub
			return null;
		}
	
    };
    */
	
	private class MyDefaultNestedTree extends DefaultNestedTree {

		public MyDefaultNestedTree(String id, ITreeProvider provider) {
			super(id, provider);
		}
		
	}

	private ITreeProvider<String> createRegRootModel() {
		return new ITreeProvider<String>(){

			public void detach() {
				// TODO Auto-generated method stub
				
			}

			public Iterator<String> getChildren(String collection) {
				ArrayList<String> resultList=new ArrayList<String>();
				if(CommandWebConsole.myFactory.terminologySetExist(collection)) {
					Set<TerminologySet> children=CommandWebConsole.myFactory.getOrCreateTerminologySet(collection).getCollections();
					Iterator<TerminologySet> setIter=children.iterator();
					while(setIter.hasNext()) {
						resultList.add(setIter.next().getURI())
;					}
				}
				return resultList.iterator();
			}

			public Iterator<String> getRoots() {
				TerminologySet[] roots=CommandWebConsole.myFactory.getRootCollections();
				ArrayList<String> rootList=new ArrayList<String>();
				for(int i=0;i<roots.length;i++) {
					rootList.add(roots[i].getURI());
				}
				return rootList.iterator();
			}

			public boolean hasChildren(String collection) {
				if(!CommandWebConsole.myFactory.terminologySetExist(collection)) return false;
				Set<TerminologySet> children=CommandWebConsole.myFactory.getOrCreateTerminologySet(collection).getCollections();
				if(children.size()>0) return true;
				else return false;
			}

			public IModel<String> model(String arg) {
				return new Model<String>(arg);
				
			}
			
		};
		
	}

	@Override
	String getSubPage() {
		return "Search";
	}

	@Override
	String getCoreMessage() {
		return "Just searching";
	}
	
	/*
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
	*/
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
