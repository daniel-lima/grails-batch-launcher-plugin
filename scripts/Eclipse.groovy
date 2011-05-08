/*
 * Copyright 2011 the original author or authors.
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

target(default: 'Generates Eclipse project files') {
    
    def eclipseFiles = ['.project', '.classpath']
    def groovyEclipseFiles = []

    def checkGroovyEclipseFile = {f ->
        def gFile = new File("${f}.groovy")
        if (gFile.exists()) {
            throw new IllegalStateException("${gFile} already exists")
        }
        groovyEclipseFiles << gFile
    }

    def createEclipseFileBackup = {f ->
        def boolean isStsFile = false

        def file = new File(f)
        if (file.exists()) {
            new File(f).withReader{r->
                String line = null
                while (!isStsFile && (line = r.readLine()) != null) {
                    isStsFile = line.contains('.sts.')
                }
            }

            if (isStsFile) {
                ant.copy(file: "${f}", tofile: "${f}.sts", preservelastmodified: true, overwrite: false)
            }
            ant.move(file: "${f}", tofile: "${f}.backup", preservelastmodified: true, overwrite: true)
        }
    }

    for (cl in [checkGroovyEclipseFile, createEclipseFileBackup]) {
        for (f in eclipseFiles) {
            cl(f)
        }
    }

    
    def grailsHome = System.env["GRAILS_HOME"]
    def grailsVersion = grailsHome.tokenize('-')[1]
    def nl = System.properties['line.separator']
    def projectName = new File(System.properties['user.dir']).name

    new File('.project.groovy').withWriter {w ->
        w << "\
<?xml version=\"1.0\" encoding=\"UTF-8\"?>${nl}\
<projectDescription>${nl}\
	<name>test_eclipse</name>${nl}\
	<comment></comment>${nl}\
	<projects>${nl}\
	</projects>${nl}\
	<buildSpec>${nl}\
		<buildCommand>${nl}\
			<name>org.eclipse.jdt.core.javabuilder</name>${nl}\
			<arguments>${nl}\
			</arguments>${nl}\
		</buildCommand>${nl}\
	</buildSpec>${nl}\
	<natures>${nl}\
		<nature>org.eclipse.jdt.groovy.core.groovyNature</nature>${nl}\
		<nature>org.eclipse.jdt.core.javanature</nature>${nl}\
	</natures>${nl}\
</projectDescription>"
    }

    new File('.classpath.groovy').withWriter{w->
        w << "\
<?xml version=\"1.0\" encoding=\"UTF-8\"?>${nl}\
<classpath>${nl}"
        
        for (d in ['src', 'grails-app', 'test']) {
            def dir = new File(d)
            if (dir.exists()) {
                for (file in dir.listFiles()) {
                    if (file.isDirectory() && !file.name.startsWith('.')) {
                        w << "    <classpathentry kind=\"src\" path=\"${dir.name}/${file.name}\"/>${nl}"
                    }
                }
            }
        }
        
        w << '	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>'
        new File("${grailsHome}/lib").eachFile {file->
            if (file.name.endsWith('.jar')) {
                w << "    <classpathentry kind=\"var\" path=\"GRAILS_HOME_${grailsVersion}/lib/${file.name}\"/>${nl}"
            }
        }
        new File("${grailsHome}/dist").eachFile {file->
            if (file.name.endsWith('.jar')) {
                def sourcePath = file.name.startsWith('grails-scripts')?'scripts': 'src'
                w << "   <classpathentry kind=\"var\" path=\"GRAILS_HOME_${grailsVersion}/dist/${file.name}\" sourcepath=\"/GRAILS_HOME_${grailsVersion}/${sourcePath}\"/>${nl}"
            }
        }
        
        w << "   <classpathentry kind=\"var\" path=\"DOT_GRAILS_HOME_${grailsVersion}/projects/${projectName}/plugin-classes\" sourcepath=\"/DOT_GRAILS_HOME_${grailsVersion}/projects/${projectName}/plugins\"/>${nl}\
<classpathentry kind=\"output\" path=\"web-app/WEB-INF/classes\"/>${nl}"
        w << '</classpath>'
    }

    groovyEclipseFiles.eachWithIndex {gFile, idx ->
        ant.copy(file: gFile, tofile: eclipseFiles[idx], overwrite: true)        
    }
}