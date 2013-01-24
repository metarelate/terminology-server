package net.metarelate.terminology.webedit;

import net.metarelate.terminology.coreModel.TerminologyEntity;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class ViewPage extends SuperPage {
	String urlToView="";
	public ViewPage(final PageParameters parameters) {
		super(parameters);
		urlToView=parameters.get("entity").toString();
		String entityType="Undefined";
		TerminologyEntity entity=null;
		boolean isSet=false;
		boolean isCode=false;
		if(CommandWebConsole.myFactory.terminologySetExist(urlToView)) {
			entity=CommandWebConsole.myFactory.getOrCreateTerminologySet(urlToView);
			entityType="Set";
			isSet=true;
		}
		if(CommandWebConsole.myFactory.terminologyIndividualExist(urlToView)) {
			entity=CommandWebConsole.myFactory.getOrCreateTerminologyIndividual(urlToView);
			entityType="Individual";
			isCode=true;
		}
		
		add(new Label("subjectType",entityType));
		add(new Label("subjectURI",urlToView));
		
		add(new Label("subjectTypes","coming soon..."));
		
		Button editButton=new Button("editButton");
		// TODO auth here
		add(editButton);
		
		Button newCodeButton=new Button("newCodeButton");
		if(isCode) newCodeButton.setEnabled(false);
		else {
			// TODO auth here
		}
		add(newCodeButton);
		
		Button newRegisterButton=new Button("newRegisterButton");
		if(isCode) newRegisterButton.setEnabled(false);
		else {
			// TODO auth here
		}
		add(newRegisterButton);
		
		Button obsoleteButton=new Button("obsoleteButton");
		// TODO auth here
		add(obsoleteButton);
		
		Button supersedButton=new Button("supersedButton");
		if(isSet) supersedButton.setEnabled(false);
		else {
			// TODO auth here
		}
		add(supersedButton);
		
		Button pullButton=new Button("pullButton");
		if(CommandWebConsole.myInitializer.hasRemote()==false) pullButton.setEnabled(false);
		else {
			// TODO auth here
		}
		add(pullButton);

		Button pushButton=new Button("pushButton");
		if(CommandWebConsole.myInitializer.isRemote()==false) pushButton.setEnabled(false);
		else {
			// TODO auth here
		}
		add(pushButton);
		
		
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
