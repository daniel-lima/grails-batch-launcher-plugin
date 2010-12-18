/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * @author Daniel Henrique Alves Lima
 */
public class Bootstrap {

    public static final Log LOG = LogFactory.getLog(Bootstrap.class);
    //    public static final Logger LOGGER = Logger.getLogger(Bootstrap.class);

    private ServletContext servletContext;
    private ServletContextListener [] servletContextListeners;

    private WebApplicationContext webContext;
    
    public void init(String [] args) {
	LOG.debug("init(): begin");

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): this classLoader " + this.getClass().getClassLoader());
	    LOG.debug("init(): thread classLoader " + Thread.currentThread().getContextClassLoader());
	}

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): env " + Environment.getCurrent());
	}

	String resourcePath = null;
	switch(Environment.getCurrent()) {
	case PRODUCTION:
	    resourcePath = "war";
	    break;
	default:
	    resourcePath = "web-app";
	}

	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): resourcePath " + resourcePath);
	}

	servletContext = resourcePath != null? new MockServletContext(resourcePath): new MockServletContext();
	servletContext.setAttribute("args", args);

	servletContextListeners = new ServletContextListener[] {
	    new Log4jConfigListener(),
	    new GrailsContextLoaderListener()
	};
	
	try
	    {
		ServletContextEvent event = new ServletContextEvent(servletContext);
		for (ServletContextListener l : servletContextListeners) {
		    l.contextInitialized(event);
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

	    try {
		GrailsConfigUtils.executeGrailsBootstraps(application, webContext, servletContext);
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


	if (LOG.isDebugEnabled()) {
	    LOG.debug("init(): thread classLoader " + Thread.currentThread().getContextClassLoader());
	}


	LOG.debug("init(): end");
    }

    public void destroy() {
	LOG.debug("destroy(): begin");

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
	if (LOG.isDebugEnabled()) {
	    LOG.debug("main(): begin " + Bootstrap.class.getName()+ " - " + new java.util.Date());
	}

	Bootstrap r = new Bootstrap();
	r.init(args);
	r.destroy();

	if (LOG.isDebugEnabled()) {
	    LOG.debug("main(): end " + Bootstrap.class.getName()+ " - " + new java.util.Date());
	}
    }
}
