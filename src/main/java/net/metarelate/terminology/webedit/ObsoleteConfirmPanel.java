package net.metarelate.terminology.webedit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


public class ObsoleteConfirmPanel extends Panel
{
	Label textLabel=null;
	AjaxLink confirmButton=null;
	AjaxLink abandonButton=null;
	AjaxyTextArea description=null;
	
	
    /**
     * @param id
     * @param message
     * @param container
     */
    public ObsoleteConfirmPanel(String id, final ViewPage viewPage, final String urlToAction)
    {
        super(id);
        String message="Do you really want to obsolete this term ?";	
        textLabel=new Label("text",message);
        add(textLabel);
        confirmButton=new AjaxLink("proceedButton",new ResourceModel("Proceed")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(viewPage.feedbackPanel);
				viewPage.obsoleteConfirmPanelWindow.close(target); 
				viewPage.proceedObsolete(target);
				//PageParameters pageParameters = new PageParameters();
				//pageParameters.add("entity", urlToAction);
				//setResponsePage(ViewPage.class,pageParameters);
				
			}
        	
        };
        add(confirmButton);
        abandonButton=new AjaxLink("abandonButton",new ResourceModel("Abandon")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(viewPage.feedbackPanel);
				viewPage.abandonCommand();
				viewPage.obsoleteConfirmPanelWindow.close(target);
				
				// TODO Auto-generated method stub
				
			}
        	
        };
        add(abandonButton);
        
        
        
        
        
        description = new AjaxyTextArea("description");		
        add(description);
        
      
    }
    
    public String getDescription() {
    	String result=description.getText();
    	System.out.println("Result: "+result);
    	return result;
    }
   
    

}