package net.metarelate.terminology.webedit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;


public class SupersedPanel2 extends Panel
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
    public SupersedPanel2(String id, final ViewPage viewPage, final String urlToAction)
    {
        super(id);
        String message="Do you really want to supersed this item ?";	
        textLabel=new Label("text",message);
        add(textLabel);
        confirmButton=new AjaxLink("proceedButton",new ResourceModel("Proceed")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(viewPage.feedbackPanel);
				viewPage.obsoleteConfirmPanelWindow.close(target); 
				viewPage.proceedSupersed(target);
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
				viewPage.supersedPanelWindow.close(target);
				
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