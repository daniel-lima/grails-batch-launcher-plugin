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
props.autoRecompile = "false".equals(System.getProperty("disable.auto.recompile", "false"))? true: false
props.autoRecompileFrequency = 3
props.args = null
props.infiniteLoop = true
props.autoReload = "false".equals(System.getProperty("batch.disable.auto.reload", "false"))? true: false
props.autoReloadFrequency = 3
props.reloadFilename = ".batch_reload"
props.bootstrapJarExploded = "true".equals(System.getProperty("batch.bootstrap.jar.exploded", "false"))? true: false

target(_batchStartLogging: "Bootstrap logging") {
  /*ant.echo("props " + props)
  ant.echo("props " + binding.batch)
  ant.echo("props " + batch)*/

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