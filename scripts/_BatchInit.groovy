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
import org.codehaus.groovy.grails.plugins.logging.Log4jConfig
import org.apache.log4j.LogManager

if (binding.variables.containsKey("_grails_batch_init_package_called")) {
    return
}

_grails_batch_init_package_called = true
binding.batch = [:]

def props = binding.batch
props.bootstrapClassName = "org.codehaus.groovy.grails.plugins.batch.Bootstrap"
props.pluginScannerEnabled = true
props.useGroovyClassLoader = false
props.autoRecompile = "false".equals(System.getProperty("disable.auto.recompile", "false"))
props.autoRecompileFrequency = 3
props.args = null
props.infiniteLoop = true
props.autoReload = "true".equals(System.getProperty("batch.auto.reload", "false"))
props.autoReloadFrequency = 3
props.reloadFilename = ".batch_reload"
props.bootstrapJarExploded = "true".equals(System.getProperty("batch.bootstrap.jar.exploded", "false"))

target(_batchStartLogging: "Bootstrap logging") {
    /*
  LogManager.resetConfiguration()
  if (config.log4j instanceof Closure) {
    profile("configuring log4j") {
      new Log4jConfig().configure(config.log4j)
    }
  }
  else {
    // setup default logging
    new Log4jConfig().configure()
    }*/
}