package net.metarelate.terminology.coreModel;

public interface CacheManager {

	public abstract String getValueFor(String resource, String property);

	public abstract boolean cleanValueFor(String resource, String property);

	public abstract void recordValue(String resource, String property,
			String value);

	public abstract void changeValue(String resource, String property,
			String value);

}