package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.exceptions.ModelException;

public interface TemplateGlobalElement extends TemplateElement {
	public  String render(TerminologyFactory factory) throws ModelException;
}
