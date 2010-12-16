if (binding.variables.containsKey("_grails_batch_install_templates_called")) {
  return
}

_grails_batch_install_templates_called = true

includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchInit.groovy")

target(_batchInstallTemplates: "") {
  depends(_batchStartLogging)
  targetDir = "${basedir}/src/templates/batch"
  //ant.echo "targetDir " + targetDir
  //def overwrite = ant.getProperty("overwrite.templates")
  //ant.echo "overwrite1 ${overwrite}"
  //ant.setProperty("overwrite.templates", "true")
  //ant.mkdir(dir: targetDir)
  //overwrite = ant.getProperty("overwrite.templates")
  //ant.echo "overwrite2 ${overwrite}"
  ant.copy(todir: targetDir) {
    fileset(dir: "${batchLauncherPluginDir}/src/templates/batch") {
      include(name: "*.bat")
      include(name: "*.sh")
    }
  }
}