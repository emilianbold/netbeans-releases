/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Behaviour of logging with respect to console output.
 *
 * @author Jaroslav Tulach
 */
public class StdOutTest extends NbTestCase {
    static ByteArrayOutputStream os = new ByteArrayOutputStream();
    static {
        PrintStream ps = new PrintStream(os);
        System.setErr(ps);
        System.setOut(ps);
    }

    private Logger log;
    
    public StdOutTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        String n = getName();
        int inx = n.indexOf("Level");
        if (inx == -1) {
            return null;
        }
        return Level.parse(n.substring(inx + 5));
    }
    
    protected void setUp() throws Exception {
        os.reset();

        log = Logger.getLogger(getName());
    }


    public void testNullLoggingPrintsToConsole() {
        log.warning("Ahoj");
        if (os.toString().indexOf("Ahoj") == -1) {
            fail("Should log: " + os);
        }
    }
    public void testNoConsoleOnLevelWARNING() {
        log.warning("Ahoj");
        if (os.toString().indexOf("Ahoj") != -1) {
            fail("Should log: " + os);
        }
    }
}

