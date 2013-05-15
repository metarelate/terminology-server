package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public class DummyTemplateElement extends TemplateTermElement {
	private String stringToRender=null;
	public DummyTemplateElement(String stringToRender) {
		super();
		this.stringToRender = stringToRender;
		System.out.println("Dummy block: "+stringToRender); //TODO test
	}
	@Override
	public String render(TerminologyEntity e, String version, int level) {
		return "Dummy element for entity "+e.getURI()+" , should expand accoring to "+stringToRender;
	}
}