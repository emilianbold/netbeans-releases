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
 * Checks that it is possible to log to console.
 */
public class TopLoggingNbLoggerConsoleTest extends TopLoggingTest {
    private static ByteArrayOutputStream w;
    private static PrintStream ps;
    static {
        final PrintStream OLD = System.err;
        System.setProperty("netbeans.logger.console", "true");
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
                TopLogging.flush(false);
                OLD.flush();

                String retValue;                
                retValue = super.toString();
                return retValue;
            }
        };

        ps = new PrintStream(w);
        System.setErr(ps);
    }


    public TopLoggingNbLoggerConsoleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        // initialize logging
        TopLogging.initialize();

        ps.flush();
        w.reset();
    }

    protected void tearDown() throws Exception {
    }

    protected ByteArrayOutputStream getStream() {
        return w;
    }

    public void testFlushHappensQuickly() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        Pattern p = Pattern.compile("INFO.*First visible message");
        Matcher m = p.matcher(getStream().toString("utf-8"));

        Matcher d = null;
        String disk = null;
        // console gets flushed at 500ms
        for (int i = 0; i < 4; i++) {
            disk = w.toString("utf-8"); // this one is not flushing
            d = p.matcher(disk);
            if (!d.find()) {
                Thread.sleep(300);
            } else {
                return;
            }
        }

        fail("msg shall be logged to file: " + disk);
    }

}
