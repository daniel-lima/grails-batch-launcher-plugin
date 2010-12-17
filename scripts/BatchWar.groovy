includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchWar.groovy")

target(default: "Runs a Grails batch application") {
    _batchWar()
}
