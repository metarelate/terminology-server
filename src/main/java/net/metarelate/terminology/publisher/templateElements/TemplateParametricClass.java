package net.metarelate.terminology.publisher.templateElements;

import java.util.HashMap;
import java.util.Map;

public class TemplateParametricClass {
	private final static String paramDelimiter="||";
	private final static String paramEqualSign="::";
	private final static String subParamSign="-";
	private final static String spacingStringParam="spacing";
	public static final String labelString="label";
	String cleanString=null;
	Map<String,String> labelMap=new HashMap<String,String>();
	protected String spacingStringValue="&nbsp;";
	public TemplateParametricClass(String templateText) {
		super();
		while(templateText.contains(paramDelimiter)) {
			String param=templateText.substring(0, templateText.indexOf(paramDelimiter));
			templateText=templateText.substring(templateText.indexOf(paramDelimiter)+paramDelimiter.length());
			String[] values=param.split(paramEqualSign);
			if(values[0].equalsIgnoreCase(spacingStringParam)) spacingStringValue=values[1];
			else if (values[0].equalsIgnoreCase(labelString)) {
				String[] subParam=values[1].split(subParamSign);
				labelMap.put(subParam[0],subParam[1]);
			}
			//else if (values[0].equalsIgnoreCase(anotherString)) {}
		}
		cleanString=templateText;
	}
	
}
