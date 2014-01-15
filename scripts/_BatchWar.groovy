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
if (binding.variables.containsKey("_grails_batch_war_package_called")) {
  return
}

_grails_batch_war_package_called = true

scriptEnv = "production"
System.setProperty("grails.war.exploded", "true")

includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchInit.groovy")
includeTargets << grailsScript("_GrailsWar")

def props = binding.batch

target(_batchWar: "") {
    def events = ['StatusFinal', 'CreateWarEnd']
    props.eventsToSuppress.addAll(events)
    
  war()
  
  props.eventsToSuppress.removeAll(events)
  
  def warDir = new File(warName.replace(".war", "/war"))
  def batchDir = warDir.parentFile
  def appName = grailsAppName
  def appVersion = metadata.getApplicationVersion()
  //def appName = batchDir.name
  def templatesSourceDir = new File("${basedir}/src/templates/batch")
  if (!templatesSourceDir.exists()) {
    templatesSourceDir = new File("${batchLauncherPluginDir}/src/templates/batch")
  }

  ant.mkdir(dir: batchDir.absolutePath)
  
  event('ReorganizeWarStart', [warDir.canonicalPath])

  def libDirName = "war/WEB-INF/lib"
  def classesDirName = "war/WEB-INF/classes"

  def bootstrapJarName = batchDir.name + ".jar"
  def stagingDir = grailsSettings.projectWarExplodedDir

  ant.copy(todir: warDir.absolutePath) {
    fileset(dir: stagingDir) {
      include(name: "**/*")
    }
  }

  ant.copy(todir: warDir.absolutePath + "/WEB-INF/lib") {
    fileset(dir: "${grailsHome}/lib/org.springframework/spring-test/jars") {
      include(name: "spring-test-*.jar")
    }
    
    fileset(dir: "${grailsHome}/lib/javax.servlet/javax.servlet-api/jars") {
       include(name: "javax.servlet-api-*.jar")
    }
    fileset(dir: "${grailsHome}/lib/javax.transaction/jta/jars") {
      include(name: "jta-*.jar")
    }
  }


  StringBuilder manifestClasspath = new StringBuilder()
  StringBuilder windowsLibClasspath = new StringBuilder("")
  StringBuilder linuxLibClasspath = new StringBuilder("")
  manifestClasspath.append(". ${classesDirName} war")

  new File("WEB-INF/lib", warDir).listFiles().each {
    manifestClasspath.append(" ${libDirName}/" + it.name)
    windowsLibClasspath.append("${libDirName}/" + it.name + ";")
    linuxLibClasspath.append("${libDirName}/" + it.name + ":")
  }

  if (!props.bootstrapJarExploded) {
    ant.jar(destfile: batchDir.absolutePath + "/" + bootstrapJarName, baseDir: warDir.absolutePath + "/WEB-INF/classes") {
      manifest {
	attribute(name: "Class-Path", value: manifestClasspath)
	attribute(name: "Main-Class", value: props.launcherClassName)
      }
    }

    ant.delete(dir: warDir.absolutePath + "/WEB-INF/classes") {
      include(name: "**/*")
    }

    windowsLibClasspath.delete(0, windowsLibClasspath.length())
    linuxLibClasspath.delete(0, linuxLibClasspath.length())
  }


  if (templatesSourceDir.exists()) {
    def filterTokens = [appName: appName, appVersion: appVersion, bootstrapJarName: bootstrapJarName, launcherClassName: props.launcherClassName, libDirName: libDirName, classesDirName: classesDirName, windowsLibClasspath: windowsLibClasspath, linuxLibClasspath: linuxLibClasspath]
    
    ant.copy(todir: batchDir.absolutePath, filtering: true) {
      fileset(dir: templatesSourceDir.absolutePath) {
	include(name: "*")
	exclude(name: "batch-launcher*")
      }
      filterset {
	filterTokens.each {
	  filter(token: it.key, value: it.value) 
	}
      }
    }

    ant.copy(todir: batchDir.absolutePath, filtering: true) {
      fileset(dir: templatesSourceDir.absolutePath) {
	include(name: "batch-launcher*")
      }
      globmapper(from: "batch-launcher*", to: appName + "*")
      filterset {
	filterTokens.each {
	  filter(token: it.key, value: it.value) 
	}
      }
    } 

    ant.chmod(dir: batchDir.absolutePath, perm: "u+x") {
      include(name: "*.sh")
    }
  }

  event('ReorganizeWarEnd', [warDir.canonicalPath])
  event('CreateWarEnd', [warName, stagingDir])
  event('StatusFinal', ["Done creating Reorganized WAR at ${warDir.canonicalPath}"])
  

}
