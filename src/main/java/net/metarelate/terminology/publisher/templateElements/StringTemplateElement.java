package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public class StringTemplateElement extends TemplateFixedElement {
	private String stringToRender=null;
	public StringTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		System.out.println("String block: "+stringToRender); //TODO test
	}
	
	@Override
	public String render(String tag) {
		return stringToRender;
	}
}