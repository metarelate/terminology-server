package net.metarelate.terminology.webedit;

import net.metarelate.terminology.coreModel.TerminologySet;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class EditPage  extends WebPage {
	private static final long serialVersionUID = 1L;
	public EditPage(final PageParameters parameters) {
		super(parameters);
		final String urlToEdit=parameters.get("entity").toString();
		add(new Label("title",urlToEdit));
		
		TerminologySet mySet=null;
		if(CommandWebConsole.myFactory.terminologySetExist(urlToEdit)) mySet=CommandWebConsole.myFactory.getOrCreateTerminologySet(urlToEdit);
		System.out.println(">>"+urlToEdit);
		// TODO if null, exception
				
		
		final TextField<String> entityLabel = new TextField<String>("entityLabel", Model.of(mySet.getLabel(mySet.getLastVersion())));
		entityLabel.setRequired(true);
		entityLabel.add(new LabelValidator());
		
		Form<?> form = new Form<Void>("entityForm") {

			@Override
			protected void onSubmit() {
				//This is called only if valid!
				
				final String labelValue = entityLabel.getModelObject();
				// update label with labelValue
				
				PageParameters pageParameters = new PageParameters();
				pageParameters.add("entity", urlToEdit);
				setResponsePage(EditPage.class, pageParameters);

			}

		};
		add(new Label("version",mySet.getLastVersion()));
		add(form);
		form.add(entityLabel);
		add(new FeedbackPanel("feedback"));
		
    }

}



