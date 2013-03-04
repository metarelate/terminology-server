package net.metarelate.terminology.webedit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;


public class SupersedPanel1 extends Panel
{
	AjaxLink searchButton=null;
	AjaxLink newButton=null;
	
    /**
     * @param id
     * @param message
     * @param container
     */
    public SupersedPanel1(String id ,final ViewPage viewPage)
    {
        super(id);
      
        searchButton=new AjaxLink("searchButton",new ResourceModel("Search")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				viewPage.supersedPanelWindow.close(target);
				viewPage.supersedRouteToSearch(target);
				
				// TODO Auto-generated method stub
				
			}
        	
        };
        add(searchButton);
        newButton=new AjaxLink("newButton",new ResourceModel("New")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				viewPage.supersedPanelWindow.close(target);
				viewPage.supersedRouteToAdd(target);
				// TODO Auto-generated method stub
				
			}
        	
        };
        add(newButton);
    }
    


}