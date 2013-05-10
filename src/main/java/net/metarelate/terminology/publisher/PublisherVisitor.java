package net.metarelate.terminology.publisher;

import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;

public abstract class PublisherVisitor {
	public abstract void crawl(TerminologySet root);
	public abstract void visit(TerminologySet set);
	public abstract void visit(TerminologyIndividual ind);
}
