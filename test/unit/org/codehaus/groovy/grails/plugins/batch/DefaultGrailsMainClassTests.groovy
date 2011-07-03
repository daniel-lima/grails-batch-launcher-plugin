package org.codehaus.groovy.grails.plugins.batch

import grails.test.*

import org.codehaus.groovy.grails.plugins.batch.DefaultGrailsMainClass


class DefaultGrailsMainClassTests extends GrailsUnitTestCase {
    
    private GroovyClassLoader classLoader
    
    void testEmptyClass() {
        def mainClass = 'class Main {\
        }'
        mainClass = classLoader.parseClass(mainClass)
        DefaultGrailsMainClass grailsMainClass = new DefaultGrailsMainClass(mainClass)
        assertNotNull('initClosure is not null', grailsMainClass.initClosure)
        assertNotNull('runClosure is not null', grailsMainClass.runClosure)
        assertNotNull('destroyClosure is not null', grailsMainClass.destroyClosure)
        
        grailsMainClass.callInit()
        grailsMainClass.callRun([a:1])
        grailsMainClass.callDestroy()
    }
    
    
    void testRegularClass() {
        def mainClass = 'class Main {\
        String step = null;\
        def init = {-> step = "init";};\
        def run = {Map context -> step = "run";};\
        def destroy = {-> step = "destroy";};\
        }'
        mainClass = classLoader.parseClass(mainClass)
        DefaultGrailsMainClass grailsMainClass = new DefaultGrailsMainClass(mainClass)
        assertNotNull('initClosure is not null', grailsMainClass.initClosure)
        assertNotNull('runClosure is not null', grailsMainClass.runClosure)
        assertNotNull('destroyClosure is not null', grailsMainClass.destroyClosure)
        
        def mainInstance = grailsMainClass.referenceInstance 
        assertNull mainInstance.step 
        grailsMainClass.callInit()
        assertEquals 'init', mainInstance.step
        grailsMainClass.callRun([a:1])
        assertEquals 'run', mainInstance.step
        grailsMainClass.callDestroy()
        assertEquals 'destroy', mainInstance.step
    }
    
    
    @Override
    protected void setUp() {
        super.setUp()
        
        this.classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader?:this.class.classLoader)
    }
    
    
}
