package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.RegistryAccessException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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
		
		
		Button obsoleteButton=new Button("obsoleteButton") {
			@Override
			public void onSubmit() {
				// TODO ask for confirmation
				System.out.println("Action:OBSOLETE");
				setResponsePage(ViewPage.class);
				//TODO check x error panel report
			}
			
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
