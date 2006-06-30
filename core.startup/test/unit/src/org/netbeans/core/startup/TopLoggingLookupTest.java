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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.XMLFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * Checks the top logging delegates to handlers in lookup.
 */
public class TopLoggingLookupTest extends NbTestCase {
    private MyHandler handler;
    
    public TopLoggingLookupTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());

        MockServices.setServices();

        // initialize logging
        TopLogging.initialize();
    }


    protected void tearDown() throws Exception {
    }

    public void testLogOneLine() throws Exception {
        MockServices.setServices(MyHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        
        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public void testDeadlock78865() throws Exception {
        MockServices.setServices(AnotherThreadLoggingHandler.class);
        handler = Lookup.getDefault().lookup(MyHandler.class);
        assertNotNull("Handler found", handler);

        Logger.getLogger(TopLoggingTest.class.getName()).log(Level.INFO, "First visible message");

        assertEquals("[First visible message]", handler.logs.toString());
    }

    public static class MyHandler extends Handler {
        public List<String> logs = new ArrayList<String>();

        public void publish(LogRecord record) {
            logs.add(record.getMessage());
        }

        public void flush() {
            logs.add("flush");
        }

        public void close() throws SecurityException {
            logs.add("close");
        }

    }
    public static final class AnotherThreadLoggingHandler extends MyHandler
    implements Runnable {
        public AnotherThreadLoggingHandler() {
            Logger.global.info("in constructor before");
            RequestProcessor.getDefault().post(this).waitFinished();
            Logger.global.info("in constructor after");
        }
        public void run() {
            Logger.global.warning("running in parael");
        }

    }
}
