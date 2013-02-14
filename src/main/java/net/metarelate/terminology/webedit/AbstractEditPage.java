package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.exceptions.WebSystemException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Common behaviour to edit and new operations. These two actions are largely
 * equivalent, except for the definition of new URIs.
 * @author andreasplendiani
 *
 */
public abstract class AbstractEditPage  extends SuperPage {
	private static final long serialVersionUID = 10L;
	//protected String uriOfEntity=null;
	protected String uriToSupersed=null;
	protected boolean isNew=false;
	protected boolean isEdit=false;
	protected boolean isSet=false;
	protected boolean isIndividual=false;
	protected boolean isSuperseding=false;
	protected TerminologyEntity myEntity=null;
	protected String pageMessage="";
	protected FeedbackPanel feedbackPanel=null;
	
	public AbstractEditPage(final PageParameters parameters) {
		super(parameters);
    }
	
	protected abstract String getURIOfEntity();
	
	protected abstract void buildEntity(Model statementsCollected,String description) throws WebSystemException;
	
	protected void buildForm() throws WebSystemException {
		
		// TODO what below should be generalized to all statements present (or required for a new term/set).
		org.apache.wicket.model.Model<String> labelModel=null;
		if(isEdit) labelModel=org.apache.wicket.model.Model.of(myEntity.getLabel(myEntity.getLastVersion()));
		else if(isNew) labelModel=new org.apache.wicket.model.Model<String>();
		else throw new WebSystemException("Neither edit or new action for edit form");
		final TextField<String> entityLabel = new TextField<String>("entityLabel", labelModel);
		
		entityLabel.setRequired(true);
		entityLabel.add(new LabelValidator()); //TODO here is where we extend validation
		
		Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onSubmit() {
				//NOTE: this method is executed only upon successful validation. We rely on this!
				System.out.println("Page is valid");
				String labelValue = entityLabel.getModel().getObject();
				if(labelValue==null) labelValue=""; // TODO check that things work here
				Statement newStatement=ResourceFactory.createStatement(ResourceFactory.createResource(getURIOfEntity()), MetaLanguage.labelProperty, ResourceFactory.createPlainLiteral(labelValue));
				Model newStats=ModelFactory.createDefaultModel().add(newStatement);
				System.out.println("Model collected: "+newStats.size()+" statements");
				
				try {
					buildEntity(newStats,"bogud description");
				} catch (WebSystemException e) {
					getSession().error("Impossible to initialize entity");
					return;
					// TODO do nothing and return
					//PageParameters pageParameters = new PageParameters();
					//pageParameters.add("entity", getURIOfEntity());
					//setResponsePage(ViewPage.class, pageParameters);
					//e.printStackTrace();
				} // TODO implement real one
				
				/**
				 * What should happen here.
				 * 1) check validation
				 * 2) prepare metadata
				 * 3) if this is a New Page:
				 *  	a) create the entity			(Possibly in TerminologyWebConstructor and should move to TerminologyManager)
				 *      b) register entity in container (Possibly in TerminologyWebConstructor and should move to TerminologyManager)
				 * 		c) regsiter values				(Possibly in TerminologyWebConstructor and should move to TerminologyManager)
				 * 4) if this is an Edit page: issue update
				 */
				
				/*
				try {
					//Do nothing now, but we should get all statements and replace the model
					CommandWebConsole.myInitializer.myTerminologyManager.replaceEntityInformation(uriOfEntity, newStats, CommandWebConsole.myInitializer.getDefaultUserURI(), "dumb edit");
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
				if(isSuperseding) {
					//route to to viewPage (target=superseding)
					System.out.println("Superseding");
					System.out.println("e: "+getURIOfEntity());
					System.out.println("s: "+uriToSupersed);
					PageParameters pageParameters = new PageParameters();
					pageParameters.add("entity", getURIOfEntity());
					pageParameters.add("superseding",uriToSupersed);
					setResponsePage(ViewPage.class, pageParameters);
				}
				else {
					//route to viewPage (target=urlOfEntity)
					PageParameters pageParameters = new PageParameters();
					pageParameters.add("entity", getURIOfEntity());
					setResponsePage(ViewPage.class, pageParameters);
				}
				
				
				// update label with labelValue
				
				

			}

		};
		//add(new Label("version",myEntity.getLastVersion()));
		add(form);
		form.add(entityLabel);
		feedbackPanel=new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
	}
	

}



