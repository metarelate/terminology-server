package net.metarelate.terminology.webedit;

import java.util.Arrays;

import net.metarelate.terminology.coreModel.LabelManager;
import net.metarelate.terminology.utils.Loggers;
import net.metarelate.terminology.webedit.AbstractEditPage.FormObject;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;

public class FormComponentOptions extends Panel {
	FormObject myObject=null;
	
	public FormComponentOptions(String id, FormObject modelObject) {
		super(id);
		myObject=modelObject;
		Label propertyLabel=new Label("fieldProperty",CommandWebConsole.myInitializer.myFactory.getLabelManager().getLabelForURI(myObject.property, LabelManager.LANG_DEF_URI));
		if(myObject.value==null) myObject.value=new org.apache.wicket.model.Model<String>();
		else if(!Arrays.asList(myObject.options).contains(myObject.value.getObject())) {
			String[] newOptions=new String[myObject.options.length+1];
			int i=0;
			for(i=0;i<myObject.options.length;i++) {
				newOptions[i]=myObject.options[i];
			}
			
			newOptions[i]=myObject.value.getObject();
			myObject.options=newOptions;
		}
		Loggers.webAdminLogger.debug("Default option should be: "+myObject.value);
		DropDownChoice<String> fieldChoice = 
		            new DropDownChoice<String>("fieldOptions",myObject.value,Arrays.asList(myObject.options));
		        
		
		
		
		//Label language=new Label("fieldLanguage","");
		add(propertyLabel);
		add(fieldChoice);
		//add(language);
	}

}
