package org.codehaus.groovy.grails.plugins.batch;

import grails.util.Environment;
import groovy.lang.Closure;

import java.util.Map;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;

class DefaultGrailsLauncherClass extends AbstractGrailsClass {

    public DefaultGrailsLauncherClass(Class<?> clazz) {
        super(clazz, "Launcher");
    }

    @SuppressWarnings("serial")
    private static Closure BLANK_CLOSURE = new Closure(Bootstrap.class) {
        @Override
        public Object call(Object[] args) {
            return null;
        }
    };

    public Closure getRunClosure() {
        Object obj = getPropertyValueObject("run");
        if (obj instanceof Closure) {
            return (Closure) obj;
        }
        return BLANK_CLOSURE;
    }

    public void callRun(Map<String, Object> context) {
        Closure init = getRunClosure();
        if (init != null) {
            init = init.curry(new Object[] { context });
            Environment.executeForCurrentEnvironment(init);
        }
    }

}
