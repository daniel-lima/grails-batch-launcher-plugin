/*
 * Copyright 2010-2011 the original author or authors.
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

import grails.util.Environment;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.BootstrapArtefactHandler;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsBootstrapClass;
import org.codehaus.groovy.grails.commons.GrailsClass;
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor;
import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener;
import org.codehaus.groovy.grails.web.util.Log4jConfigListener;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Daniel Henrique Alves Lima
 */
public class Launcher {

    private static final String PROPERTY_PREFIX = "grails.plugins.batch.";

    private final String className = getClass().getName();
    private final Log log = LogFactory.getLog(getClass());

    private ServletContext servletContext;
    private ServletContextListener[] servletContextListeners;

    private final boolean logEnabled;

    private Thread shutdownHook;
    private boolean destroyed;

    private final String initLock = "init";
    private final String destroyLock = "destroy";

    public Launcher() {
        logEnabled = "true".equals(getSystemProperty("debugLauncher", "false"))
                || "true".equals(getSystemProperty("debugBootstrap", "false"));
    }

    public void init(String[] args) {
        logDebug(true, "init(): begin");
        synchronized (initLock) {

            logDebug(true, "init(): this classLoader ", this.getClass()
                    .getClassLoader());
            logDebug(true, "init(): thread classLoader ", Thread
                    .currentThread().getContextClassLoader());

            logDebug(true, "init(): env ", Environment.getCurrent());

            String resourcePath = getSystemProperty("resourcePath", null);
            if (resourcePath == null) {
                resourcePath = "war";
            }

            logDebug(true, "init(): resourcePath ", resourcePath);

            servletContext = resourcePath != null ? new MockServletContext(
                    resourcePath) : new MockServletContext();
            servletContext.setAttribute("args", args);

            servletContextListeners = new ServletContextListener[] {
                    new Log4jConfigListener(),
                    new GrailsContextLoaderListener() };

            this.shutdownHook = new Thread() {

                public void run() {
                    logDebug(true, "shutdown hook run():");
                    Launcher.this.destroy();
                }
            };

            this.destroyed = false;

            Runtime.getRuntime().addShutdownHook(this.shutdownHook);
            logDebug(true, "init(): shutdown hook added");

            try {
                ServletContextEvent event = new ServletContextEvent(
                        servletContext);
                for (ServletContextListener l : servletContextListeners) {
                    l.contextInitialized(event);
                }
            } catch (RuntimeException e) {
                log.error("init()", e);
                throw e;
            }

            logDebug("init(): thread classLoader ", Thread.currentThread()
                    .getContextClassLoader());

            GrailsApplication grailsApplication = ApplicationHolder
                    .getApplication();
            DefaultGrailsMainClass main = getMainClass(grailsApplication);

            if (main != null) {
                final Object instance = main.getReferenceInstance();

                WebApplicationContext webContext = (WebApplicationContext) grailsApplication
                        .getMainContext();
                webContext.getAutowireCapableBeanFactory()
                        .autowireBeanProperties(instance,
                                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                false);
                main.callInit();
            }

            logDebug("init(): end");
        }
    }

    public void run() {
        logDebug(true, "run(): begin");

        GrailsApplication application = ApplicationHolder.getApplication();
        DefaultGrailsMainClass main = getMainClass(application);
        logDebug("run(): main ", main);

        if (main != null) {
            executeMainClass(application, main);

        }

        logDebug(true, "run(): end");
    }

    public void destroy() {
        logDebug("destroy(): begin");
        synchronized (destroyLock) {

            if (!this.destroyed) {
                GrailsApplication grailsApplication = ApplicationHolder
                        .getApplication();

                if (grailsApplication != null) {
                    DefaultGrailsMainClass main = getMainClass(grailsApplication);
                    if (main != null) {
                        main.callDestroy();
                    }

                    GrailsClass[] bootstraps = grailsApplication
                            .getArtefacts(BootstrapArtefactHandler.TYPE);
                    for (int i = bootstraps.length - 1; i >= 0; i--) {
                        GrailsClass bootstrap = bootstraps[i];
                        ((GrailsBootstrapClass) bootstrap).callDestroy();
                    }
                }

                {
                    ServletContextEvent event = new ServletContextEvent(
                            servletContext);
                    for (int i = servletContextListeners.length - 1; i >= 0; i--) {
                        ServletContextListener l = servletContextListeners[i];
                        l.contextDestroyed(event);
                    }
                }

                if (shutdownHook != null) {
                    if (!shutdownHook.isAlive()) {
                        Runtime.getRuntime().removeShutdownHook(
                                this.shutdownHook);
                        logDebug(true, "destroy(): shutdown hook removed");
                    }
                    this.shutdownHook = null;
                }

                servletContext = null;
            }

            this.destroyed = true;
            logDebug(true, "destroy(): end");
        }
    }

    private void executeMainClass(GrailsApplication application,
            DefaultGrailsMainClass main) {
        Map<String, Object> context = new LinkedHashMap<String, Object>();
        for (@SuppressWarnings("rawtypes")
        Enumeration e = servletContext.getAttributeNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            Object value = servletContext.getAttribute(key);
            context.put(key, value);
        }

        WebApplicationContext webContext = (WebApplicationContext) application
                .getMainContext();

        PersistenceContextInterceptor interceptor = null;
        String[] beanNames = webContext
                .getBeanNamesForType(PersistenceContextInterceptor.class);
        if (beanNames.length > 0) {
            interceptor = (PersistenceContextInterceptor) webContext
                    .getBean(beanNames[0]);
        }

        if (interceptor != null) {
            interceptor.init();
        }

        try {
            main.callRun(context);

            if (interceptor != null) {
                interceptor.flush();
            }
        } finally {
            if (interceptor != null) {
                interceptor.destroy();
            }
        }
    }

    private DefaultGrailsMainClass getMainClass(
            GrailsApplication grailsApplication) {
        Class<?> mainClass = grailsApplication
                .getClassForName(DefaultGrailsMainClass.MAIN);
        DefaultGrailsMainClass grailsMainClass = null;

        if (mainClass != null) {
            grailsMainClass = new DefaultGrailsMainClass(mainClass);
        }

        return grailsMainClass;
    }

    private static String getSystemProperty(String propertyName,
            String defaultValue) {
        propertyName = PROPERTY_PREFIX + propertyName;
        String value = System.getProperty(propertyName);
        if (value != null && value.length() <= 0) {
            value = null;
        }

        return value != null ? value : defaultValue;
    }

    void logDebug(String message, Object... extra) {
        logDebug(false, message, extra);
    }

    void logDebug(boolean forceSysOut, String message, Object... extra) {
        if (logEnabled) {
            StringBuilder msg = new StringBuilder(message);
            for (Object x : extra) {
                if (x != null) {
                    msg.append(x.toString());
                } else {
                    msg.append("null");
                }
            }

            if (log.isDebugEnabled() && !forceSysOut) {
                log.debug(msg.toString());
            } else {
                System.out.print("[");
                System.out.print(className);
                System.out.print("] ");
                System.out.println(msg);
            }
        }
    }

    public static void main(String[] args) {
        Launcher r = new Launcher();
        r.logDebug(true, "main(): begin ", new java.util.Date());

        try {
            r.init(args);
            r.run();
        } finally {
            try {
                r.destroy();
            } finally {
                r.logDebug(true, "main(): end ", new java.util.Date());
            }
        }
    }
}
