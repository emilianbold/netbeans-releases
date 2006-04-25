/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.core.startup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;


/**
 * Checks the behaviour of NetBeans logging support.
 */
public class TopLoggingTest extends NbTestCase {
    private ByteArrayOutputStream w;
    private Handler handler;
    private Logger logger;
    
    public TopLoggingTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

        w = new ByteArrayOutputStream() {
            public void write(byte[] b, int off, int len) {
                super.write(b, off, len);
            }

            public void write(byte[] b) throws IOException {
                super.write(b);
            }

            public void write(int b) {
                super.write(b);
            }

            public String toString() {
                handler.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }

        };

        handler = TopLogging.createStreamHandler(new PrintStream(getStream()));
        logger = Logger.getLogger("");
        Handler[] old = logger.getHandlers();
// do not remove default handlers from CLIOptions.initialize():
//        for (int i = 0; i < old.length; i++) {
//            logger.removeHandler(old[i]);
//        }
        logger.addHandler(handler);

        w.reset();

    }


    protected void tearDown() throws Exception {
    }

    protected ByteArrayOutputStream getStream() {
        return w;
    }

    public void testLogOneLine() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        Pattern p = Pattern.compile("INFO.*First visible message");
        Matcher m = p.matcher(getStream().toString());

        if (!m.find()) {
            fail("msg shall be logged: " + getStream().toString());
        }

        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }

    }
    public void testLogMultiLineIsPrintedWithoutTheWarningPrefix() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.WARNING, "Some info");
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "Second msg\nand its second line");

        String p = "\nSecond msg\nand its second line";
        if (getStream().toString().indexOf(p) == -1) {
            fail("msg shall be logged: " + getStream().toString());
        }

        String disk = readLog(true);

        if (disk.indexOf(p) == -1) {
            fail("msg shall be logged to file: " + disk);
        }

    }
    public void testLogLoggingMessagesEndsUpInMultipleFiles() throws Exception {
        StringBuffer sb = new StringBuffer();
        while(sb.length() < 1024) {
            sb.append("0123456789");
        }

        Logger l = Logger.getLogger(TopLoggingTest.class.getName());
        for (int i = 0; i < 2048; i++) {
            l.log(Level.WARNING, sb.toString() + " index: " + i);
            getStream().reset();
        }

        TopLogging.flush(false);

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());


        File log2 = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log.1");
        assertFalse("Currently we rotate just one file: " + log2, log2.canRead());

        TopLogging.close();
        // simulate new start
        TopLogging.flush(true);

        TopLogging.initialize();

        assertTrue("2 Log file exists: " + log, log.canRead());
        assertTrue("Restarrt creates new log file: " + log2, log2.canRead());

    }

    public void testCanInfluenceBehaviourBySettingALevelProperty() throws Exception {
        System.setProperty(TopLoggingTest.class.getName() + ".level", "100");
        LogManager.getLogManager().readConfiguration();

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testCanInfluenceBehaviourBySettingALevelPropertyOnParent() throws Exception {
        System.setProperty("ha.nu.level", "100");
        LogManager.getLogManager().readConfiguration();

        Logger.getLogger("ha.nu.wirta").log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testCanInfluenceBehaviourBySettingALevelPropertyOnExistingParent() throws Exception {
        System.setProperty("ha.nu.level", "100");

        Logger l = Logger.getLogger("ha.nu.wirta");

        LogManager.getLogManager().readConfiguration();

        l.log(Level.FINER, "Finer level msg");

        Pattern p = Pattern.compile("FINER.*Finer level msg");
        String disk = readLog(true);
        Matcher d = p.matcher(disk);

        if (!d.find()) {
            fail("msg shall be logged to file: " + disk);
        }
    }

    public void testSystemErrIsSentToLog() throws Exception {
        System.err.println("Ahoj");
        new IllegalStateException("Hi").printStackTrace();

        if (handler != null) {
            handler.flush();
        }

        String disk = readLog(true);

        int ah = disk.indexOf("Ahoj");
        if (ah == -1) {
            char next = disk.charAt(ah + 4);
            if (next != 10 && next != 13) {
                fail("Expecting 'Ahoj': index: " + ah + " next char: " + (int)next + "text:\n" + disk);
            }
        }
        
        Pattern p = Pattern.compile("IllegalStateException.*Hi");
        Matcher d = p.matcher(disk);
        if (!d.find()) {
            fail("Expecting exception: " + disk);
        }
    }

    public void testFlushHappensAfterFewSeconds() throws Exception {
        Logger l = Logger.getLogger(TopLoggingTest.class.getName());
        l.log(Level.INFO, "First visible message");

        Pattern p = Pattern.compile("INFO.*First visible message");
        Matcher m = p.matcher(getStream().toString());

        Matcher d = null;
        String disk = null;
        // at most in 10s the output should be flushed
        for (int i = 0; i < 30; i++) {
            disk = readLog(false);
            d = p.matcher(disk);
            if (!d.find()) {
                Thread.sleep(300);
            } else {
                return;
            }
        }

        fail("msg shall be logged to file: " + disk);
    }

    private String readLog(boolean doFlush) throws IOException {
        if (doFlush) {
            TopLogging.flush(false);
        }

        File log = new File(new File(new File(getWorkDir(), "var"), "log"), "messages.log");
        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        return new String(arr);
    }

}
