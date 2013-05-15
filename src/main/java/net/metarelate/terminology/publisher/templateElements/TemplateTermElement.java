package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public abstract class TemplateTermElement extends TemplateElement {

	public abstract String render(TerminologyEntity e, String version, int level);

	@Override
	public boolean isFixed() {
		return false;
	}

	@Override
	public boolean isPerTerm() {
		return true;
	}

}
