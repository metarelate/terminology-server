package net.metarelate.terminology.modelBuilders;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TerminologyModelBuilderConfig {
	public static final Resource pragmaExpandDashAndSuppress=ResourceFactory.createResource("http://metarelate.net/config/ExpandDashAndSuppress");
	public static final Resource pragmaExpandTree =ResourceFactory.createResource("http://metarelate.net/config/ExpandTreeProcedure");
	public static final Resource pragmaSuppress =ResourceFactory.createResource("http://metarelate.net/config/pragmaSuppressSource");
	public static final Resource pragmaHardLimitCut =ResourceFactory.createResource("http://metarelate.net/config/hardLimitCut");

	public static final Property pragmaTreeCollection = ResourceFactory.createProperty("http://metarelate.net/config/pragmaTreeCollection");
	public static final Property pragmaSchemaProperty = ResourceFactory.createProperty("http://metarelate.net/config/pragmaSchema");

	public static final Property pragmaPropProperty = ResourceFactory.createProperty("http://metarelate.net/config/pragmaProp");
	public static final Property pragmaOverrideProp = ResourceFactory.createProperty("http://metarelate.net/config/overrideProp");
	public static final Property pragmaHardLimit = ResourceFactory.createProperty("http://metarelate.net/config/hardLimit");
	public static final Property pragmaPad = ResourceFactory.createProperty("http://metarelate.net/config/pragmaPad");
}
