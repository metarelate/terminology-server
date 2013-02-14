package net.metarelate.terminology.webedit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.StringValidator;


public class SearchPage extends SuperPage {
	private static final long serialVersionUID = 1L;
	boolean hasSuperseder=false;
	String urlToSupersed=null;
	String pageStateMessageString="Just searching";
	public SearchPage(PageParameters parameters) {
		super(parameters);
		urlToSupersed=parameters.get("superseding").toString();
		if(urlToSupersed!=null) {
			hasSuperseder=true;
			pageStateMessageString="Search for a term to supersed: "+urlToSupersed;
		}
		postConstructionFinalize();
		//final Label resultLabel=new Label("resultLabel","Nothing yet");
		//resultLabel.setOutputMarkupId(true);
		//add(resultLabel);
		
		/*********************************************************************
		 * Result Table
		 *********************************************************************/
		
		final SearchResultList resultList=new SearchResultList();
		
		final MarkupContainer resContainer=new WebMarkupContainer("resultRenderer");
		final DataView resultView=new DataView<String>("simpleResults", resultList) {
			private static final long serialVersionUID = 1L;

		            @Override
		            protected void populateItem(final Item<String> item)
		            {
		                String elementURI = item.getModelObject();
		                if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(elementURI)) {
		                	item.add(new Label("resultType","Set"));
		                	// TODO we coould have something more personalized here (Collection, Register..) or use an image...
		                	String lastVersion=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(elementURI).getLastVersion();
		                	String idLabel=SimpleQueriesProcessor.getOptionalLiteralValueAsString(CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(elementURI).getResource(), MetaLanguage.notationProperty, CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(elementURI).getStatements(lastVersion));
		                	
		                	
		                	String lastStatusURI=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(elementURI)
		                	.getGenericVersionSpecificURIObject(MetaLanguage.hasStatusProperty,lastVersion);
		                	
		                	String lastStatus=CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(lastStatusURI, LabelManager.URI_IF_NULL);
		                	if(idLabel==null) idLabel="undefined";
		                	if(lastStatus==null) lastStatus="undefined";
		                	item.add(new Label("resultID",idLabel));
		                	item.add(new Label("resultDescription",CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(elementURI).getLabel(lastVersion)));
		                	BookmarkablePageLink pageLink=new BookmarkablePageLink("resultURI",ViewPage.class);
		                	if(hasSuperseder) {
		                		System.out.println("has: "+elementURI);
		                		pageLink.getPageParameters().set("entity", urlToSupersed);
		                		pageLink.getPageParameters().set("superseder", elementURI);
		        				
		                	}
		                	else {
		                		System.out.println("has NOT ");
		                		pageLink.getPageParameters().set("entity", elementURI);
			    		    	
		                	}
		                	pageLink.add(new Label("resultURILabel",elementURI));
		                	item.add(pageLink);
		                	item.add(new Label("resultLastVersion",lastVersion));
		                	item.add(new Label("resultStatus",lastStatus));
		                }
		                else if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(elementURI)) {
		                	item.add(new Label("resultType","Individual"));
		                	// TODO same note as above
		                	String lastVersion=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(elementURI).getLastVersion();
		                	String idLabel=SimpleQueriesProcessor.getOptionalLiteralValueAsString(CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(elementURI).getResource(), MetaLanguage.notationProperty, CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(elementURI).getStatements(lastVersion));
		                	
		                	String lastStatusURI=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(elementURI)
				                	.getGenericVersionSpecificURIObject(MetaLanguage.hasStatusProperty,lastVersion);
				                	
				            String lastStatus=CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(lastStatusURI, LabelManager.URI_IF_NULL);
		                	

		                	if(idLabel==null) idLabel="undefined";
		                	if(lastStatus==null) lastStatus="undefined";
		                	item.add(new Label("resultID",idLabel));
		                	item.add(new Label("resultDescription",CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(elementURI).getLabel(lastVersion)));
		                	BookmarkablePageLink pageLink=new BookmarkablePageLink("resultURI",ViewPage.class);
		                	if(hasSuperseder) {
		                		System.out.println("has: "+elementURI);
		                		pageLink.getPageParameters().set("entity", urlToSupersed);
		                		pageLink.getPageParameters().set("superseder", elementURI);
		        				
		                	}
		                	else {
		                		System.out.println("has NOT ");
		                		pageLink.getPageParameters().set("entity", elementURI);
			    		    	
		                	}
		    		    	pageLink.add(new Label("resultURILabel",elementURI));
		                	item.add(pageLink);
		                	item.add(new Label("resultLastVersion",lastVersion));
		                	item.add(new Label("resultStatus",lastStatus));
		                }
		                else {
		                	item.add(new Label("resultType","?"));
		                	// TODO same note as above
		                	item.add(new Label("resultID","Undefined"));
		                	item.add(new Label("resultDescription","Undefined"));
		                	item.add(new Label("resultURI",elementURI));
		                	item.add(new Label("resultLastVersion","undefined"));
		                	item.add(new Label("resultStatus","undefined"));
		                	
		                	
		                	
		                	//TODO to check: this may break Wicket Code as there are no instructions to render a link
		                }
		          
		              
		            }
		        };
		        resultView.setOutputMarkupId(true);
		        resContainer.add(resultView);
		        resContainer.setOutputMarkupId(true);
		        add(resContainer);
		
		/*********************************************************************
		* Ajax query form
	    *********************************************************************/
		        
		final FormComponent queryField = new RequiredTextField<String>("queryText", new Model());        
		queryField.add(new StringValidator(3,null));    
		
		final FormComponent regCheckbox=new CheckBox("queryOnReg",new Model(Boolean.FALSE));
		regCheckbox.setOutputMarkupId(true);
		
		final FormComponent codeCheckbox=new CheckBox("queryOnCode",new Model(Boolean.TRUE));
		codeCheckbox.setOutputMarkupId(true);
		
		regCheckbox.add(new AjaxEventBehavior("onclick") {

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				codeCheckbox.setModelObject(Boolean.FALSE);
				regCheckbox.setModelObject(Boolean.TRUE);
				target.add(codeCheckbox);
				target.add(regCheckbox);
				//System.out.println("REG");
				//System.out.println("Code: "+codeCheckbox.getModelObject().toString());
				//System.out.println("Reg: "+regCheckbox.getModelObject().toString());
			}
			
		});
		
		
		codeCheckbox.add(new AjaxEventBehavior("onclick") {

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				regCheckbox.setModelObject(Boolean.FALSE);
				codeCheckbox.setModelObject(Boolean.TRUE);
				target.add(regCheckbox);
				target.add(codeCheckbox);
				//System.out.println("CODE");
				//System.out.println("Code: "+codeCheckbox.getModelObject().toString());
				//System.out.println("Reg: "+regCheckbox.getModelObject().toString());
			}
			
		});
			
		
		

	       
		
		Form<?> form = new Form<Void>("queryForm") {        
			 

	       
		};        
		
		AjaxFormValidatingBehavior.addToAllFormComponents(form, "keydown", Duration.ONE_SECOND);

		form.add(queryField);
		form.add(regCheckbox);
		form.add(codeCheckbox);
		
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
	    feedback.setOutputMarkupId(true);
	    add(feedback);
		
		 // add a button that can be used to submit the form via ajax
        form.add(new AjaxButton("search", form)
        {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form)
            {
            	Set<String> testAnswer=new HashSet<String>();
            	String textQueryString=queryField.getModelObject().toString();
            	
            	if(((Boolean)codeCheckbox.getModelObject()).booleanValue()) testAnswer.addAll(CommandWebConsole.myInitializer.myFactory.extractIndividualsWithMarchingValue(textQueryString));
            	if(((Boolean)regCheckbox.getModelObject()).booleanValue()) testAnswer.addAll(CommandWebConsole.myInitializer.myFactory.extractSetsWithMarchingValue(textQueryString));
            			
       
            	
            	/*
            	String[] testResults=new String[testAnswer.size()];
            	Iterator<TerminologyEntity> answIter=testAnswer.iterator();
            	int i=0;
            	while(answIter.hasNext()) {
            		testResults[i]=answIter.next().getURI();
            		i++;
            	}
            	*/
            	resultList.changeTo(testAnswer.toArray(new String[0]));
		        target.add(resContainer);
                // repaint the feedback panel so that it is hidden
                target.add(feedback);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form)
            {
                // repaint the feedback panel so errors are shown
                target.add(feedback);
            }
        });
		
		add(form);
		form.setOutputMarkupId(true);

		
		/*********************************************************************
		 * Regsiters tree
		 *********************************************************************/
		
		AbstractTree<String> registerTree=new DefaultNestedTree<String>("registerTree",createRegRootModel())  {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
				            	String selectedSetURI=model.getObject().toString();
				            	//resultLabel.setDefaultModelObject(selectedSetURI);
				   		        //target.add(resultLabel);
				   		        String res[]=null;
				   		        if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(selectedSetURI)) {
				   		        	//TODO check consistency of defaults (versions)
				   		        	Set<TerminologyIndividual> childrenSet=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(selectedSetURI).getIndividuals();
				   		        	res=new String[childrenSet.size()+1];
				   		        	res[0]=selectedSetURI;
				   		        	Iterator<TerminologyIndividual> childrenIter=childrenSet.iterator();
				   		        	int i=1;
				   		        	while (childrenIter.hasNext()) {
				   		        		res[i]=childrenIter.next().getURI();
				   		        		i++;
				   		        	}
				   		        	resultList.changeTo(res);
				   		        	
				   		        }
				   		        
				   		        target.add(resContainer);
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
								if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(myLabel))
									myLabel=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(myLabel).getLabel(CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(myLabel).getLastVersion());
									//TODO terrible!!!!! Must be cleaned up a bit
								return new Label("label",myLabel);
							}

			

							
				     
				
				};
			}
		};
		
		
		
		add(registerTree);
		
	}
		
		
		
		
		
		

		 
		
	
	private ITreeProvider<String> createRegRootModel() {
		return new ITreeProvider<String>(){

			public void detach() {
				// TODO Auto-generated method stub
				
			}

			public Iterator<String> getChildren(String collection) {
				ArrayList<String> resultList=new ArrayList<String>();
				if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(collection)) {
					Set<TerminologySet> children=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(collection).getCollections();
					Iterator<TerminologySet> setIter=children.iterator();
					while(setIter.hasNext()) {
						resultList.add(setIter.next().getURI())
;					}
				}
				return resultList.iterator();
			}

			public Iterator<String> getRoots() {
				TerminologySet[] roots=CommandWebConsole.myInitializer.myFactory.getRootCollections();
				ArrayList<String> rootList=new ArrayList<String>();
				for(int i=0;i<roots.length;i++) {
					rootList.add(roots[i].getURI());
				}
				return rootList.iterator();
			}

			public boolean hasChildren(String collection) {
				if(!CommandWebConsole.myInitializer.myFactory.terminologySetExist(collection)) return false;
				Set<TerminologySet> children=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(collection).getCollections();
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
	String getPageStateMessage() {
		return pageStateMessageString;
	}
	
	private class SearchResultList implements IDataProvider<String> {
		String[] results;
		
		public SearchResultList(String[] results) {
			this.results=results;
		}
		
		void changeTo(String[] results) {
			this.results=results;
		}
		
		public SearchResultList() {
			this.results=new String[0];
		}

		public void detach() {
			// TODO Auto-generated method stub
			
		}

		public Iterator<? extends String> iterator(long offset, long total) {
			ArrayList<String> res=new ArrayList<String>();
			for(long i=offset;i<total;i++) {
				res.add(results[(int) i]); // TODO dangerous. 
			}
			return res.iterator();
		}

		public IModel<String> model(String arg0) {
			return new Model(arg0);
		}

		public long size() {
			return results.length;
		}
		
	}
	
}
