package org.codehaus.groovy.grails.plugins.batch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

import org.codehaus.groovy.grails.web.context.GrailsConfigUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.web.context.support.XmlWebApplicationContext;

import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.commons.spring.GrailsApplicationContext;
import org.codehaus.groovy.grails.commons.BootstrapArtefactHandler;
import org.codehaus.groovy.grails.compiler.injection.GrailsAwareClassLoader;
import org.codehaus.groovy.grails.web.util.Log4jConfigListener;
import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener;
import org.codehaus.groovy.grails.plugins.PluginManagerHolder;
import org.codehaus.groovy.grails.plugins.GrailsPluginManager;
import org.codehaus.groovy.grails.plugins.DefaultGrailsPluginManager;


import grails.util.GrailsUtil;
import grails.util.Environment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import org.apache.log4j.Logger;

public class Bootstrap {

    public static final Log LOG = LogFactory.getLog(Bootstrap.class);
    //    public static final Logger LOGGER = Logger.getLogger(Bootstrap.class);

    private ServletContext servletContext;
    private ServletContextListener [] servletContextListeners;

    private WebApplicationContext webContext;
    
    public void init(String [] args) {
	LOG.debug("init(): begin");
	// System.out.println("" + LOG);
	// System.out.println("" + LOG.isErrorEnabled());
	// System.out.println("" + LOG.isDebugEnabled());
	// System.out.println("" + LOGGER.getLevel());
	// LOG.error("BLA1"); LOG.debug("BLA1");
	// LOGGER.error("BLA1"); LOGGER.debug("BLA1");
	// System.out.println("+++++++++++++++++++ " + Thread.currentThread().getContextClassLoader());
	// System.out.println("+++++++++++++++++++ " + this.getClass().getClassLoader());
	// System.out.println("args " + java.util.Arrays.asList(args));

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): this classLoader " + this.getClass().getClassLoader());
	    LOG.debug("init(): thread classLoader " + Thread.currentThread().getContextClassLoader());
	}

	/*if (1 == 1) {
	  throw new RuntimeException("bla__");
	  }*/

	//Thread.currentThread().setContextClassLoader(new GrailsAwareClassLoader());

	// System.out.println("environment " + Environment.getCurrent());
	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): env " + Environment.getCurrent());
	}

	String resourcePath = null;
	switch(Environment.getCurrent()) {
	case PRODUCTION:
	    resourcePath = "war";
	    break;
	default:
	    //resourcePath = (new java.io.File("web-app")).getAbsolutePath();
	    resourcePath = "web-app";
	}

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): resourcePath " + resourcePath);
	}

	// System.out.println("path " + resourcePath);
	servletContext = resourcePath != null? new MockServletContext(resourcePath): new MockServletContext();
	servletContext.setAttribute("args", args);

	/*try {
	// System.out.println("path " + servletContext.getResource("/"));
	// System.out.println("path " + servletContext.getResource(""));
	// System.out.println("path " + servletContext.getResource("."));
	} catch (java.net.MalformedURLException e) {
	throw new RuntimeException(e);
	}*/

	servletContextListeners = new ServletContextListener[] {
	    new Log4jConfigListener(),
	    new GrailsContextLoaderListener()
	};
	
	try
	    {
		ServletContextEvent event = new ServletContextEvent(servletContext);
		for (ServletContextListener l : servletContextListeners) {
		    l.contextInitialized(event);
		    // System.out.println("" + l + " ok");
		    // System.out.println("" + LOG);
		    // System.out.println("" + LOG.isErrorEnabled());
		    // System.out.println("" + LOG.isDebugEnabled());
		    // System.out.println("" + LOGGER.getLevel());
		    // LOG.error("BLA2");LOG.debug("BLA1");
		    // LOGGER.error("BLA1"); LOGGER.debug("BLA1");
		}
	    } catch (RuntimeException e) {
	    LOG.error("init()", e);
	    throw e;
	}
	
	// No fixed context defined for this servlet - create a local one.
	/*XmlWebApplicationContext parent = new XmlWebApplicationContext();
	  parent.setServletContext(servletContext);
	  //parent.setNamespace(getClass().getName() + ".CONTEXT.");
	  parent.refresh();*/
	WebApplicationContext parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);

	WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(servletContext);
	// construct the SpringConfig for the container managed application
	//Assert.notNull(parent, "Grails requires a parent ApplicationContext, is the /WEB-INF/applicationContext.xml file missing?");
	GrailsApplication application = parent.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
    
	//WebApplicationContext webContext;
	if (wac instanceof GrailsApplicationContext) {
	    webContext = wac;
	}
	else {
	    webContext = GrailsConfigUtils.configureWebApplicationContext(servletContext, parent);
	    // System.out.println("configureWebApplicationContext ok");

	    try {
		GrailsConfigUtils.executeGrailsBootstraps(application, webContext, servletContext);
		// System.out.println("executeGrailsBootstraps ok");
	    }
	    catch (Exception e) {
		LOG.debug("init()", e);
		GrailsUtil.deepSanitize(e);
		if (e instanceof BeansException) {
		    throw (BeansException)e;
		}
	
		throw new BootstrapException("Error executing bootstraps", e);
	    }
	}

	/*
	  boolean x = true;
	  try {
	  while (x) {
	  // System.out.println(Bootstrap.class.getName() + ": " + Thread.currentThread() + " " + new java.util.Date());
	  Thread.sleep(10000);
	  }
	  } catch (Exception e) {
	  }*/

	// System.out.println("+++++++++++++++++++ " + Thread.currentThread().getContextClassLoader());
	// System.out.println("+++++++++++++++++++ " + this.getClass().getClassLoader());

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): thread classLoader " + Thread.currentThread().getContextClassLoader());
	}


	/*GrailsPluginManager pm = PluginManagerHolder.getPluginManager();
	  if (pm instanceof DefaultGrailsPluginManager) {
	  ((DefaultGrailsPluginManager) pm).startPluginChangeScanner();
	  }*/

	/*if (1 == 1) {
	    throw new Error("abc");
	    }*/
	LOG.debug("init(): end");
    }

    public void destroy() {
	LOG.debug("destroy(): begin");
	/*GrailsPluginManager pm = PluginManagerHolder.getPluginManager();
	  if (pm instanceof DefaultGrailsPluginManager) {
	  ((DefaultGrailsPluginManager) pm).stopPluginChangeScanner();
	  }*/

	GrailsApplication grailsApplication = webContext.getBean(GrailsApplication.APPLICATION_ID, GrailsApplication.class);
    
	GrailsClass[] bootstraps =  grailsApplication.getArtefacts(BootstrapArtefactHandler.TYPE);
	for (GrailsClass bootstrap : bootstraps) {
	    ((GrailsBootstrapClass) bootstrap).callDestroy();
	}


	{
	    ServletContextEvent event = new ServletContextEvent(servletContext);
	    for (ServletContextListener l : servletContextListeners) {
		l.contextDestroyed(event);
	    }
	}

	servletContext = null;	

	LOG.debug("destroy(): end");
    }


    public static void main(String [] args) {
	// System.out.println(Bootstrap.class.getName()+ ".main() - begin " + new java.util.Date());
	if (LOG.isDebugEnabled()) {
	    LOG.debug("main(): begin " + Bootstrap.class.getName()+ " - " + new java.util.Date());
	}

	Bootstrap r = new Bootstrap();
	r.init(args);
	r.destroy();

	if (LOG.isDebugEnabled()) {
	    LOG.debug("main(): end " + Bootstrap.class.getName()+ " - " + new java.util.Date());
	}

	// System.out.println(Bootstrap.class.getName()+ ".main() - end " + new java.util.Date());
    }
}
