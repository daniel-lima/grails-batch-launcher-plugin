// configuration for plugin testing - will not be included in the plugin zip
 
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

grails.doc.authors='Daniel Henrique Alves Lima (text revised by Gislaine Fonseca Ribeiro and others)'
grails.doc.license='Apache License 2.0'

grails.doc.alias = [
  intro: "1. Introduction",
  quickStart: "1.1 Quick Start",
  bootstrapClasses: "2.1. Bootstrap Classes",
  bootstrapClasses: "2.2. Bootstrap Classes",
  logging: "2.3. Logging"
]


println "Hello world!"

/*Thread t = new Thread() {
  public void run() {
    println "Waiting..."
    Thread.sleep(10000)
  }
  }.start()*/