/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
