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
import groovy.lang.Closure;

import java.util.Map;

import org.codehaus.groovy.grails.commons.AbstractGrailsClass;

class DefaultGrailsMainClass extends AbstractGrailsClass {
    
    public static final String MAIN = "Main";

    public DefaultGrailsMainClass(Class<?> clazz) {
        super(clazz, MAIN);
    }

    @SuppressWarnings("serial")
    private static Closure BLANK_CLOSURE = new Closure(
            DefaultGrailsMainClass.class) {
        
        @SuppressWarnings("unused")
        public Object doCall(Object[] args) {
            return null;
        }
    };

    public Closure getInitClosure() {
        return getNamedClosure("init");
    }

    public Closure getRunClosure() {
        return getNamedClosure("run");
    }

    public Closure getDestroyClosure() {
        return getNamedClosure("destroy");
    }

    public void callInit() {
        Closure init = getInitClosure();
        if (init != null) {
            Environment.executeForCurrentEnvironment(init);
        }
    }

    public void callRun(Map<String, Object> context) {
        Closure run = getRunClosure();
        if (run != null) {
            run = run.curry(new Object[] { context });
            Environment.executeForCurrentEnvironment(run);
        }
    }

    public void callDestroy() {
        Closure destroy = getDestroyClosure();
        if (destroy != null) {
            Environment.executeForCurrentEnvironment(destroy);
        }
    }

    private Closure getNamedClosure(String name) {

        Object obj = getPropertyValueObject(name);
        if (obj instanceof Closure) {
            return (Closure) obj;
        }
        return BLANK_CLOSURE;

    }

}
