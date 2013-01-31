package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.RegistryAccessException;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class ViewPage extends SuperPage {
	String urlToView="";
	public ViewPage(final PageParameters parameters) throws RegistryAccessException {
		super(parameters);
		urlToView=parameters.get("entity").toString();
		String entityType="Undefined";
		TerminologyEntity entity=null;
		boolean isSet=false;
		boolean isCode=false;
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToView)) {
			entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToView);
			entityType="Set";
			isSet=true;
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToView)) {
			entity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(urlToView);
			entityType="Individual";
			isCode=true;
		}
		
		add(new Label("subjectType",entityType));
		add(new Label("subjectURI",urlToView));
		
		add(new Label("subjectTypes","coming soon..."));
		final FeedbackPanel feedbackPanel=new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);

		
		Form<?> form = new Form<Void>("actionForm"); 
				
		Button editButton=new Button("editButton") {
			@Override
			public void onSubmit() {
				System.out.println("Action:EDIT");
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("entity", urlToView);
				setResponsePage(EditPage.class, pageParameters);
			}
		};
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyUpdateAction.getURI(), urlToView))
			editButton.setEnabled(false);
		else editButton.setEnabled(true);
		// TODO auth here
		
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
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyAddItemAction.getURI(), urlToView))
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
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyAddItemAction.getURI(), urlToView))
				newRegisterButton.setEnabled(false);
			else newRegisterButton.setEnabled(true);
		}
		form.add(newRegisterButton);
		
		final FormComponent<Boolean> obsoleteCheckbox=new CheckBox("obsoleteVerify",new Model<Boolean>(Boolean.FALSE));
		obsoleteCheckbox.setOutputMarkupId(true);
		form.add(obsoleteCheckbox);
		
		/////////////////////////////////////////////////
		// TODO trying the Ajax way for modal panels
		AjaxButton obsoleteButton=new AjaxButton("obsoleteButton") {
			@Override
			public void onSubmit(AjaxRequestTarget target, Form form) {
				// TODO ask for confirmation
				System.out.println("Action:OBSOLETE");
				target.add(feedbackPanel);
				if(!obsoleteCheckbox.getModelObject().booleanValue()) {
					getSession().error("Tick the checkbox!");
					System.out.println("NO to obsolete");
					// Here signal some issue
				}
				else {
					System.out.println("OK to obsolete");
					// TODO insert checkpoint
					
					///////
					/*
					try {
						//TODO
						if(isCode) CommandWebConsole.myInitializer.myTerminologyManager.delTermFromRegister(urlToView, regURI, CommandWebConsole.myInitializer.getDefaultUserURI(), "dumb desrciption");
						if(isSet) CommandWebConsole.myInitializer.myTerminologyManager.delTermFromRegister(urlToView, regURI, CommandWebConsole.myInitializer.getDefaultUserURI(), "dumb description");
					} catch (AuthException e) {
						// TODO Auto-generated catch block
						getSession().error("Auth error");
						e.printStackTrace();
					} catch (RegistryAccessException e) {
						// TODO Auto-generated catch block
						getSession().error("Reg error");
						e.printStackTrace();
					}
					*/
					///////
					
					PageParameters pageParameters = new PageParameters();
					//TODO actually do something
					pageParameters.add("entity", urlToView);
					setResponsePage(ViewPage.class,pageParameters);
				}
			
				//TODO check x error panel report
			}
			
			//ajaxCallListener.onPrecondition("return confirm('are you sure?');");
		};
		
		
		
		
		
	
		// TODO auth here
		if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologyDelItemAction.getURI(), urlToView))
			obsoleteButton.setEnabled(false);
		else obsoleteButton.setEnabled(true);
		form.add(obsoleteButton);
		
		Button supersedButton=new Button("supersedButton"){
			@Override
			public void onSubmit() {
				System.out.println("Action:SUPERSED");
				//PageParameters pageParameters = new PageParameters();
				//pageParameters.add("entity", urlToEdit);
				//setResponsePage(ViewPage.class, pageParameters);
			}
			
		
		};
		//if(isSet) supersedButton.setEnabled(false);
		//else {
			// TODO auth here
			if(!CommandWebConsole.myInitializer.myAuthManager.can(CommandWebConsole.myInitializer.getDefaultUserURI(), MetaLanguage.terminologySupersedAction.getURI(), urlToView))
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
	String getCoreMessage() {
		return "no message";
	}

}
