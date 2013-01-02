package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyFactoryTDBImpl;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.SSLogger;
import net.metarelate.terminology.utils.SimpleQueriesProcessor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see net.metarelate.termminology.Start#main(String[])
 */
public class CommandWebConsole extends WebApplication
{    	
	private static Initializer myInitializer=null;
	public static TerminologyFactory myFactory=null;
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		try {
			myInitializer=new Initializer();
		} catch (ConfigurationException e1) {
			SSLogger.log("Initalization fatal problem");
			e1.printStackTrace();
			System.exit(-1);
		}
		Model configuration=null;
		try {
			configuration = myInitializer.getConfigurationGraph();
		} catch (ConfigurationException e) {
			SSLogger.log("Problems in reading configuration files");
			e.printStackTrace();
			System.exit(-1);
		}
		String tdbPath=SimpleQueriesProcessor.getOptionalConfigurationParameterSingleValue(configuration, MetaLanguage.tdbPrefixProperty);
		
		//TODO for the time being only tdb is supported!
		if(tdbPath==null) {
			//throw new ConfigurationException("Unable to find a TDB directory");
			SSLogger.log("Unable to find a TDB directory");
			System.exit(-1);
		}
		myFactory=new TerminologyFactoryTDBImpl(tdbPath);
		
		
		mountPage("/",HomePage.class);
	}
	
	
}
