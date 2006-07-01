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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
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
public class TopLoggingOwnConfigClassTest extends NbTestCase {
    static File log;
    
    public TopLoggingOwnConfigClassTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();

        System.setProperty("netbeans.user", getWorkDirPath());

        log = new File(getWorkDir(), "own.log");

        System.setProperty("java.util.logging.config.class", Cfg.class.getName());

        // initialize logging
        TopLogging.initialize();
    }

    protected void tearDown() throws Exception {
    }


    public void testLogOneLine() throws Exception {
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.FINER, "First visible message");

        String content = readLog();
        if (content.indexOf("<!DOCTYPE") == -1) {
            fail("Content must be XML based: " + content);
        }

        if (content.indexOf("First vis") == -1) {
            fail("It must contain our log message: " + content);
        }
    }

    private String readLog() throws IOException {
        Handler[] ha = Logger.getLogger("").getHandlers();
        assertEquals("There is one handler", 1, ha.length);
        ha[0].flush();

        assertTrue("Log file exists: " + log, log.canRead());

        FileInputStream is = new FileInputStream(log);

        byte[] arr = new byte[(int)log.length()];
        int r = is.read(arr);
        assertEquals("all read", arr.length, r);
        is.close();

        return new String(arr);
    }

    public static final class Cfg extends Object {
        public Cfg() throws IOException {

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStreamWriter w = new OutputStreamWriter(os);
            w.write("handlers=java.util.logging.FileHandler\n");
            w.write(".level=100\n");
            w.write("java.util.logging.FileHandler.pattern=" + log.toString().replace('\\', '/') +"\n");
            w.close();

            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
            
        }
    }
}
