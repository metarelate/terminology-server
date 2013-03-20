package net.metarelate.terminology.webedit;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ImporterException;
import net.metarelate.terminology.exceptions.InvalidProcessException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.PropertyConstraintException;
import net.metarelate.terminology.exceptions.RegistryAccessException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.webedit.validators.DaftValidator;
import net.metarelate.terminology.webedit.validators.InRegisterValidator;
import net.metarelate.terminology.webedit.validators.IsNumericValidator;
import net.metarelate.terminology.webedit.validators.IsURIValidator;
import net.metarelate.terminology.webedit.validators.MaxCardinalityValidator;
import net.metarelate.terminology.webedit.validators.MinCardinalityValidator;
import net.metarelate.terminology.webedit.validators.OptionValidator;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
	//protected TerminologyEntity myEntity=null;
	LoadableDetachableModel<TerminologyEntity> terminologyEntityWrapper=null;
	LoadableDetachableModel<Model> extraStatements=null;
	protected String pageMessage="";
	protected FeedbackPanel feedbackPanel=null;
	protected String uriOfContainer=null;
	
	AjaxyTextArea actionDescription=null;
	
	public AbstractEditPage(final PageParameters parameters) {
		super(parameters);
    }
	
	
	
	protected abstract String getURIOfEntity();
	protected abstract boolean isURIValid();
	
	public String getDescription() {
		if(actionDescription!=null) {
			return actionDescription.getText();
		}
		else return "";
	}
	
	//protected abstract void buildEntity(Model statementsCollected,String description) throws WebSystemException;
	

	/**
	 * Here we build the form.
	 * @throws WebSystemException
	 * @throws ConfigurationException
	 * @throws PropertyConstraintException 
	 * @throws UnknownURIException 
	 * @throws ModelException 
	 */
	protected void buildForm() throws WebSystemException, ConfigurationException, PropertyConstraintException, UnknownURIException, ModelException {
		/*
		 * If this is an edit action, the TerminologyEntityModel is initialized, and it will be used in this page.
		 * If this is a new action, it will be created on submit
		 */
	
		SSLogger.log("Building plan for form",SSLogger.DEBUG);
		/*
		 * First we ask which constraints apply.
		 */
		String[] constraints=null;
		if(isNew) {
			SSLogger.log("New mode",SSLogger.DEBUG);
			if(isSet) constraints=CommandWebConsole.myInitializer.myConstraintsManager.getSortedConstraintsForNewReg(uriOfContainer);
			if(isIndividual) constraints=CommandWebConsole.myInitializer.myConstraintsManager.getSortedConstraintsForNewCode(uriOfContainer);
			SSLogger.log("Commputed",SSLogger.DEBUG);
		}
		else if(isEdit) {
			SSLogger.log("Edit mode",SSLogger.DEBUG);
			if(isSet) constraints=CommandWebConsole.myInitializer.myConstraintsManager.getSortedConstraintsForNewReg(terminologyEntityWrapper.getObject().getURI());
			if(isIndividual) constraints=CommandWebConsole.myInitializer.myConstraintsManager.getSortedConstraintsForNewCode(terminologyEntityWrapper.getObject().getURI());
		}
		else {} //TODO Something would be wrong if we end up here
		SSLogger.log("Found:",SSLogger.DEBUG);
		for (String cons:constraints) SSLogger.log(cons,SSLogger.DEBUG);
		
		/*
		 * We make a copy of statements already known that should be edited.
		 * We will use previusy known statements to make fields even if constraints don't propose them.
		 * 
		 */
		com.hp.hpl.jena.rdf.model.Model bagOfStatements=ModelFactory.createDefaultModel();	//Note: hopefully this is serializable or wicket will complain
		if(isEdit) bagOfStatements.add(MetaLanguage.filterForEdit(terminologyEntityWrapper.getObject().getStatements(terminologyEntityWrapper.getObject().getLastVersion())));
		SSLogger.log("Previous statements: "+bagOfStatements.size(), SSLogger.DEBUG);
		
		
		extraStatements=new LoadableDetachableModel<Model>() {
			@Override
			protected Model load() {
				Model result=ModelFactory.createDefaultModel();
				if(isEdit) result.add(MetaLanguage.filterForEditComplement(terminologyEntityWrapper.getObject().getStatements(terminologyEntityWrapper.getObject().getLastVersion())));
				//TODO note that the wrapper is null for non edit
				return result;
			}
			
		};
		
		
		/*
		 * Here we assemble a list of things that will be used to build the form.
		 */
		final ArrayList<FormObject> formObjects=new ArrayList<FormObject>();
		final ArrayList<DaftValidator> validators=new ArrayList<DaftValidator>();
		/*
		 * First we start from constraints.
		 */
		for(String cons:constraints) {
			//FormComponent currentFormItem=null;
			SSLogger.log("Starting analysis for constraint: "+cons,SSLogger.DEBUG);
			final String property=CommandWebConsole.myInitializer.myConstraintsManager.getPropertyForConstraint(cons);
			String language=CommandWebConsole.myInitializer.myConstraintsManager.getForConstraintLanguage(cons);
			int minCardinality=CommandWebConsole.myInitializer.myConstraintsManager.getMinCardinalityForConstr(cons);
			int maxCardinality=CommandWebConsole.myInitializer.myConstraintsManager.getMaxCardinalityForConstr(cons);
			boolean isNumeric=CommandWebConsole.myInitializer.myConstraintsManager.isNumeric(cons);
			String[] options=CommandWebConsole.myInitializer.myConstraintsManager.getOptionsForConstraints(cons);
			boolean onData=CommandWebConsole.myInitializer.myConstraintsManager.isOnDataProperty(cons);
			boolean onObject=CommandWebConsole.myInitializer.myConstraintsManager.isOnObjectProperty(cons);
			boolean inRegister=CommandWebConsole.myInitializer.myConstraintsManager.isInRegisterForConstr(cons);
			//System.out.println("Language: "+language);
			/**
			 * We build and record the corresponding form validator
			 */
			if(minCardinality>0) validators.add(new MinCardinalityValidator(property,minCardinality,language));
			if(maxCardinality>0) validators.add(new MaxCardinalityValidator(property,maxCardinality,language));
			if(isNumeric) validators.add(new IsNumericValidator(property));
			if(onObject) validators.add(new IsURIValidator(property));
			if(options!=null) validators.add(new OptionValidator(property,options));
			if(inRegister) validators.add(new InRegisterValidator(property));
			
			/*
			 * How many form objects for this property ?
			 */
			int nOfElements=0;
			if(minCardinality>-1) nOfElements=minCardinality; //Never less than the minimum
			if(maxCardinality>nOfElements) nOfElements=maxCardinality; //making some space if more are required.
			//Now checking what we have in the model (unless it's edit, it should be empty)
			int previouslyKnown=0;
			if(language==null) previouslyKnown=bagOfStatements.listObjectsOfProperty(ResourceFactory.createProperty(property)).toSet().size();
			else {
				NodeIterator objects=bagOfStatements.listObjectsOfProperty(ResourceFactory.createProperty(property));
				while(objects.hasNext()) {
					RDFNode object=objects.next();
					if(object.isLiteral())
						if(object.asLiteral().getLanguage()!=null)
							if(language.equals(object.asLiteral().getLanguage()))
								previouslyKnown++;
				}
			}
			if(previouslyKnown>nOfElements) nOfElements=previouslyKnown; //we don't drop statements
			if(nOfElements==0) nOfElements=1; //if it'smention, we are going to propose one field.
			SSLogger.log("Min.: "+minCardinality,SSLogger.DEBUG);
			SSLogger.log("Max.: "+maxCardinality,SSLogger.DEBUG);
			SSLogger.log("Prev.: "+previouslyKnown,SSLogger.DEBUG);
			SSLogger.log("For "+property+" we are going to have "+nOfElements+" form elements",SSLogger.DEBUG);
			
			/*
			 * New we need to build these form objects
			 */
			FormObject[] formsObjectsBlock=new FormObject[nOfElements];
			for(int i=0;i<formsObjectsBlock.length;i++) {
				formsObjectsBlock[i]=new FormObject();
			}
		
			/*
			 * Generic initialization
			 */
			for(FormObject f:formsObjectsBlock) {
				f.property=property;
				f.isNumeric=isNumeric;
				f.isURI=onObject;
				f.options=options;
				f.language=language;
			}
			/*
			 * New we set the value (only really happens with edit)
			 */
			
			//Note: take care of language!
			com.hp.hpl.jena.rdf.model.Model statementsForProperty=ModelFactory.createDefaultModel();
			SSLogger.log("Inspecting known statements ",SSLogger.DEBUG);
			int i=0;
			if(onObject) {
				SSLogger.log("Looking for URIs",SSLogger.DEBUG);
				StmtIterator preMatchingStatements=bagOfStatements.listStatements(null,
						ResourceFactory.createProperty(property),
						(RDFNode)null);
				i=0;
				while(preMatchingStatements.hasNext()) {
					Statement currentStat=preMatchingStatements.nextStatement();
					if(currentStat.getObject().isURIResource()) {
						statementsForProperty.add(currentStat);
						formsObjectsBlock[i].value=org.apache.wicket.model.Model.of(currentStat.getObject().asResource().getURI());
						SSLogger.log("URI value set to "+formsObjectsBlock[i].value+" for element "+i,SSLogger.DEBUG);
						i++;
					}
				}
					
			}
			else if(language!=null) {
				SSLogger.log("Looking for language literals",SSLogger.DEBUG);
				StmtIterator preMatchingStatements=bagOfStatements.listStatements(null,
						ResourceFactory.createProperty(property),
						(RDFNode)null);
				i=0;
				while(preMatchingStatements.hasNext()) {
					Statement currentStat=preMatchingStatements.nextStatement();
					if(currentStat.getObject().isLiteral()) 
						if(currentStat.getObject().asLiteral().getLanguage()!=null)
							if(currentStat.getObject().asLiteral().getLanguage().equals(language)) {
								statementsForProperty.add(currentStat);
								formsObjectsBlock[i].value=org.apache.wicket.model.Model.of(currentStat.getObject().asLiteral().getValue().toString());
								i++;
							}
								
				}
			}
			else {
				SSLogger.log("Looking for simple literals",SSLogger.DEBUG);
				StmtIterator preMatchingStatements=bagOfStatements.listStatements(null,
						ResourceFactory.createProperty(property),
						(RDFNode)null);
				i=0;
				while(preMatchingStatements.hasNext()) {
					Statement currentStat=preMatchingStatements.nextStatement();
					if(currentStat.getObject().isLiteral()) {
						statementsForProperty.add(currentStat);
						formsObjectsBlock[i].value=org.apache.wicket.model.Model.of(currentStat.getObject().asLiteral().getValue().toString());
						i++;
					}
				}
			}
			 
			bagOfStatements.remove(statementsForProperty);
			SSLogger.log("Previous statements are down to "+bagOfStatements.size(),SSLogger.DEBUG);
			
			
			
			/*
			 * We add the form of objects to our bag to be rendered
			 */
			for(FormObject f:formsObjectsBlock) {
				formObjects.add(f);
			}
			
			
			
		
			
		
			
		}
		
		/**
		 * Creating field for remaining statements... (this should happen only for edit)
		 */
		SSLogger.log("Processing remaining statements ",SSLogger.DEBUG);
		FormObject[] extraKnownStatements=new FormObject[(int)bagOfStatements.size()];
		int i2=0;
		StmtIterator remStats=bagOfStatements.listStatements();
		while(remStats.hasNext()) {
			Statement currentStat=remStats.nextStatement();
			FormObject newObject=new FormObject();
			newObject.property=currentStat.getPredicate().getURI();
			SSLogger.log("Processing "+newObject.property+" - internal counter is "+i2,SSLogger.DEBUG);
			if(currentStat.getObject().isURIResource()) {
				newObject.isURI=true;
				newObject.value=org.apache.wicket.model.Model.of(currentStat.getObject().asResource().getURI());
			}
			if(currentStat.getObject().isLiteral()) {
				if(currentStat.getObject().asLiteral().getLanguage()!=null) {
					newObject.language=currentStat.getObject().asLiteral().getLanguage();
				}
				newObject.value=org.apache.wicket.model.Model.of(currentStat.getObject().asLiteral().getValue().toString());

				
			}
			extraKnownStatements[i2++]=newObject;
			
			
		}
		
		/*
		 * We add the last form objects to our bag to be rendered
		 */
		i2=0;
		for(FormObject f:extraKnownStatements) {
			System.out.println(i2++);
			formObjects.add(f);
		}
		
		
		
		
		
		org.apache.wicket.model.Model<String> labelModel=null;
		if(isEdit) labelModel=org.apache.wicket.model.Model.of(terminologyEntityWrapper.getObject().getLabel(terminologyEntityWrapper.getObject().getLastVersion()));
		else if(isNew) labelModel=new org.apache.wicket.model.Model<String>();
		else throw new WebSystemException("Neither edit or new action for edit form");
		//final TextField<String> entityLabel = new TextField<String>("entityLabel", labelModel);
		
		//entityLabel.setRequired(true);
		//entityLabel.add(new LabelValidator()); //TODO here is where we extend validation
		
		final Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onSubmit() {
				/*
				 * TODO validators should have been called at this point. Can something be interecepted and added here ?
				 */
				//TODO note: we rely on what below!
				SSLogger.log("Page is valid",SSLogger.DEBUG);
				/*
				 * We build the new model 
				 * TODO we could capture exceptions in case of malformed URIs and the like, and route it to the validation
				 * 
				 */
				Model newStatememts=ModelFactory.createDefaultModel();
				for(FormObject f:formObjects) {
					SSLogger.log("Collecting infos for statement with property: "+f.property,SSLogger.DEBUG);
					if(f.value.getObject()!=null) {
						if(f.isURI) {
							String prop=null;
							if(!f.isNew) {
								prop=f.property;
							}
							else{
								try{
									URL test=new URL(f.propValue.getObject());
								} catch (Exception e) {
									getSession().error("Invalid URL for property: "+f.propValue.getObject());
									return;
								}
								prop=f.propValue.getObject();
							}
							
							newStatememts.add(ResourceFactory.createStatement(
									ResourceFactory.createResource(getURIOfEntity()),
									ResourceFactory.createProperty(prop),
									ResourceFactory.createResource(f.value.getObject())
									));
						}
						else {
							if(f.language!=null){
								String prop=null;
								if(!f.isNew) {
									prop=f.property;
								}
								else {
									try{
										URL test=new URL(f.propValue.getObject());
									} catch (Exception e) {
										getSession().error("Invalid URL for property: "+f.propValue.getObject());
										return;
									}
									prop=f.propValue.getObject();
								}
								
								newStatememts.add(ResourceFactory.createStatement(
										ResourceFactory.createResource(getURIOfEntity()),
										ResourceFactory.createProperty(prop),
										newStatememts.createLiteral(f.value.getObject(),f.language)
										//ResourceFactory.createLangLiteral(f.value.getObject(),f.language)
										));
										//TODO note the hack... we miss an operator!!!
							}
							else {
								String prop=null;
								if(!f.isNew) {
									prop=f.property;
								}
								else {
									try{
										URL test=new URL(f.propValue.getObject());
									} catch (Exception e) {
										getSession().error("Invalid URL for property: "+f.propValue.getObject());
										return;
									}
									prop=f.propValue.getObject();
								}
								
							
								
								newStatememts.add(ResourceFactory.createStatement(
									ResourceFactory.createResource(getURIOfEntity()),
									ResourceFactory.createProperty(prop),
									ResourceFactory.createPlainLiteral(f.value.getObject())
									));
							}
						}
					}
					else {} //This was empty...
				}
				
				//String labelValue = entityLabel.getModel().getObject();
				//if(labelValue==null) labelValue=""; // TODO check that things work here
				//Statement newStatement=ResourceFactory.createStatement(ResourceFactory.createResource(getURIOfEntity()), MetaLanguage.labelProperty, ResourceFactory.createPlainLiteral(labelValue));
				//Model newStats=ModelFactory.createDefaultModel().add(newStatement);
				SSLogger.log("Collected model with : "+newStatememts.size()+" statements");
				
				/*
				 * Here we prepare the metadata
				 */
				String urlToEdit=getURIOfEntity();
				String userURI=CommandWebConsole.myInitializer.getDefaultUserURI();
				//String actionDescription="bogus right now"; //TODO we should add the description panel!
				
				
				/*
				 * The actual action!
				 */
				
				/*
				 * First, our "custom validation"...
				 */
				if(!isURIValid()) {
					getSession().error("URI is invalid");
					return;
				}
				
				for(DaftValidator v:validators) {
					if(!v.validate(newStatememts)) {
						getSession().error(v.getMessage());
						return;
					}
				}
				
				try {
					if(isEdit) {
						// Entity exists...
						CommandWebConsole.myInitializer.myTerminologyManager.sobstituteEntityInformation(urlToEdit, newStatememts.add(extraStatements.getObject()), CommandWebConsole.myInitializer.getDefaultUserURI(), getDescription());
					}
					if(isNew) {
						if(isSet) CommandWebConsole.myInitializer.myTerminologyManager.addSubRegister(urlToEdit, uriOfContainer, newStatememts.add(extraStatements.getObject()), userURI, getDescription(), true);
						if(isIndividual) CommandWebConsole.myInitializer.myTerminologyManager.addTermToRegister(urlToEdit, uriOfContainer, newStatememts.add(extraStatements.getObject()), userURI, getDescription(), true);
					}
				} catch (AuthException e) {
					getSession().error("Auth error");
					e.printStackTrace();
				} catch (InvalidProcessException e) {
					getSession().error("Invalid process error");
					e.printStackTrace();
				} catch (RegistryAccessException e) {
					getSession().error("Reg error");
					e.printStackTrace();
				} catch (ModelException e) {
					getSession().error("Model consistency error");
					e.printStackTrace();
				} catch (ImporterException e) {
					getSession().error("Importer error!");
					e.printStackTrace();
				}
				
	
				
				/*
				 * What should happen next
				 */	
				if(isSuperseding) {
					//route to to viewPage (target=superseding)
					System.out.println("Superseding");
					System.out.println("e: "+getURIOfEntity());
					System.out.println("s: "+uriToSupersed);
					PageParameters pageParameters = new PageParameters();
					pageParameters.add("entity", uriToSupersed);
					pageParameters.add("superseder",getURIOfEntity());
					setResponsePage(ViewPage.class, pageParameters);
				}
				else {
					//route to viewPage (target=urlOfEntity)
					PageParameters pageParameters = new PageParameters();
					pageParameters.add("entity", getURIOfEntity());
					setResponsePage(ViewPage.class, pageParameters);
				}
				
				
		

			}

		};
		form.setOutputMarkupId(true);
		form.add(new ListView<FormObject>("formList",formObjects){

			@Override
			protected void populateItem(ListItem<FormObject> item) {
				if(item.getModelObject().isNew) item.add(new FormComponentNew("field",item.getModelObject())); 
				else {
					if(item.getModelObject().options==null) item.add(new FormComponentComplexField("field",item.getModelObject()));
					else item.add(new FormComponentOptions("field",item.getModelObject()));
				}
			}}
		);
		//TODO note that we have bypassed wicket validation here...
		//for (AbstractFormValidator v:validators) form.add(v);
		//add(new Label("version",myEntity.getLastVersion()));
		add(form);
		//form.add(entityLabel);
		feedbackPanel=new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		add(feedbackPanel);
		
		add(new AjaxLink("addObjectField") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(form);
				FormObject newField=new FormObject();
				//newField.property="http://bogusNow";
				newField.isURI=true;
				newField.isNew=true;
				formObjects.add(newField);
				// TODO Auto-generated method stub
				
			}
			
		});
		add(new AjaxLink("addDataField"){

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(form);
				FormObject newField=new FormObject();
				//newField.property="http://bogusNow";
				newField.isNew=true;
				newField.isURI=false;
				
				formObjects.add(newField);
				// TODO Auto-generated method stub
				
			}
			
		});
		
		actionDescription=new AjaxyTextArea("actionDescription");
		add(actionDescription);
		
	}
	
	/**
	 * Used to package information needed to construct and operate a field
	 * @author andreasplendiani
	 *
	 */
	class FormObject implements Serializable{
		String property=null;		//The property this field refers to
		boolean isNumeric=false;	//Whether this field is numeric
		boolean isURI=false;		//Whether this field is a URI (object property)
		String[] options=null;		//Option constraints (if it applies)
		String language=null;		//Language constraints (if it applied)
		org.apache.wicket.model.Model<String> value=null;	//Model for the value of the object (for wicket)
		org.apache.wicket.model.Model<String> propValue=null;	//Model for the property (when it applies, e.g. for new fields)
		boolean isNew=false;		//Whether this element has just be added (e.g.: property is from the model)
		
	}
}



