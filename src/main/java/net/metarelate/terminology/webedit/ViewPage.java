package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ImpossibleOperationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.RegistryAccessException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ViewPage extends SuperPage {
	// TODO when is this used in Wicket ?
	private static final long serialVersionUID = 1951824836857826137L;
	protected String urlToAction=null;		// The url this page shows operations (and details) for.
	protected String urlSuperseder=null;	// (if present) the url of the entity available for superseding
	// TODO we have to wrap this in a session-
	//protected TerminologyEntity entity=null;// The entity related to urlToAction
	protected boolean isSet=false;			// whether urlToAction refers to a set
	protected boolean isCode=false;			// wehther urlToAction refers to an individual
	protected boolean hasSuperseder=false;	// whether a superseder has been provided
	protected String entityType="undefined";// "Set" or "Individual": for displaying TODO consider multi-language
	
	ModalWindow obsoleteConfirmPanelWindow=null;
	ObsoleteConfirmPanel obsoleteConfirmPanelContent=null;
	FeedbackPanel feedbackPanel=null;
	
	LoadableDetachableModel<TerminologyEntity> TerminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
		@Override
		protected TerminologyEntity load() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	protected String pageStateMessage="no message";// A message representing the "state" of the page
	public ViewPage(final PageParameters parameters) throws RegistryAccessException {
		super(parameters);
		urlToAction=parameters.get("entity").toString();
		urlSuperseder=parameters.get("superseder").toString();
		if(urlSuperseder!=null) {
			hasSuperseder=true;
			pageStateMessage="You can now supersed this term with: "+urlSuperseder;
		}
	
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			TerminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
				//TerminologyEntity entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToAction);

				@Override
				protected TerminologyEntity load() {
					return CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToAction);
					
				}
			};
			//entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToAction);
			entityType="Set";
			isSet=true;
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
			TerminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
				//TerminologyEntity entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(urlToAction);

				@Override
				protected TerminologyEntity load() {
					return CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(urlToAction);
					
				}
			};
			//entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(urlToAction);
			entityType="Individual";
			isCode=true;
		}
		
		/**********************************************************************
		 * Building Modal Windows we may use.
		 * The confirmPanel and its container window are resources of this page,
		 * independent from the specific entity that the page refers to.
		 * 
		 **********************************************************************/
	     
		obsoleteConfirmPanelWindow=new ModalWindow("confirmPanel");
	    obsoleteConfirmPanelContent=new ObsoleteConfirmPanel(obsoleteConfirmPanelWindow.getContentId(),this,urlToAction);
	    obsoleteConfirmPanelWindow.setContent(obsoleteConfirmPanelContent);
	    obsoleteConfirmPanelWindow.setInitialHeight(200);
	    obsoleteConfirmPanelWindow.setInitialWidth(400);
	    obsoleteConfirmPanelWindow.setHeightUnit("pixel"); //TODO to verify
	    add(obsoleteConfirmPanelWindow);
	    
		final ModalWindow supersedPanelWindow=new ModalWindow("supersedPanel");
	    final SupersedPanel supersedPanelContent=new SupersedPanel(supersedPanelWindow.getContentId(),supersedPanelWindow);
	    supersedPanelWindow.setContent(supersedPanelContent);
	    supersedPanelWindow.setInitialHeight(200);
	    supersedPanelWindow.setInitialWidth(400);
	    supersedPanelWindow.setHeightUnit("pixel"); //TODO to verify
	    add(supersedPanelWindow);
	       
	      
	    /******************************************************************
	     * CONSTRUCTION OF VIEW DISPLAY    
	     ******************************************************************/
		
		add(new Label("subjectType",entityType));
		add(new Label("subjectURI",urlToAction));
		add(new Label("subjectTypes","coming soon...")); // TODO to extract types and show them here
		feedbackPanel=new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);

	    /******************************************************************
	     * CONSTRUCTION OF VIEW DISPLAY: ENTITY DETAILS and HISTORY 
	     ******************************************************************/
		
		// TODO Here we should list, for each versions, all details... most recent to oldest
		
		
	    /******************************************************************
	     * ACTION FORM AND ACTIONS EXECUTION   
	     ******************************************************************/
		
		Form<?> form = new Form<Void>("actionForm"); 
			
	    /******************************************************************
	     * EDIT ACTION   
	     ******************************************************************/
		Button editButton=new Button("editButton") {
			private static final long serialVersionUID = 5972112410556091326L;

			@Override
			public void onSubmit() {
				// Here we route to the edit page (action will be there, on submit)
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("entity", urlToAction);
				setResponsePage(EditPage.class, pageParameters);
			}
		};
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyUpdateAction.getURI(), urlToAction))
			editButton.setEnabled(false);
		else editButton.setEnabled(true);
		form.add(editButton);
		
		
		
		Button newCodeButton=new Button("newCodeButton") {
			@Override
			public void onSubmit() {
				System.out.println("Action:NEWCODE");
				//PageParameters pageParameters = new PageParameters();
				//pageParameters.add("entity", urlToEdit);
				//setResponsePage(ViewPage.class, pageParameters);
			}
			
		};
		if(isCode) newCodeButton.setEnabled(false);
		else {
			// TODO auth here
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyAddItemAction.getURI(), urlToAction))
				newCodeButton.setEnabled(false);
			else newCodeButton.setEnabled(true);
		}
		form.add(newCodeButton);
		
		Button newRegisterButton=new Button("newRegisterButton"){
			@Override
			public void onSubmit() {
				System.out.println("Action:NEWREGISTER");
				//PageParameters pageParameters = new PageParameters();
				//pageParameters.add("entity", urlToEdit);
				//setResponsePage(ViewPage.class, pageParameters);
			}
		};
		if(isCode) newRegisterButton.setEnabled(false);
		else {
			// TODO auth here
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyAddItemAction.getURI(), urlToAction))
				newRegisterButton.setEnabled(false);
			else newRegisterButton.setEnabled(true);
		}
		form.add(newRegisterButton);
		

		
		
		/********************************************************************
		 * OBSOLETE Action Controls @see proceedObsolete for actual execution
		 * We open a modal panel to confirm the user wants to proceed
		 ********************************************************************/
		AjaxButton obsoleteButton=new AjaxButton("obsoleteButton") {
			private static final long serialVersionUID = 7284165411845513293L;

			@Override
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(feedbackPanel);	// TODO clarify the role of target
				System.out.println("onSubmit-ShowPanel");
				obsoleteConfirmPanelWindow.show(target);
				System.out.println("onSubmit-PostShowPanel");
				/*
				System.out.println(1);
				if(obsoleteConfirmPanelContent.getValueOnce()==false) {
					System.out.println(2);
					getSession().warn("Obsolete action abandoned");	// TODO multilingual support
				}
				else {
					
					System.out.println(3);
					String description="No description available now"; // TODO add support to get description
					try {
						CommandWebConsole.myInitializer.myTerminologyManager.delTerm(
								urlToAction, 
								CommandWebConsole.myInitializer.getDefaultUserURI() , 
								description);
					} catch (ModelException e) {
						getSession().error("Impossible to obsolete: "+urlToAction+" because of internal inconsistencies in the terminology model");
						if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
						e.printStackTrace(); // TODO route to logger
					} catch (RegistryAccessException e) {
						getSession().error("Impossible to obsolete: "+urlToAction+" (access denied)");
						if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
						e.printStackTrace(); // TODO route to logger
					} catch (ImpossibleOperationException e) {
						getSession().error("Impossible to obsolete: "+urlToAction+" (operation not possible)");
						if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
						e.printStackTrace(); // TODO route to logger
					}
					
					PageParameters pageParameters = new PageParameters();
					pageParameters.add("entity", urlToAction);
					setResponsePage(ViewPage.class,pageParameters);
					
				}*/
				
				
			}
			
		};
		
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyDelItemAction.getURI(), urlToAction))
			obsoleteButton.setEnabled(false);
		else obsoleteButton.setEnabled(true);
		form.add(obsoleteButton);
	
		/********************************************************************
		 * SUPERSED Action
		 * If no superseding term is selected, we redirect to a search or
		 * add page (with proper settings).
		 * If a superseding term is selected, we just proceed with the action.
		 * Clues are provided to inform the user that a superseding term
		 * is selected.
		 ********************************************************************/
		AjaxButton supersedButton=new AjaxButton("supersedButton"){
			@Override
			public void onSubmit(AjaxRequestTarget target,Form form) {
				System.out.println("Action:SUPERSED");
				target.add(feedbackPanel);	// TODO clarify the role of target
				
				if(!hasSuperseder) {
					supersedPanelWindow.show(target);
					if(supersedPanelContent.getValueOnce()==false) {
						System.out.println("Do search");
						// We send to search, but we must know what to do next
						PageParameters pageParameters = new PageParameters();
						pageParameters.add("superseding", urlToAction);
						setResponsePage(SearchPage.class,pageParameters);
					}
					else {
						System.out.println("Do new");
						//Set<TerminologyEntity> cotainers=entity.getContainers(entity.getLastVersion());
						PageParameters pageParameters = new PageParameters();
						pageParameters.add("superseding", urlToAction);
						//pageParameters.add("container", );
						setResponsePage(NewPage.class,pageParameters);
						// We send to add, but must know what to do next
					}
				}
				else {
					// TODO issue the supersed operation
				}
			}
			
		
		};
		//if(isSet) supersedButton.setEnabled(false);
		//else {
			// TODO auth here
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologySupersedAction.getURI(), urlToAction))
				supersedButton.setEnabled(false);
			else supersedButton.setEnabled(true);
		//}
		form.add(supersedButton);
		
		Button pullButton=new Button("pullButton");
		if(CommandWebConsole.myInitializer.hasRemote()==false) pullButton.setEnabled(false);
		else {
			// TODO auth here
		}
		form.add(pullButton);

		Button pushButton=new Button("pushButton");
		if(CommandWebConsole.myInitializer.isRemote()==false) pushButton.setEnabled(false);
		else {
			// TODO auth here
		}
		form.add(pushButton);
		
		add(form);
	}
	
	@Override
	String getSubPage() {
		return "Entity details and operations";
	}

	@Override
	String getPageStateMessage() {
		return pageStateMessage;
	}

	public void proceedObsolete(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		System.out.println("proceedObsolete");
		String description="No description available now"; // TODO add support to get description
		try {
			CommandWebConsole.myInitializer.myTerminologyManager.delTerm(
					urlToAction, 
					CommandWebConsole.myInitializer.getDefaultUserURI() , 
					description);
		} catch (ModelException e) {
			getSession().error("Impossible to obsolete: "+urlToAction+" because of internal inconsistencies in the terminology model");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		} catch (RegistryAccessException e) {
			getSession().error("Impossible to obsolete: "+urlToAction+" (access denied)");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		} catch (ImpossibleOperationException e) {
			getSession().error("Impossible to obsolete: "+urlToAction+" (operation not possible)");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		}
		//obsoleteConfirmPanelWindow.close(target);
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("entity", urlToAction);
		setResponsePage(ViewPage.class,pageParameters);
	}

	public void abandonCommand() {
		getSession().warn("Operation abandoned");
		
	}

}
