/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.core.startup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


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
    public void testLoggingAnnotateException() throws Exception {
        Exception e = new Exception("One");
        Exceptions.attachMessage(e, "Two");

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "Three", e);

        String disk = readLog(true);
        if (!disk.contains("One") || !disk.contains("Two") || !disk.contains("Three")) {
            fail("There shall be One, Two, Three text in the log:\n" + disk);
        }
    }
    public void testLoggingLocalizedAnnotateException() throws Exception {
        Exception e = new Exception("One");
        Exceptions.attachLocalizedMessage(e, "Two");

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "Three", e);

        String disk = readLog(true);
        if (!disk.contains("One") || !disk.contains("Two") || !disk.contains("Three")) {
            fail("There shall be One, Two, Three text in the log:\n" + disk);
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
        System.err.println("Jardo");
        new IllegalStateException("Hi").printStackTrace();
        System.err.flush();

        if (handler != null) {
            handler.flush();
        }

        String disk = readLog(true);

        Matcher m = Pattern.compile("^Ahoj(.*)Jardo", Pattern.MULTILINE | Pattern.DOTALL).matcher(disk);
        assertTrue(disk, m.find());
        assertEquals("One group found", 1, m.groupCount());
        assertTrue("Non empty group: " + m.group(1) + "\n" + disk, m.group(1).length() > 0);
        char next = m.group(1).charAt(0);
        if (next != 10 && next != 13) {
            fail("Expecting 'Ahoj': index: " + 0 + " next char: " + (int)next + "text:\n" + disk);
        }
        
        Pattern p = Pattern.compile("IllegalStateException.*Hi");
        Matcher d = p.matcher(disk);
        if (!d.find()) {
            fail("Expecting exception: " + disk);
        }
    }
    
    public void testSystemErrPrintLnIsSentToLog() throws Exception {
        System.err.println("BEGIN");
        System.err.println("");
        System.err.println("END");
        System.err.flush();

        if (handler != null) {
            handler.flush();
        }

        String disk = readLog(true);
        Matcher m = Pattern.compile("BEGIN.*END", Pattern.MULTILINE | Pattern.DOTALL).matcher(disk);
        assertTrue("There is text between BEGINandEND\n" + disk, m.find());
        disk = m.group(0);
        disk = disk.replace('\n', 'n');
        disk = disk.replace('\r', 'r');

        if (org.openide.util.Utilities.isWindows()) {
            assertEquals("BEGINrnrnEND", disk);
        } else {
            assertEquals("BEGINnnEND", disk);
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
    
    public void testLetsTryToReportToABugInAWT() throws Exception {
        class R implements Runnable {
            public RuntimeException ex;
            public void run() {
                if (ex != null) {
                    throw ex;
                }
            }
        }

        R thrw = new R();
        thrw.ex = new IllegalStateException();
        
        SwingUtilities.invokeLater(thrw);
        
        R wai = new R();
        SwingUtilities.invokeAndWait(wai);
        

        String log = readLog(true);
        if (log.indexOf("IllegalStateException") == -1) {
            fail("There should be IllegalStateException:\n" + log);
        }
    }
    
    public void testLoggingFromRequestProcessor() throws Exception {
        Logger.getLogger("org.openide.util.RequestProcessor").setLevel(Level.ALL);

        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                
            }
        }).waitFinished();
        
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

    public void testAttachMessage() throws Exception { // #158906
        Exception e = new Exception("Help");
        String msg = "me please";
        Exception result = Exceptions.attachMessage(e, msg);
        assertSame(result, e);
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "background", e);
        String disk = readLog(true);
        assertTrue(disk, disk.contains("background"));
        assertTrue(disk, disk.contains("java.lang.Exception"));
        assertTrue(disk, disk.contains("Help"));
        assertTrue(disk, disk.contains("me please"));
    }

}
