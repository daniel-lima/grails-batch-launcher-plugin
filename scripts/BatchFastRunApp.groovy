includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchRun.groovy")

target(default: "Runs a Grails batch application in fast launch/bootstrap mode") {
  binding.batch.pluginScannerEnabled = false
  binding.batch.useGroovyClassLoader = true
  _batchRunApp()
}
