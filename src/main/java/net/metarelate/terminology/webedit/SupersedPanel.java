package net.metarelate.terminology.webedit;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;


public class SupersedPanel extends Panel
{
	AjaxLink searchButton=null;
	AjaxLink newButton=null;
	public boolean isSearch=false;
	
    /**
     * @param id
     * @param message
     * @param container
     */
    public SupersedPanel(String id ,final ModalWindow container)
    {
        super(id);
      
        searchButton=new AjaxLink("searchButton",new ResourceModel("Search")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isSearch=true;
				container.close(target);
				
				// TODO Auto-generated method stub
				
			}
        	
        };
        add(searchButton);
        newButton=new AjaxLink("newButton",new ResourceModel("New")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				isSearch=false;
				container.close(target);
				// TODO Auto-generated method stub
				
			}
        	
        };
        add(newButton);
    }
    
    public boolean getValueOnce() {
    	boolean toReturn=isSearch;
    	isSearch=false;
    	return toReturn;
    }
    

}