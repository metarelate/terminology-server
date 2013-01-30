package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.AuthException;
import net.metarelate.terminology.exceptions.RegistryAccessException;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class EditPageOld  extends SuperPage {
	private static final long serialVersionUID = 1L;
	public EditPageOld(final PageParameters parameters) {
		super(parameters);
		final String urlToEdit=parameters.get("entity").toString();
		add(new Label("title",urlToEdit));
		
		TerminologySet mySet=null;
		if(CommandWebConsole.myInitializer.myFactory.terminologySetExist(urlToEdit)) mySet=CommandWebConsole.myInitializer.myFactory.getOrCreateTerminologySet(urlToEdit);
		System.out.println(">>"+urlToEdit);
		// TODO if null, exception
				
		
		final TextField<String> entityLabel = new TextField<String>("entityLabel", org.apache.wicket.model.Model.of(mySet.getLabel(mySet.getLastVersion())));
		//final TextField<String> entityLabel = new TextField<String>("entityLabel", org.apache.wicket.model.Model.of(""));

		entityLabel.setRequired(true);
		entityLabel.add(new LabelValidator());
		
		Form<?> form = new Form<Void>("entityForm") {

			@Override
			protected void onSubmit() {
				//This is called only if valid!
				
				final String labelValue = entityLabel.getModelObject();
				Statement newStatement=ResourceFactory.createStatement(ResourceFactory.createResource(urlToEdit), MetaLanguage.labelProperty, ResourceFactory.createPlainLiteral(labelValue));
				Model newStats=ModelFactory.createDefaultModel().add(newStatement);
				try {
					CommandWebConsole.myInitializer.myTerminologyManager.addToEntityInformation(urlToEdit, newStats, CommandWebConsole.myInitializer.getDefaultUserURI(), "dumb edit");
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
				
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("entity", urlToEdit);
				setResponsePage(EditPageOld.class, pageParameters);

			}

		};
		add(new Label("version",mySet.getLastVersion()));
		add(form);
		form.add(entityLabel);
		add(new FeedbackPanel("feedback"));
		
    }
	@Override
	String getSubPage() {
		return "Term edition";
	}
	@Override
	String getCoreMessage() {
		return "Nothing to say";
	}

}



