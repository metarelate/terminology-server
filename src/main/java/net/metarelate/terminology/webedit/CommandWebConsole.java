package net.metarelate.terminology.webedit;

import net.metarelate.terminology.config.MetaLanguage;
import net.metarelate.terminology.coreModel.TerminologyFactory;
import net.metarelate.terminology.coreModel.TerminologyFactoryTDBImpl;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.management.TerminologyManager;
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
	public static final int MANAGEMENT_PAGE = 0;
	public static final int LOCAL_CONFIGURATION_PAGE = 1;
	public static final int OTHER_PAGE = 2;
	
	public static Initializer myInitializer=null;
	
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return (Class<? extends WebPage>) SearchPage.class;
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
		
		
		mountPage("/edit",EditPage.class);
		mountPage("/config",LocalConfiguration.class);
		//mountPage("/search",SearchPage.class);
		mountPage("/search",SearchPage.class);
		mountPage("/view",ViewPage.class);
	}
	
	
}
