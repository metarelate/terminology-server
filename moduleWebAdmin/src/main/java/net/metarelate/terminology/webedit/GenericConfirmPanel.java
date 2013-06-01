package net.metarelate.terminology.webedit;

import net.metarelate.terminology.utils.Loggers;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;


public class GenericConfirmPanel extends Panel
{
	Label textLabel=null;
	AjaxLink confirmButton=null;
	AjaxLink abandonButton=null;
	AjaxyTextArea description=null;
	FunctionExecutor myExecutor=null;
	
	
    /**
     * @param id
     * @param message
     * @param container
     */
    public GenericConfirmPanel(String id, final ViewPage viewPage, final String urlToAction)
    {
        super(id);
        String message="Do you really want to proceed with this action ?";	
        textLabel=new Label("text",message);
        textLabel.setOutputMarkupId(true);
        add(textLabel);
        confirmButton=new AjaxLink("proceedButton",new ResourceModel("Proceed")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(viewPage.feedbackPanel);
				viewPage.obsoleteConfirmPanelWindow.close(target); 
				if(myExecutor!=null) myExecutor.execute(target);
				//viewPage.proceedObsolete(target);
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
    	Loggers.webAdminLogger.debug("Result: "+result);
    	return result;
    }
   
    public void setMessage(String message) {
    	textLabel.setDefaultModelObject(message);
    }
    
    public void setExecutor(FunctionExecutor e) {
    	myExecutor=e;
    }
    

}