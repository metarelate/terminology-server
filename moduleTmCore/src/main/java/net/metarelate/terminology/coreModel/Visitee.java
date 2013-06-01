package net.metarelate.terminology.coreModel;

import java.io.IOException;

import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.publisher.PublisherVisitor;

public interface Visitee {
	public void accept(PublisherVisitor v) throws WebWriterException, IOException, ConfigurationException, ModelException;
}
