package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.exceptions.PropertyConstraintException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.exceptions.WebSystemException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class NewPage  extends AbstractEditPage {
	private static final long serialVersionUID = 1L;
	//private String uriOfContainer=null;
	private String type=null;
	AjaxyTextArea uriField=null;
	Label uriStatus=null;
	public NewPage(final PageParameters parameters) throws WebSystemException, ConfigurationException, PropertyConstraintException {
		super(parameters);
		//uriOfEntity=parameters.get("entity").toString();
		uriToSupersed=parameters.get("superseding").toString();
		type=parameters.get("type").toString();
		uriOfContainer=parameters.get("container").toString();
		if(uriToSupersed!=null) {
			isSuperseding=true;
			pageMessage="Adding a new entity to supersed "+uriToSupersed;
		}
		else {
			pageMessage="Nothing to say";
		}
		isNew=true;
		if(type.equals("Set")) isSet=true;
		else if(type.equals("Individual")) isIndividual=true;
		else {
			//TODO here we have a fatal error condition
		}
		if(uriOfContainer==null) {
			//TODO here we have another fatal condition
		}
		
		uriField=new AjaxyTextArea("uriField");
		uriField.setText(uriOfContainer+"/");
		uriField.add(new AjaxFormComponentUpdatingBehavior("onkeyup"){
	            protected void onUpdate(AjaxRequestTarget target) { 
	                target.add(uriStatus);
	                target.add(feedbackPanel);
	                if(!validateURI(uriField.getText())) {
	                	//getSession().error("Invalid URI or code already defined");
	                	uriStatus.setDefaultModelObject("Not valid");
	                	System.out.println("URI not valid");
	                }
	                else {
	                	uriStatus.setDefaultModelObject("Valid");
	                	System.out.println("URI is valid");
	                }
	            } 
	        }); 
		
		add(uriField);
		//here we should add a validation behaviour.
		uriStatus=new Label("uriStatus","Not valid");
		uriStatus.setOutputMarkupId(true);
		add(uriStatus);
		
	
		//TODO here is where we start with fields...
		
		buildForm();
		postConstructionFinalize();
		
		//add(new Label("version",myEntity.getLastVersion()));

		
    }
	
	@Override
	protected void buildEntity(Model statementsCollected,String description) throws WebSystemException {
		String uri=uriField.getText();
		if(!validateURI(uri)) {
			getSession().error(uri+" is not a valid uri"); //TODO to move up to Abstract
			throw new WebSystemException("cannot build entity with invalid uri");
		}
		try {
			if(isSet) {
				CommandWebConsole.myInitializer.myTerminologyManager.addSubRegister(
						getURIOfEntity(), 
						uriOfContainer, 
						statementsCollected,
						CommandWebConsole.myInitializer.getDefaultUserURI(), 
						description, 
						true);
			}
			else {
			
				CommandWebConsole.myInitializer.myTerminologyManager.addTermToRegister(
					getURIOfEntity(), 
					uriOfContainer, 
					statementsCollected,
					CommandWebConsole.myInitializer.getDefaultUserURI(), 
					description, 
					true);
			}
		} catch (AuthException e) {
				// TODO Auto-generated catch block
				getSession().error("Auth error");
				e.printStackTrace();
		} catch (RegistryAccessException e) {
				// TODO Auto-generated catch block
				getSession().error("Reg error");
				e.printStackTrace();
		} catch (InvalidProcessException e) {
			// TODO Auto-generated catch block
			getSession().error("Process error");
			e.printStackTrace();
		}
		

	}
	
	private boolean validateURI(String uri) {
		if(uri.length()<8) {
			return false;
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(uri)) {
			return false;
		}
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(uri)) {
			return false;
		}
		if(uri.endsWith("/")) {
			if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(uri.substring(0,uri.length()-1))) {
				return false;
			}
			if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(uri.substring(0,uri.length()-1))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	String getSubPage() {
		return "New Term";
	}
	@Override
	String getPageStateMessage() {
		return pageMessage;
	}
	
	//TODO maybe we want a text field here
	public class AjaxyTextArea extends TextArea {
		private static final long serialVersionUID = 1L;
		private String text; 
		
		public AjaxyTextArea(String id) { 
		        super(id); 
		        setModel(new PropertyModel(this, "text")); 
		        add(new AjaxFormComponentUpdatingBehavior("onchange"){ 

		            protected void onUpdate(AjaxRequestTarget target) { 
		                System.out.println("text: " + text); 
		            } 
		        }); 
		    } 

		public String getText(){ return text; } 

		public void setText(String text) { this.text = text; } 
	}

	@Override
	protected String getURIOfEntity() {
		String result=uriField.getText();
		System.out.println("URI of entity: "+result);
		//if(result!=null)
		//	if(validateURI(result)) return result;
		return result;
		
	} 
	
}



