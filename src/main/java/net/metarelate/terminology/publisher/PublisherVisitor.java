package net.metarelate.terminology.publisher;

import java.io.IOException;

import net.metarelate.terminology.coreModel.TerminologyEntity;
import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;

public abstract class PublisherVisitor {
	//public abstract void crawl(TerminologySet root) throws WebWriterException, ModelException, IOException, ConfigurationException;
	public abstract void visit(TerminologySet set) throws WebWriterException, IOException, ConfigurationException, ModelException;
	public abstract void visit(TerminologyIndividual ind) throws WebWriterException, IOException, ConfigurationException, ModelException;
}
