package net.metarelate.terminology.webedit;

import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.instanceManager.Initializer;
import net.metarelate.terminology.utils.Loggers;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import org.apache.wicket.Application;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.ClassProvider;

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
			Loggers.webAdminLogger.fatal("Initalization fatal problem");
			e1.printStackTrace();
			System.exit(-1);
		}
		
		
		mount(new PageParameterAwareMountedMapper("/edit",EditPage.class));
		mount(new PageParameterAwareMountedMapper("/config",LocalConfiguration.class));
		//mountPage("/search",SearchPage.class);
		mount(new PageParameterAwareMountedMapper("/search",SearchPage.class));
		mount(new PageParameterAwareMountedMapper("/view",ViewPage.class));
		mount(new PageParameterAwareMountedMapper("/new",NewPage.class));
	}
	
	
	
	/**
	 * Intercept wicket page rendering to create a new page if a parameter is changed
	 * Solution from Johannes Unterstein
	 * http://wicket-tales.org/2012/05/16/changing-pageparameters/
	 * 
	 */
	
	
	/**
	* This mapper represents exactly the same behavior like the default
	* {@link MountedMapper}, except one behavior:<br/>
	* The default {@link MountedMapper} implementation prefers the pageId parameter
	* with a higher priority then the rest of the {@link PageParameters}.<br>
	* This preference leads to the following behavior:
	*
	* <pre>
	* ...myApplication/User/4?15
	* </pre>
	*
	* leads to the UserPage with the indexed {@link PageParameters} "4" at index 0.
	* So the User with id 4 is shown. When a user manually changes the URL to
	* something like this:
	*
	* <pre>
	* ...myApplication/User/5?15
	* </pre>
	*
	* The {@link MountedMapper} would deliver the same page as before and would
	* show the user with id 4 and not - like it is implicated in the url - the user
	* with id 5, because the pageId parameter is preferred during mapping process. <br/>
	* <br/>
	* Therefore this implementation compares the existing {@link PageParameters} of
	* the cached page and the {@link PageParameters} of the current request. If a
	* difference between this two {@link PageParameters} are recognized, this
	* mapper redirects to a fresh bookmarkable instance of the current requested
	* page. <br/>
	* <br/>
	* To use this mapper to mount your pages, you must declare the mapper
	* programmatically in your {@link Application} by overwriting the
	* "public void init()" method, like shown below:
	*
	* <pre>
	* @Override
	* public void init() {
	*     super.init();
	*     // Use our own mapper to mount the mountpage
	*     mount(new PageParameterAwareMountedMapper(&quot;Home&quot;, HomePage.class));
	* }
	* </pre>
	*
	* @author <a href="mailto:unterstein@me.com">Johannes Unterstein</a>
	*
	*/
	public class PageParameterAwareMountedMapper extends MountedMapper {
	    public PageParameterAwareMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass) {
	        super(mountPath, pageClass);
	    }
	    public PageParameterAwareMountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass,
	            IPageParametersEncoder pageParametersEncoder) {
	        super(mountPath, pageClass, pageParametersEncoder);
	    }
	    public PageParameterAwareMountedMapper(String mountPath, ClassProvider<? extends IRequestablePage> pageClassProvider) {
	        super(mountPath, pageClassProvider);
	    }
	    public PageParameterAwareMountedMapper(String mountPath, ClassProvider<? extends IRequestablePage> pageClassProvider,
	            IPageParametersEncoder pageParametersEncoder) {
	        super(mountPath, pageClassProvider, pageParametersEncoder);
	    }
	    @Override
	    protected IRequestHandler processHybrid(PageInfo pageInfo, Class<? extends IRequestablePage> pageClass, PageParameters pageParameters, Integer renderCount) {
	        IRequestHandler handler = super.processHybrid(pageInfo, pageClass, pageParameters, renderCount);
	        if (handler instanceof RenderPageRequestHandler) {
	            // in the current implementation (wicket 1.5.6) super.processHybrid
	            // returns a RenderPageRequestHandler
	            RenderPageRequestHandler renderPageHandler = (RenderPageRequestHandler) handler;
	            if (renderPageHandler.getPageProvider() instanceof PageProvider) {
	                PageProvider provider = (PageProvider) renderPageHandler.getPageProvider();
	                // This check is necessary to prevent a RestartResponseAtInterceptPageException at the wrong time in request cycle
	                if (provider.hasPageInstance()) {
	                    PageParameters newPageParameters = renderPageHandler.getPageParameters();
	                    PageParameters oldPageParameters = renderPageHandler.getPageProvider().getPageInstance().getPageParameters();
	                    // if we recognize a change between the page parameter of the loaded
	                    // page and the page parameter of the current request, we redirect
	                    // to a fresh bookmarkable instance of that page.
	                    if (!PageParameters.equals(oldPageParameters, newPageParameters)) {
	                        handler = processBookmarkable(pageClass, newPageParameters);
	                    }
	                }
	            }
	        }
	        return handler;
	    }
	}
	
	
}
