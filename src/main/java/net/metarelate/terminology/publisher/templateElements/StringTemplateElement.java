package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public class StringTemplateElement implements TemplateFixedElement, TemplateTermElement {
	private String stringToRender=null;
	public StringTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		System.out.println("String block: "+stringToRender); //TODO test
	}
	
	
	public String render(String tag) {
		return stringToRender;
	}

	public boolean isFixed() {
		return true;
	}

	public boolean isPerTerm() {
		return false;
	}

	public String render(TerminologyEntity e, String version, int level) {
		return stringToRender;
	}
}