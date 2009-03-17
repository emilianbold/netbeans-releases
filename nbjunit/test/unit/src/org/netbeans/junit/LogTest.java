package org.netbeans.junit;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/** Is logging working OK?
 */
public class LogTest extends NbTestCase {
    private Logger LOG = Logger.getLogger("my.log.for.test");
    
    public LogTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.OFF;
    }
    
    public void testLogEnable() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LOG.setLevel(Level.FINEST);
        LOG.finest("Too finest message to be seen");
        assertEquals(seq.toString(), 0, seq.length());
    }


    public void testLogSurviveRemoval() throws Exception {
        CharSequence seq = Log.enable(LOG.getName(), Level.FINE);

        LogManager.getLogManager().readConfiguration();

        LOG.warning("Look msg");
        if (seq.toString().indexOf("Look msg") == -1) {
            fail(seq.toString());
        }
    }

}
