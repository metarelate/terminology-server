package net.metarelate.terminology.reasoning;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class ReasonerProxyConfig {
	public static final Property generatesPropertyProperty=ResourceFactory.createProperty("http://metarelate.net/config/generatesProperty");
	public static final Property generatesTypeProperty=ResourceFactory.createProperty("http://metarelate.net/config/generatesType");

	public static final Property symmetricProperty=ResourceFactory.createProperty("http://metarelate.net/config/symmetric");
}
