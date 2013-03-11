package net.metarelate.terminology.webedit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;

import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.webedit.AbstractEditPage.FormObject;

public class FormComponentComplexField extends Panel {
	FormObject myObject=null;
	
	public FormComponentComplexField(String id, FormObject modelObject) {
		super(id);
		myObject=modelObject;
		Label propertyLabel=new Label("fieldProperty",CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(myObject.property, LabelManager.LANG_DEF_URI));
		if(myObject.value==null) myObject.value=new org.apache.wicket.model.Model<String>();
		final TextField<String> inputField = new TextField<String>("fieldValue", myObject.value);
		Label language=null;
		if(myObject.language!=null) {
			language=new Label("fieldLanguage",myObject.language);
		}
		else language=new Label("fieldLanguage","");
		add(propertyLabel);
		add(inputField);
		add(language);
	}


	
	
	
}
