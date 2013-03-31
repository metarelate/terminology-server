package net.metarelate.terminology.instanceManager;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class InitializerConfig {
	public static final Property tdbPrefixProperty=ResourceFactory.createProperty("http://metarelate.net/config/tdbPrefix");

}
