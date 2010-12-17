includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchRun.groovy")

target(default: "Reloads batch app") {
  _batchReloadApp()
}
