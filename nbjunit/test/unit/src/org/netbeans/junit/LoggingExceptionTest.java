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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Checks that we can do proper logging of exceptions.
 *
 * @author Jaroslav Tulach
 */
public class LoggingExceptionTest extends NbTestCase {
    private Throwable toThrow;
    
    public LoggingExceptionTest(String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    @Override
    protected void setUp() throws IOException {
        clearWorkDir();
    }

    @Override
    protected int timeOut() {
        return getName().contains("Time") ? 10000 : 0;
    }
    
    
    public void testLoggedExceptionIsPrinted() throws Exception {
        Exception ex = new IOException("Ahoj");
        LogRecord rec = new LogRecord(Level.WARNING, "Cannot process {0}");
        rec.setThrown(ex);
        rec.setParameters(new Object[] { "Jardo" });
        Logger.global.log(rec);
        
        File[] arr = getWorkDir().listFiles();
        assertEquals("One log file", 1, arr.length);
        String s = LoggingTest.readFile(arr[0]);
        
        if (s.indexOf("Ahoj") == -1) {
            fail("There needs to be 'Ahoj':\n" + s);
        }
        if (s.indexOf("Jardo") == -1) {
            fail("There needs to be 'Jardo':\n" + s);
        }
        if (s.indexOf("testLoggedExceptionIsPrinted") == -1) {
            fail("There needs to be name of the method:\n" + s);
        }
    }
    public void testLoggedExceptionIsPrintedWithTimeout() throws Exception {
        testLoggedExceptionIsPrinted();
    }
}