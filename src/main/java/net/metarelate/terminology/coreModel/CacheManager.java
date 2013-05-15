package net.metarelate.terminology.coreModel;

public interface CacheManager {

	public  String getValueFor(String resource, String property);

	public  boolean cleanValueFor(String resource, String property);

	public  void recordValue(String resource, String property,
			String value);

	public  void changeValue(String resource, String property,
			String value);

	public void forceCleanProp(String propertyURI);

}