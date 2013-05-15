package net.metarelate.terminology.publisher;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class PublisherConfig {
	/**
	 * Web serialization support (needed to serialize via web)
	 * these properties don't enter the data model
	 * TODO namespace property is currently stored in the data model (it doesn't appear in view, but it should regarded as homogenuos to the other metadata)
	 */
	
	public static final String uriHasUrl="http://metarelate.net/internal/cache/uriHasUrl"; // TODO maybe we should move this somewhere else
	public static final String uriHasDisk="http://metarelate.net/internal/cache/uriHasDisk"; // TODO maybe we should move this somewhere else
	
	
	// The root of the website
	//public static final Property sitePrefixProperty=ResourceFactory.createProperty("http://metarelate.net/config/sitePrefix");
	// The root of the disk location
	public static final Property diskPrefixProperty=ResourceFactory.createProperty("http://metarelate.net/config/diskPrefix");
	// The address of the css fill
	public static final Property cssAddressProperty=ResourceFactory.createProperty("http://metarelate.net/config/cssAddress");
	//public static final Property cssAddressProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#cssAddress");
	// For every entity a namespace to be used to compose a URL 
	
	// Override the base path for a specific collection (or entity?) // TODO decide
	public static final Property overrideBasePathProperty=ResourceFactory.createProperty("http://metarelate.net/config/overridesDiskPrefix");	
	// Override the base namespace for a specific collection (or entity?) // TODO decide

	public static final Property baseURLProperty = ResourceFactory.createProperty("http://metarelate.net/config/baseURL");	
	
	public static final Property overrideBaseSiteProperty=ResourceFactory.createProperty("http://metarelate.net/config/overrideSitePrefix");	
	
	//public static final Property localIdProperty=ResourceFactory.createProperty("http://reference.metoffice.gov.uk/data/wmo/meta/mapping#localID");	

}
