package net.metarelate.terminology.webedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ImpossibleOperationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.management.RegistryPolicyManager;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.utils.StatementsOrganizer;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.Statement;

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
	GenericConfirmPanel obsoleteConfirmPanelContent=null;
	
	ModalWindow supersedPanelWindow=null;
    SupersedPanel1 supersedPanelContent1=null;
    SupersedPanel2 supersedPanelContent2=null;
   	
	FeedbackPanel feedbackPanel=null;
	
	LoadableDetachableModel<TerminologyEntity> TerminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
		@Override
		protected TerminologyEntity load() {
			// TODO Auto-generated method stub
			return null;
		}
		
	};
	
	protected String pageStateMessage="no message";// A message representing the "state" of the page
	public ViewPage(final PageParameters parameters) throws RegistryAccessException, ModelException {
		super(parameters);
		urlToAction=parameters.get("entity").toString();
		urlSuperseder=parameters.get("superseder").toString();
		if(urlSuperseder!=null) {
			hasSuperseder=true;
			pageStateMessage="You can now supersed this term with: "+urlSuperseder;
		}
		postConstructionFinalize();
		
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			TerminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
				@Override
				protected TerminologyEntity load() {
					return CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction);
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
					return CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction);
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
	    obsoleteConfirmPanelContent=new GenericConfirmPanel(obsoleteConfirmPanelWindow.getContentId(),this,urlToAction);
	    obsoleteConfirmPanelWindow.setContent(obsoleteConfirmPanelContent);
	    obsoleteConfirmPanelWindow.setInitialHeight(200);
	    obsoleteConfirmPanelWindow.setInitialWidth(400);
	    obsoleteConfirmPanelWindow.setHeightUnit("pixel"); //TODO to verify
	    add(obsoleteConfirmPanelWindow);
	    
	    if(!hasSuperseder) {
	    	supersedPanelWindow=new ModalWindow("supersedPanel");
	    	supersedPanelContent1=new SupersedPanel1(supersedPanelWindow.getContentId(),this);
	    	supersedPanelWindow.setContent(supersedPanelContent1);
	    	supersedPanelWindow.setInitialHeight(200);
	    	supersedPanelWindow.setInitialWidth(400);
	    	supersedPanelWindow.setHeightUnit("pixel"); //TODO to verify
	    }
	    else {
	    	supersedPanelWindow=new ModalWindow("supersedPanel");
	    	supersedPanelContent2=new SupersedPanel2(supersedPanelWindow.getContentId(),this,urlToAction);
	    	supersedPanelWindow.setContent(supersedPanelContent2);
	    	supersedPanelWindow.setInitialHeight(200);
	    	supersedPanelWindow.setInitialWidth(400);
	    	supersedPanelWindow.setHeightUnit("pixel"); //TODO to verify
	    }
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
		String lastVersion=TerminologyEntityWrapper.getObject().getLastVersion();
		String[] versions=TerminologyEntityWrapper.getObject().getVersionsChainFor(lastVersion);
		List<String> versionsList=Arrays.asList(versions);
		
		ListView<String> entityVersionsDetailsView = new ListView<String>("versionList", versionsList) {
		    protected void populateItem(ListItem<String> item) {
		    	WebMarkupContainer versionContainer=new WebMarkupContainer("versionContainer");
		    	String currentVersion=item.getModelObject();
		    	Label versionLabel=new Label("versionNumber",currentVersion);
		    	Label versionState=new Label("versionState",TerminologyEntityWrapper.getObject().getStateURI(currentVersion));
		    	Label versionDate=new Label("versionDate",TerminologyEntityWrapper.getObject().getActionDate(currentVersion)); 
		    	Label versionAction=new Label(
		    			"versionAction",
		    			CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(
		    					TerminologyEntityWrapper.getObject().getActionURI(currentVersion),
		    					LabelManager.URI_IF_NULL)
		    			);
		    	Label versionAuthor=new Label(
		    			"versionAuthor",
		    			CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(
		    			TerminologyEntityWrapper.getObject().getActionAuthorURI(currentVersion),
		    			LabelManager.URI_IF_NULL)
		    			);
		    	Label versionDescription=new Label("versionDescription",TerminologyEntityWrapper.getObject().getActionDescription(currentVersion));
		    	versionContainer.add(versionLabel);
		    	versionContainer.add(versionState);
		    	versionContainer.add(versionDate);
		    	versionContainer.add(versionAction);
		    	versionContainer.add(versionAuthor);
		    	versionContainer.add(versionDescription);
		    	
		    	
		    	//StatementsOrganizer statsOrg=new StatementsOrganizer(CommandWebConsole.myInitializer);
		    	
		    	LoadableDetachableModel<ArrayList<Statement>> statementsListWrapper=new StatementsListWrapper(currentVersion);
		    	
		    	/*
		    	ArrayList<Statement> statements=statsOrg.orderStatements(statsOrg.filterModelForWeb(TerminologyEntityWrapper.getObject().getStatements(currentVersion)));
		    	System.out.println("Pre size: "+TerminologyEntityWrapper.getObject().getStatements(currentVersion).size());
		    	System.out.println("After filter: "+statsOrg.filterModelForWeb(TerminologyEntityWrapper.getObject().getStatements(currentVersion)).size());

		    	System.out.println("List size: "+statements.size());
		    	*/
		    	
		    	
		    	ListView<Statement> statementsView = new ListView<Statement>("statsContainer", statementsListWrapper) {
					private static final long serialVersionUID = 7349972737999142555L;

					protected void populateItem(ListItem<Statement> innerItem) {
				    	Label propLabel=new Label(
				    			"property",
				    			CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(
						    			innerItem.getModelObject().getPredicate().getURI(),
						    			LabelManager.URI_IF_NULL)	
				    			);
				    	Label valueLabel=null;
				    	if(innerItem.getModelObject().getObject().isResource()) {
				    		valueLabel=new Label("value",
				    				CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(
							    			innerItem.getModelObject().getObject().asResource().getURI(),
							    			LabelManager.URI_IF_NULL)
				    				);
				    	}
				    	else if(innerItem.getModelObject().getObject().isLiteral()) {
				    		valueLabel=new Label("value",innerItem.getModelObject().getObject().asLiteral().getValue().toString());
				    	}
				    	else {
				    		valueLabel=new Label("value",innerItem.getModelObject().getObject().toString());
				    	}
				    	innerItem.add(propLabel);
				    	innerItem.add(valueLabel);
				    }
		    	};
		    	versionContainer.add(statementsView);
		    	item.add(versionContainer);
		    }
		};
		add(entityVersionsDetailsView);
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
		boolean editActive=true;
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), RegistryPolicyManager.actionUpdateURI, urlToAction))
			editActive=false;
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnCode(
					RegistryPolicyManager.actionUpdateURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				editActive=false;	
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnReg(
					RegistryPolicyManager.actionUpdateURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				editActive=false;	
		}
		
		
		editButton.setEnabled(editActive);
		form.add(editButton);
		
		
		
	    /******************************************************************
	     * NEW CODE ACTION   
	     ******************************************************************/
		Button newCodeButton=new Button("newCodeButton") {
			@Override
			public void onSubmit() {
				Loggers.webAdminLogger.debug("Action:NEWCODE");
				/////
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("type","Individual");
				
				// TODO there must be one, and we take one just at random... if more than one is present, this bit of code is inconsistent (as the rest of the interface)
				//String container=TerminologyEntityWrapper.getObject().getContainers(TerminologyEntityWrapper.getObject().getLastVersion()).iterator().next().getURI();
				pageParameters.add("container", urlToAction);
				setResponsePage(NewPage.class,pageParameters);
				////
				//PageParameters pageParameters = new PageParameters();
				//pageParameters.add("entity", urlToEdit);
				//setResponsePage(ViewPage.class, pageParameters);
			}
			
		};
		
		boolean newCodeEnabled=true;
		if(isCode) newCodeEnabled=false;
		
		
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), RegistryPolicyManager.actionAddURI, urlToAction))
				newCodeEnabled=false;
	
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnReg(
					RegistryPolicyManager.actionAddURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				newCodeEnabled=false;	
		}	
		
		
		newCodeButton.setEnabled(newCodeEnabled);
		form.add(newCodeButton);
		
	    /******************************************************************
	     * NEW REGISTER ACTION   
	     ******************************************************************/
		Button newRegisterButton=new Button("newRegisterButton"){
			@Override
			public void onSubmit() {
				Loggers.webAdminLogger.debug("Action:NEWREGISTER");
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("type","Set");
				
				// TODO there must be one, and we take one just at random... if more than one is present, this bit of code is inconsistent (as the rest of the interface)
				//String container=TerminologyEntityWrapper.getObject().getContainers(TerminologyEntityWrapper.getObject().getLastVersion()).iterator().next().getURI();
				pageParameters.add("container", urlToAction);
				setResponsePage(NewPage.class,pageParameters);
			}
		};
		boolean newRegisterEnabled=true;
		if(isCode) newRegisterEnabled=false;
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), RegistryPolicyManager.actionAddURI, urlToAction))
				newRegisterEnabled=false;
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnReg(
					RegistryPolicyManager.actionAddURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				newRegisterEnabled=false;	
		}	
		
		newRegisterButton.setEnabled(newRegisterEnabled);
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
				obsoleteConfirmPanelContent.setMessage("Do you realy want to obsolete this entity?");
				obsoleteConfirmPanelContent.setExecutor(new FunctionExecutor(){
					@Override
					public void execute(AjaxRequestTarget target) {
						proceedObsolete(target);
						
					}
					
				});
				obsoleteConfirmPanelWindow.show(target);	
			}
		};
		
		boolean obsoleteEnabled=true;
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), RegistryPolicyManager.actionObsoleteURI, urlToAction))
			obsoleteEnabled=false;
		/////
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnCode(
					RegistryPolicyManager.actionObsoleteURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				obsoleteEnabled=false;	
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
			if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnReg(
					RegistryPolicyManager.actionObsoleteURI, 
					CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getLastVersion()), 
					null, 
					null, 
					null))
				obsoleteEnabled=false;	
		}
		////
		obsoleteButton.setEnabled(obsoleteEnabled);
		form.add(obsoleteButton);
	
		/********************************************************************
		 * SUPERSED Action
		 * If no superseding term is selected, we redirect to a search or
		 * add page (with proper settings).
		 * If a superseding term is selected, we ask for description and 
		 * proceed/abandon
		 ********************************************************************/
		AjaxButton supersedButton=new AjaxButton("supersedButton"){
			private static final long serialVersionUID = 2663696796017307696L;
			@Override
			public void onSubmit(AjaxRequestTarget target,Form form) {
				Loggers.webAdminLogger.debug("Action:SUPERSED");
				target.add(feedbackPanel);	// TODO clarify the role of target
				supersedPanelWindow.show(target);
			}
			
		
		};
		boolean supersedEnabled=true;
		if(isSet) supersedEnabled=false;
		
		
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), RegistryPolicyManager.actionSupersedURI, urlToAction))
			supersedEnabled=false;
		if(hasSuperseder) {
			if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
				if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnCode(
						RegistryPolicyManager.actionSupersedURI, 
						CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getLastVersion()), 
						null,
						null,
						CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlSuperseder).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlSuperseder).getLastVersion()) 
						))
					supersedEnabled=false;
				Loggers.webAdminLogger.debug("Supersed: non viable operation 1");
			}
			else {
				supersedEnabled=false;
				Loggers.webAdminLogger.debug("Supersed: unknown URL 1");
			}
			
		}
		else {
			if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
				if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnCode(
						RegistryPolicyManager.actionSupersedURI, 
						CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getLastVersion()), 
						null,
						null,
						null
						))
					supersedEnabled=false;	
				Loggers.webAdminLogger.debug("Supersed: non viable operation 2");
			}
			else {
				supersedEnabled=false;
				Loggers.webAdminLogger.debug("Supersed: unknown URL 2");
			}
		}
		
		
		
		
		supersedButton.setEnabled(supersedEnabled);
		form.add(supersedButton);
		
		
		/********************************************************************
		 * EXTRA Actions button list
		 ********************************************************************/
		
		String[] extraActions=CommandWebConsole.myInitializer.myRegistryPolicyManager.getExtraActions();
		List<String>actionsList=new ArrayList<String>();
		for(String extraAct:extraActions) actionsList.add(extraAct);
		
		ListView<String> extraButtons = new ListView<String>("extraButtons", actionsList) {

			@Override
			protected void populateItem(ListItem<String> currentAction) {
				final String actionURI=currentAction.getModelObject();
				final String actionLabel=CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(actionURI, LabelManager.LANG_DEF_SHORTURI);
				// TODO Auto-generated method stub
				AjaxButton extraActionButton=new AjaxButton("extraButton"){
					@Override
					public void onSubmit(AjaxRequestTarget target,Form form) {
						Loggers.webAdminLogger.debug("Action: "+actionURI);
						target.add(feedbackPanel);	// TODO clarify the role of target
						obsoleteConfirmPanelContent.setMessage("Do you really want to proceed with action "+actionLabel+" ?");
						obsoleteConfirmPanelContent.setExecutor(new FunctionExecutor(){

							@Override
							public void execute(AjaxRequestTarget target) {
								try {
									CommandWebConsole.myInitializer.myTerminologyManager.performGenericAction(actionURI, urlToAction, CommandWebConsole.myInitializer.getDefaultUserURI(), obsoleteConfirmPanelContent.getDescription());
								} catch (RegistryAccessException e) {
									getSession().error("Impossible to perform action "+actionLabel+" on "+urlToAction+" (access denied)");
									if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
									e.printStackTrace(); // TODO route to logger
								} catch (ImpossibleOperationException e) {
									getSession().error("Impossible to perform action "+actionLabel+" on "+urlToAction+" (operation not possible)");
									if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
									e.printStackTrace(); // TODO route to logger
								} catch (ModelException e) {
									getSession().error("Impossible to perform action "+actionLabel+" on "+urlToAction+" (inconsistent terminology)");
									if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
									e.printStackTrace(); // TODO route to logger
								}
							
								//obsoleteConfirmPanelWindow.close(target);
								PageParameters pageParameters = new PageParameters();
								pageParameters.add("entity", urlToAction);
								setResponsePage(ViewPage.class,pageParameters);
							}
							
						});
						obsoleteConfirmPanelWindow.show(target);
					}
					
				
				};
				boolean isActive=false;
				try {
					if(CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), actionURI, urlToAction))
						isActive=true;
				} catch (RegistryAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				///
				if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToAction)) {
					if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnCode(
							actionURI, 
							CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToAction).getLastVersion()), 
							null, 
							null, 
							null))
						isActive=false;
					
				}
				if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToAction)) {
					if(!CommandWebConsole.myInitializer.myRegistryPolicyManager.isViableOperationOnReg(
							actionURI, 
							CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getStateURI(CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToAction).getLastVersion()), 
							null, 
							null, 
							null))
					isActive=false;
					
				}
				///
				extraActionButton.setEnabled(isActive);
				extraActionButton.setModel(new Model(actionLabel));
				currentAction.add(extraActionButton);
				
				
				///////
				
			}
			
		};
		form.add(extraButtons);
		
		
		
		/********************************************************************
		 * PULL Action
		 * Still to be implemented!
		 ********************************************************************/
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
	/********************************************************************
	 * OBSOLETE Action (this is where the obsolete action is issued)
	 ********************************************************************/
	public void proceedObsolete(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		Loggers.webAdminLogger.debug("proceedObsolete");
		String description=obsoleteConfirmPanelContent.getDescription(); // TODO add support to get description
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
	
	/********************************************************************
	 * SUPERSED Action (this is where the supersed action is issued)
	 ********************************************************************/
	public void supersedRouteToSearch(AjaxRequestTarget target) {
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("superseding", urlToAction);
		setResponsePage(SearchPage.class,pageParameters);
	}
	public void supersedRouteToAdd(AjaxRequestTarget target) throws ModelException {
		PageParameters pageParameters = new PageParameters();
		if(isSet) {
			pageParameters.add("type","Set");
		}
		if(isCode) {
			pageParameters.add("type","Individual");
		}
		// TODO there must be one, and we take one just at random... if more than one is present, this bit of code is inconsistent (as the rest of the interface)
		String container=TerminologyEntityWrapper.getObject().getContainers(TerminologyEntityWrapper.getObject().getLastVersion()).iterator().next().getURI();
		pageParameters.add("container", container);
		pageParameters.add("superseding", urlToAction);
		setResponsePage(NewPage.class,pageParameters);
	}
	
	public void proceedSupersed(AjaxRequestTarget target) {
		Loggers.webAdminLogger.debug("ACTION: supersed");
		String description=supersedPanelContent2.getDescription(); // TODO add support to get description
		try {
			CommandWebConsole.myInitializer.myTerminologyManager.superseedTerm(urlToAction, 
					urlSuperseder,
					CommandWebConsole.myInitializer.getDefaultUserURI(), 
					description);
		} catch (ModelException e) {
			getSession().error("Impossible to supersed: "+urlToAction+" with "+urlSuperseder+" because of internal inconsistencies in the terminology model");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		} catch (RegistryAccessException e) {
			getSession().error("Impossible to supersed: "+urlToAction+" with "+urlSuperseder+" (access denied)");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		} catch (ImpossibleOperationException e) {
			getSession().error("Impossible to supersed: "+urlToAction+" with "+urlSuperseder+" (operation not possible)");
			if(CommandWebConsole.myInitializer.debugMode) getSession().error(e.getMessage());
			e.printStackTrace(); // TODO route to logger
		}
		
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("entity", urlToAction);
		setResponsePage(ViewPage.class,pageParameters);
		/*
		if(!hasSuperseder) {
			supersedPanelWindow.show(target);
			if(supersedPanelContent2.getValueOnce()==false) {
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
		*/
	}
	
	private class StatementsListWrapper extends LoadableDetachableModel<ArrayList<Statement>>{
		private static final long serialVersionUID = 1L;
		private String currentVersion=null;
		
		
		

		public StatementsListWrapper(String currentVersion) {
			super();
			this.currentVersion = currentVersion;
			
		}



		@Override
		protected ArrayList<Statement> load() {
			return StatementsOrganizer.orderStatements(StatementsOrganizer.filterModelForWeb(TerminologyEntityWrapper.getObject().getStatements(currentVersion)), CommandWebConsole.myInitializer);
		}
		
	};

}
