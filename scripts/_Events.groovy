includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchInstallTemplates.groovy")

eventStatusUpdate = { msg ->
  if (msg && msg.startsWith("Templates installed successfully")) {
    println ">>>>>>>>>>>>> ${msg} ${ant}"
    _batchInstallTemplates()
  }
}