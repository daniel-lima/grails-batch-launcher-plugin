import grails.test.AbstractCliTestCase
import static org.junit.Assert.*;

class BatchRunAppTests extends AbstractCliTestCase {

    void testDefault() {
        execute(['-Dbatch.infinite.loop=false','batch-run-app'])
        assertEquals 0, waitForProcess()
        verifyHeader()

        // Make sure that the script was found.
        assertFalse output.contains('Script not found:')
        assertTrue output.contains('Hello world!')
        assertTrue output.contains('autoRecompile true')
        assertTrue output.contains('autoReload false')
    }
}
