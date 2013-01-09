package net.metarelate.terminology.webedit;

import org.apache.wicket.validation.CompoundValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.StringValidator;

public final class LabelValidator extends CompoundValidator<String> {
	 
	private static final long serialVersionUID = 1L;
 
	public LabelValidator() {
 
		add(StringValidator.lengthBetween(5, 15));
		//add(new PatternValidator("[a-z0-9_- ]+"));
 
	}
}