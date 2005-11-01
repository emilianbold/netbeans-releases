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

package org.netbeans.junit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import org.netbeans.junit.NbTestCase;

/** Checks that we can do proper logging.
 *
 * @author Jaroslav Tulach
 */
public class LoggingTest extends NbTestCase {
    
    public LoggingTest(String testName) {
    	super(testName);
    }
    
    
    public void testLogFileName() throws Exception {
        PrintStream ps = getLog("ahoj");
        File f = new File(getWorkDir(), "ahoj");
        assertEquals("Log file exists", true, f.exists());
    }
    public void testLogFileNameEqualsToNameOfTest() throws Exception {
        PrintStream ps = getLog();
        File f = new File(getWorkDir(), getName() + ".log");
        assertEquals("Log file exists", true, f.exists());
    }
}
