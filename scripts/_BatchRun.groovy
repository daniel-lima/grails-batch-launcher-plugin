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
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.tools.shell.util.NoExitSecurityManager

if (binding.variables.containsKey("_grails_batch_run_package_called")) {
    return
}

_grails_batch_run_package_called = true

includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchInit.groovy")
includeTargets << grailsScript("_GrailsRun")

def props = binding.batch

def recompileThread = null
def killThread = null
def batchThread = null
def batchReload = false

def oldSecurityManager = null
def newSecurityManager = null

target(_batchRunApp: "") {
    depends(_batchStartLogging, _batchParseArguments)

    def autoRecompile = props.autoRecompile
    def autoRecompileFrequency = props.autoRecompileFrequency
    def autoReload = props.autoReload
    def autoReloadFrequency = props.autoReloadFrequency
    ant.echo("autoRecompile ${autoRecompile}")
    ant.echo("autoReload ${autoReload}")

    if (autoRecompile && (!recompileThread || !recompileThread.isAlive())) {
        def ant = new AntBuilder(ant.project) // To avoid concurrent access to AntBuilder
        def exec = {
            while (true) {
                _batchAutoRecompile()
                Thread.sleep(autoRecompileFrequency * 1000)
            }
        } as Runnable
    
        recompileThread = newDaemonThread(exec, "RecompileThread-" + (Thread.activeCount() + 1))
        recompileThread.start()
    }
  
    long sleepTime = (autoReload? autoReloadFrequency : autoRecompileFrequency) * 1000
    if (!killThread || !killThread.isAlive()) {
        def exec = {
            def ant = new AntBuilder(ant.project)  // To avoid concurrent access to AntBuilder
            def touchFile = autoReload? new File(grailsSettings.classesDir.path) : _createReloadFile(props)
            long lastModified = touchFile.lastModified()
      
            while (true) {
                Thread.sleep(sleepTime)
                long lastModified2 = touchFile.lastModified()
                if (lastModified2 > lastModified) {
                    lastModified = lastModified2
                    _batchKill(props)
                    batchReload = true
                }
            }
        } as Runnable
    
        killThread = newDaemonThread(exec, "KillThread-" + (Thread.activeCount() + 1))
        killThread.start()
    }

    while (true) {
        _preBatchRun(props)
        _batchRun(props)
        _postBatchRun(props)
        if (!props.infiniteLoop) {
            break;
        }

    
        ant.echo('App finished. ' + (autoReload?'':"Waiting for 'reload' command"))
        while (!batchReload) {
            Thread.sleep(sleepTime)
        }
    }
}



_batchRun = {myProps ->

    def exec = {

        ant.echo("Loading ${myProps.bootstrapClassName}")
        Class c = classLoader.loadClass(myProps.bootstrapClassName)
        c.main(myProps.args as String[])

    } as Runnable

    
    batchThread = newDaemonThread(exec)

    if (myProps.autoReload && myProps.infiniteLoop && !newSecurityManager) {
        oldSecurityManager = System.getSecurityManager()
        if (oldSecurityManager) {
            newSecurityManager = new NoExitSecurityManager(oldSecurityManager)
        } else {
            newSecurityManager = new NoExitSecurityManager()
        }
    }
    if (newSecurityManager) {
        ant.echo("disabling System.exit()")
        System.setSecurityManager(newSecurityManager)
    }

    batchThread.setContextClassLoader(classLoader)
    
    batchReload = false
    batchThread.start()
    try {
        while (batchThread && batchThread.isAlive() && !batchThread.interrupted()) {
            batchThread.join(myProps.autoReload? myProps.autoReloadFrequency * 1000: 0)
        }
    } catch (Exception e) {
        def handled = false
        if (InterruptedException.class.instanceOf(e) || java.nio.channels.ClosedByInterruptException.class.instanceOf(e)) {
            if (batchThread.interrupted()) {
                handled = true
                e.printStackTrace()
            } 
        } 

        if (!handled) {
            throw e
        }
    }

    if (newSecurityManager) {
        ant.echo("enabling System.exit()")
        System.setSecurityManager(oldSecurityManager)
    }

    batchThread = null
}



_batchKill = {myProps ->
    
    if (batchThread) {
        ant.echo("unloading batch app")
        while (batchThread && batchThread.isAlive() && !batchThread.interrupted()) {
            batchThread.interrupt()
            Thread.yield()
        }
        ant.echo("batch app unloaded")
    }
}

_preBatchRun = {myProps ->
    
    def pluginScannerEnabled = myProps.pluginScannerEnabled
    def useGroovyClassLoader = myProps.useGroovyClassLoader
    
    def grailsReloadLocation = System.getProperty("grails.reload.location")
    if (!grailsReloadLocation) {
        grailsReloadLocation = new java.io.File("").absolutePath
        System.setProperty("grails.reload.location", grailsReloadLocation)
    }
    
    def sourceDirs = [new java.io.File("").toURI().toURL()]
    resolveResources("file:${basedir}/grails-app/*").each {sourceDirs << it.file.toURI().toURL()}
    
    def classesDirs = [classesDir]
    try {
        classesDirs << pluginClassesDir
    } catch (MissingPropertyException e) {
        // Grails 1.2.5
        if (e.property != "pluginClassesDir") {
            throw e
        }
    }
    classesDirs = classesDirs.collect { it.toURI().toURL() }

    classLoader = null
    if (useGroovyClassLoader) {
        classLoader = new GroovyClassLoader(rootLoader)
        sourceDirs.each {
            classLoader.addURL(it)
        }
        classesDirs.each {
            classLoader.addURL(it)
        }
    } else {
        classesDirs.add(0, sourceDirs[0])
        classLoader = new URLClassLoader(classesDirs as URL[], rootLoader)
    }
    
    Thread.currentThread().setContextClassLoader classLoader
    PluginManagerHolder.pluginManager = null
    
    loadPlugins()
    if (pluginScannerEnabled) {
        startPluginScanner()
        ant.echo("plugin scanner started")
    }
}


_postBatchRun = {myProps ->

    if (myProps.pluginScannerEnabled) {
        stopPluginScanner()
        ant.echo("plugin scanner stoped")
    }
}

_createReloadFile = {myProps ->
    def file = new File(myProps.reloadFilename)
    if (!file.exists()) {
        file.createNewFile()
    }
    ant.attrib(file: file,hidden: true)
    return file
}

target(_batchReloadApp: "") {
    def file = _createReloadFile(props)
    file.setLastModified(System.currentTimeMillis())
}


target(_batchParseArguments: "Parse the arguments passed on the command line") {
    if (!props.args) {
        def argsSwitchFound = false
        props.args = []

        args?.tokenize().each {token ->
            def argsSwitch = token =~ "--?args"
            if (argsSwitch.matches()) {
                argsSwitchFound = true
            }
            else { 
                if (argsSwitchFound) {
                    props.args << token
                }
            }
        }
    }
}


target(_batchAutoRecompile: "") {
    depends(compilePlugins)
  
    def ant = new AntBuilder(ant.project)
    def classesDirPath = new File(grailsSettings.classesDir.path)
    ant.mkdir(dir:classesDirPath)
  
    profile("Compiling sources to location [$classesDirPath]") {
        try {
            String classpathId = "grails.compile.classpath"

            /* To avoid continuous resources.groovy recompilation. */
            ant.groovyc(destdir:classesDirPath,
                        classpathref:classpathId,
                        encoding:"UTF-8",
                        verbose: grailsSettings.verboseCompile,
                        listfiles: grailsSettings.verboseCompile,
                        excludes: "spring/resources.groovy",
                        compilerPaths.curry(classpathId))
      
            ant.groovyc(destdir:classesDirPath,
                        classpathref:classpathId,
                        encoding:"UTF-8",
                        verbose: grailsSettings.verboseCompile,
                        listfiles: grailsSettings.verboseCompile,
                        includes: "resources.groovy"
                       ) {
                src(path: "${basedir}/grails-app/conf/spring")
            }
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    
        classLoader.addURL(grailsSettings.classesDir.toURI().toURL())
        try {
            classLoader.addURL(grailsSettings.pluginClassesDir.toURI().toURL())
        } catch (MissingPropertyException e) {
            if (e.property != "pluginClassesDir") {
                throw e
            }
        }
    
        // If this is a plugin project, the descriptor is not included
        // in the compiler's source path. So, we manually compile it now.
        if (isPluginProject) compilePluginDescriptor(findPluginDescriptor(grailsSettings.baseDir))
    }
}


// Workaround for old Groovy parser
def newDaemonThread(Runnable r, String name = null) {
    Thread t = name != null? new Thread(r, name) : new Thread(r)
    t.daemon = true
    return t
}