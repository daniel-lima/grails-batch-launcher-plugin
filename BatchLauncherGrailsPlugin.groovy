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

/**
 * @author Daniel Henrique Alves Lima
 */
class BatchLauncherGrailsPlugin {
    // the plugin version
    def version = "0.5.6"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.2.5 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
      "grails-app/**/*",
      "scripts/PublishGooglecode.groovy",
      "scripts/Eclipse.groovy",
      "web-app/css/**/*",
      "web-app/images/**/*",
      "web-app/js/**/*",
      "web-app/WEB-INF/tld/**/*"
    ]

    // TODO Fill in these fields
    def author = "Daniel Henrique Alves Lima"
    def authorEmail = "email_daniel_h@yahoo.com.br"
    def title = 'Grails Batch Launcher Plugin'
    def description = '''\\
Plugin to load and run batch (non-web/webless) Grails application.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/batch-launcher"
    //def documentation = "https://code.google.com/p/grails-batch-launcher-plugin"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
