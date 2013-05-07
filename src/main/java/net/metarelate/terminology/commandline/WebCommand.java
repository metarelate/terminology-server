package net.metarelate.terminology.commandline;

import org.apache.wicket.util.time.Duration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import net.metarelate.terminology.instanceManager.Initializer;

public class WebCommand extends TsCommand {
	private int port=8080;
	public WebCommand(Initializer myInitializer, String[] args, boolean debug) {
		super(myInitializer, args,debug);
		
		boolean nextIsPort=false;
		for(String arg:args) {
			if(arg.equalsIgnoreCase("--port") || arg.equalsIgnoreCase("-port")) {
				nextIsPort=true;
			}
			else if(nextIsPort) {
				port=Integer.parseInt(arg);
				nextIsPort=false;
			}
		}
		
	}

	@Override
	public void localExecute() {
		int timeout = (int) Duration.ONE_HOUR.getMilliseconds();
		Server server = new Server();
		SocketConnector connector = new SocketConnector();
		
		// Set some timeout options to make debugging easier.
	    connector.setMaxIdleTime(timeout);
	    connector.setSoLingerTime(-1);
	    connector.setPort(port);
	    server.addConnector(connector);
	        
	        //Note: No security for local installation. This is anyway for a local connection. 
	        /*
	        Resource keystore = Resource.newClassPathResource("/keystore");
	        if (keystore != null && keystore.exists()) {
	            // if a keystore for a SSL certificate is available, start a SSL
	            // connector on port 8443.
	            // By default, the quickstart comes with a Apache Wicket Quickstart
	            // Certificate that expires about half way september 2021. Do not
	            // use this certificate anywhere important as the passwords are
	            // available in the source.

	            connector.setConfidentialPort(8443);

	            SslContextFactory factory = new SslContextFactory();
	            factory.setKeyStoreResource(keystore);
	            factory.setKeyStorePassword("wicket");
	            factory.setTrustStoreResource(keystore);
	            factory.setKeyManagerPassword("wicket");
	            SslSocketConnector sslConnector = new SslSocketConnector(factory);
	            sslConnector.setMaxIdleTime(timeout);
	            sslConnector.setPort(8443);
	            sslConnector.setAcceptors(4);
	            server.addConnector(sslConnector);

	            System.out.println("SSL access to the quickstart has been enabled on port 8443");
	            System.out.println("You can access the application using SSL on https://localhost:8443");
	            System.out.println();
	        }
			*/
	        WebAppContext bb = new WebAppContext();
	        bb.setServer(server);
	        bb.setContextPath("/");
	        //String webDir = this.getClass().getResource("/").toExternalForm();
	        
	        //TODO make what's below system-independent 
	        
	        //TODO below it's just a test
	        bb.setWar(myInitializer.getRootDirectory() +"/web");
	        try {
				System.out.println(Class.forName("net.metarelate.terminology.commandline.WebCommand").getProtectionDomain());
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

	        // START JMX SERVER
	        // MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
	        // MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
	        // server.getContainer().addEventListener(mBeanContainer);
	        // mBeanContainer.start();
	      server.setHandler(bb);

	      try {
	          System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
	          server.start();
	          System.in.read();
	          System.out.println(">>> STOPPING EMBEDDED JETTY SERVER");
	          server.stop();
	          server.join();
	      } catch (Exception e) {
	          e.printStackTrace();
	          System.exit(1);
          }        
	  }

	
	public static String getStaticLocalHelpMessage() {
		return "ts web [--port number]\n"+
		"Starts a web server for local admin on the port specified (defaults to 8080)\n"+
		"Attention: works only if properly configured!\n";
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public String getLocalHelpMessage() {
		return getStaticLocalHelpMessage();
	}
	
	

}
