package net.metarelate.terminology.publisher;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;

public interface TerminologyVisitor {
	public String visit (TerminologySet set);
	public String visit (TerminologyIndividual ind);
}
