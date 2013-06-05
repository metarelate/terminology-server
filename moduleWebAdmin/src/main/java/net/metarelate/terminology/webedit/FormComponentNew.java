package net.metarelate.terminology.webedit;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;

import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.exceptions.WebSystemException;
import net.metarelate.terminology.webedit.AbstractEditPage.FormObject;

public class FormComponentNew extends Panel {
	FormObject myObject=null;
	
	public FormComponentNew(String id, FormObject modelObject) {
		super(id);
		myObject=modelObject;
		if(myObject.value==null) myObject.value=new org.apache.wicket.model.Model<String>();
		if(myObject.propValue==null) myObject.propValue=new org.apache.wicket.model.Model<String>();
		final TextField<String> inputField = new TextField<String>("fieldValue", myObject.value);
		final TextField<String> inputField2 = new TextField<String>("fieldProperty", myObject.propValue);
		Label language=null;
		if(myObject.language!=null) {
			language=new Label("fieldLanguage",myObject.language);
		}
		else language=new Label("fieldLanguage","");
		add(inputField2);
		add(inputField);
		add(language);
	}


	
	
	
}
