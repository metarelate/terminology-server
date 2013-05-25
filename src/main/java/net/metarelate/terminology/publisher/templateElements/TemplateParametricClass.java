package net.metarelate.terminology.publisher.templateElements;

import java.util.HashMap;
import java.util.Map;

public class TemplateParametricClass {
	private final static String paramDelimiter="||";
	private final static String paramEqualSign="::";
	private final static String subParamSign="-";
	private final static String spacingStringParam="spacing";
	public static final String labelString="label";
	public static final String scopeForVersionString="versionScope";
	public static final String scopeAllString="all";
	public static final String scopeVersionString="version";
	public static final String scopeNonversionString="nonversion";
	public static final String statementsBlockModeParamName="mode";
	public static final String statementsBlockConceptLabelParamName="conceptLabel";
	public static final String statementsBlockCodeLabelParamName="codeLabel";
	public static final String statementsBlockOtherLabelParamName="otherLabel";
	
	protected boolean printIfVersioned=true;
	protected boolean printIfUnVersioned=true;
	
	protected String statementsBlockMode="html";
	protected String statementsBlockConceptLabel="";
	protected String statementsBlockCodeLabel="codeLabel";
	protected String statementsBlockOtherLabel="otherLabel";
	
	
	String rawString=null;
	Map<String,String> labelMap=new HashMap<String,String>();
	protected String spacingStringValue="&nbsp;";
	public TemplateParametricClass(String templateText) {
		//super();
		while(templateText.contains(paramDelimiter)) {
			String param=templateText.substring(0, templateText.indexOf(paramDelimiter));
			templateText=templateText.substring(templateText.indexOf(paramDelimiter)+paramDelimiter.length());
			
			String[] values=param.split(paramEqualSign);
			if(values[0].equalsIgnoreCase(spacingStringParam)) spacingStringValue=values[1];
			else if (values[0].equalsIgnoreCase(labelString)) {
				String[] subParam=values[1].split(subParamSign);
				labelMap.put(subParam[0],subParam[1]);
			}
			else if(values[0].equalsIgnoreCase(scopeForVersionString)) {
				if(values[1].equalsIgnoreCase(scopeVersionString)) {
					printIfUnVersioned=false;
					printIfVersioned=true;
				}
				else if(values[1].equalsIgnoreCase(scopeNonversionString)) {
					printIfVersioned=false;
					printIfUnVersioned=true;
				}
				else {
					printIfVersioned=true;
					printIfUnVersioned=true;
				}
				
			}
			else if (values[0].equalsIgnoreCase(statementsBlockModeParamName)) {
				statementsBlockMode=values[1];
			}
			else if (values[0].equalsIgnoreCase(statementsBlockConceptLabelParamName)) {
				statementsBlockConceptLabel=values[1];
			}
			else if (values[0].equalsIgnoreCase(statementsBlockCodeLabelParamName)) {
				statementsBlockCodeLabel=values[1];
			}
			else if (values[0].equalsIgnoreCase(statementsBlockOtherLabelParamName)) {
				statementsBlockOtherLabel=values[1];
			}
			
			//else if (values[0].equalsIgnoreCase(anotherString)) {}
		}
		rawString=templateText;
	}
	
}
