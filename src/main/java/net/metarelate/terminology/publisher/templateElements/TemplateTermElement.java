package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public interface  TemplateTermElement extends TemplateElement {

	public abstract String render(TerminologyEntity e, String version, int level);



}
