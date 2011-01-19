/*
 * Copyright 2010 the original author or authors.
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

if (binding.variables.containsKey("_grails_batch_clean_package_called")) {
  return
}

_grails_batch_clean_package_called = true

System.setProperty("grails.war.exploded", "true")

includeTargets << new File("${batchLauncherPluginDir}/scripts/_BatchInit.groovy")
includeTargets << grailsScript("_GrailsClean")
includeTargets << grailsScript("_GrailsWar")

target(_batchClean: "") {
  depends(cleanAll, configureWarName)
  
  def warDir = new File(warName.replace(".war", "/war"))
  def batchDir = warDir.parentFile

  ant.echo("batchDir ${batchDir}")

  if (batchDir.exists()) {
    ant.delete(dir:batchDir, failonerror:false)
  }
  
}