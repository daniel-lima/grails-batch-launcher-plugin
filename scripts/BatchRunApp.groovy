includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchRun.groovy")

target(default: "Runs a Grails batch application") {
    depends(checkVersion, configureProxy, packageApp, parseArguments)
    _batchRunApp()
}
