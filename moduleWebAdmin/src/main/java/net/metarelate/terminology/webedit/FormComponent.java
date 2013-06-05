package net.metarelate.terminology.webedit;

import org.apache.wicket.markup.html.panel.Panel;

public abstract class FormComponent extends Panel {
	protected String propertyURI=null;
	protected boolean isNumeric=false;
	protected boolean isData=false;
	protected boolean isURI=false;
	protected boolean inReg=false;
	protected int minCardinality=-1;
	protected int maxCardinality=-1;
	protected String languageTag=null;
	protected String[] values=null;
	public FormComponent(String id,String propertyURI, int minCardinality, int maxCardinality) {
		super(id);
		this.propertyURI=propertyURI;
		this.minCardinality=minCardinality;
		this.maxCardinality=maxCardinality;
	}
	
	public void setValues(String[] values) {
		this.values=values;
	}
	
	public void setIsData(boolean b) {
		isData=b;
		isURI=!b;
		
	}
	public void setIsURI(boolean b) {
		isURI=b;
		isData=!b;
		
	}
	
	public void setIsNumeric(boolean b) {
		isNumeric=b;
		
	}

	public abstract void build();

	public void selfValidate(AbstractEditPage abstractEditPage) {
		// TODO Auto-generated method stub
		
	}
}
