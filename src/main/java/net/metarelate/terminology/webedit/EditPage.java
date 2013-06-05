package net.metarelate.terminology.webedit;

import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.PropertyConstraintException;
import net.metarelate.terminology.exceptions.UnknownURIException;
import net.metarelate.terminology.exceptions.WebSystemException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
/**
 * Specialized the behaviour of the AbstractEdit page for an edit operation
 * @author andreasplendiani
 *
 */
public class EditPage  extends AbstractEditPage {
	private static final long serialVersionUID = 1L;
	private String urlToEdit=null;
	public EditPage(final PageParameters parameters) throws WebSystemException, ConfigurationException, PropertyConstraintException, UnknownURIException, ModelException {
		super(parameters);
		/*
		 * URL is fixed, we just show it.
		 */
		urlToEdit=parameters.get("entity").toString();
		add(new Label("urlToEdit",urlToEdit));
		
		/*
		 * Things to set that are specific of edit
		 */
		isNew=false;
		isEdit=true;
		pageMessage="Nothing to say";
		/*
		 * We now the entity here, so we just create it
		 */
		if(CommandWebConsole.myInitializer.myFactory.terminologyIndividualExist(urlToEdit)) {
			//myEntity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologyIndividual(urlToEdit);
			isSet=false;
			isIndividual=true;
		}
		else if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToEdit)) {
			//myEntity=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToEdit);
			isSet=true;
			isIndividual=false;
		}
		else {
			// TODO nothing was found, which is impossible. But let's add some fall back action here anyway...
			//TODO we should set an error page here
			return;
		}
		
		terminologyEntityWrapper=new LoadableDetachableModel<TerminologyEntity>() {
			@Override
			protected TerminologyEntity load() {
				if(isSet) return CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologySet(urlToEdit);
				else if(isIndividual) return CommandWebConsole.myInitializer.myFactory.getUncheckedTerminologyIndividual(urlToEdit);
				else {
					//TODO we should set an error here
					//this should not happen
					return null;
				}
			}
		};
		
		
		
		buildForm();
		postConstructionFinalize();
		///
		

		
		
		
			
		
		//final TextField<String> entityLabel = new TextField<String>("entityLabel", org.apache.wicket.model.Model.of(terminologyEntityWrapper.getObject().getLabel(terminologyEntityWrapper.getObject().getLastVersion())));
		//final TextField<String> entityLabel = new TextField<String>("entityLabel", org.apache.wicket.model.Model.of(""));

		//entityLabel.setRequired(true);
		//entityLabel.add(new LabelValidator());
		/*
		Form<?> form = new Form<Void>("editForm") {

			@Override
			protected void onSubmit() {
				//This is called only if valid!
				
				final String labelValue = entityLabel.getModelObject();
				Statement newStatement=ResourceFactory.createStatement(ResourceFactory.createResource(urlToEdit), MetaLanguage.labelProperty, ResourceFactory.createPlainLiteral(labelValue));
				Model newStats=ModelFactory.createDefaultModel().add(newStatement);
				try {
					CommandWebConsole.myInitializer.myTerminologyManager.replaceEntityInformation(urlToEdit, newStats, CommandWebConsole.myInitializer.getDefaultUserURI(), "dumb edit");
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					getSession().error("Auth error");
					e.printStackTrace();
				} catch (RegistryAccessException e) {
					// TODO Auto-generated catch block
					getSession().error("Reg error");
					e.printStackTrace();
				}
				// update label with labelValue
				catch (InvalidProcessException e) {
					// TODO Auto-generated catch block
					getSession().error("Process error");
					e.printStackTrace();
				}
				
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("entity", urlToEdit);
				setResponsePage(ViewPage.class, pageParameters);

			}
		 
		};
		*/
		//add(new Label("version",myEntity.getLastVersion()));
		//add(form);
		//form.add(entityLabel);
		//add(new FeedbackPanel("feedback"));
		//postConstructionFinalize();
		
    }
	@Override
	String getSubPage() {
		return "Term edition";
	}
	@Override
	String getPageStateMessage() {
		return pageMessage;
	}
	
	
	@Override
	protected String getURIOfEntity() {
		return urlToEdit;
	}
	@Override
	protected boolean isURIValid() {
		return true;
	}
	

}



