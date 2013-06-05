package net.metarelate.terminology.webedit;

import net.metarelate.terminology.utils.Loggers;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.PropertyModel;

public class AjaxyTextArea extends TextArea {
	private static final long serialVersionUID = 1L;
	private String text; 
	
	public AjaxyTextArea(String id) { 
	        super(id); 
	        setModel(new PropertyModel(this, "text")); 
	        add(new AjaxFormComponentUpdatingBehavior("onchange"){ 

	            protected void onUpdate(AjaxRequestTarget target) { 
	            	Loggers.webAdminLogger.debug("text: " + text); 
	            } 
	        }); 
	    } 

	public String getText(){ return text; } 

	public void setText(String text) { this.text = text; } 
} 