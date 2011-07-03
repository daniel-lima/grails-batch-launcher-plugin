import grails.test.AbstractCliTestCase
import static org.junit.Assert.*;

class BatchReloadAppTests extends AbstractCliTestCase {

    void testDefault() {
        File reloadFile = new File('.batch_reload')

        def verifyExec = {
            long lastModified = reloadFile.exists()? reloadFile.lastModified() : 0
            execute(['batch-reload-app'])
            assertEquals 0, waitForProcess()
            verifyHeader()

            // Make sure that the script was found.
            assertFalse output.contains('Script not found:')
            assertTrue reloadFile.exists()
            assertTrue reloadFile.lastModified() > lastModified
        }

        verifyExec()
        verifyExec()
    }
}
