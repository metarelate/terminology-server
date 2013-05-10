package net.metarelate.terminology.publisher.templateElements;

import net.metarelate.terminology.coreModel.TerminologyEntity;

public abstract class TemplateFixedElement extends TemplateElement {
	public abstract String render(String tag);

	@Override
	public boolean isFixed() {
		return true;
	}

	@Override
	public boolean isPerTerm() {
		return false;
	}

}
