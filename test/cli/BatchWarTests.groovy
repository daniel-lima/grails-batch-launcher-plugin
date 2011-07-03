import grails.test.AbstractCliTestCase
import static org.junit.Assert.*;

class BatchWarTests extends AbstractCliTestCase {

    void testDefault() {
        final String appName = 'batch-launcher'
        
        File targetDir = new File('target')
        File installDir = new File(targetDir, "${appName}")
        File warDir = new File(installDir, 'war')
        
        def verifyExec = {String env = null ->
            File tmpDir = File.createTempFile("${appName}", 'tmp', targetDir)
            tmpDir.delete()
            if (installDir.exists()) {
                assertTrue "[${env}] targetDir renamed to tmpDir", new File(targetDir, installDir.name).renameTo(tmpDir)
            }
            
            def x = [targetDir, installDir, warDir].collect{File f -> f.exists()? f.lastModified() : 0l}
            Collections.sort(x)
            
            long lastModified = x[x.size() - 1]
            
            def command = ['batch-war']
            if (env) {
                command.add(0, env)
            }
            
            execute(command)
            assertEquals 0, waitForProcess()
            verifyHeader()

            // Make sure that the script was found.
            assertFalse output.contains('Script not found:')

            assertTrue "[${env}] warDir exists", warDir.exists()
            assertTrue "[${env}] warDir is recent", warDir.lastModified() > lastModified

            Set files = null
            def checkFile = {String name->
                File f = new File(installDir, name)
                assertTrue "[${env}] file ${f.name} exists", files.contains(f)
                assertTrue "[${env}] file ${f.name} is recent", f.lastModified() > lastModified
            }

            files = new LinkedHashSet(installDir.listFiles() as List)
            checkFile("${appName}.bat")
            checkFile("${appName}.sh")
            checkFile("${appName}.jar")

            assertTrue warDir.listFiles().length > 0
        }
        
        verifyExec()
        verifyExec('dev')
        verifyExec('prod')
    }
}
