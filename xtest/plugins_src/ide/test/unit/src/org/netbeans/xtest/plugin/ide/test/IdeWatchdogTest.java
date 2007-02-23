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

package org.netbeans.xtest.plugin.ide.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import org.netbeans.junit.NbTestCase;
import org.netbeans.xtest.util.NativeKill;

/** Test of IdeWatchdog. First testbag satisfies that watchdog stays alive.
 * Second testbag checks that watchdog first clears previous one and
 * the test is not accidentally killed.
 */
public class IdeWatchdogTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public IdeWatchdogTest(String name) {
        super(name);
    }

    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Make the watchdog to not be finished at the end of testbag. It can 
     * happen when whole JVM crash.
     */
    public void testDoNotStopWatchdog() throws IOException {
        new File(System.getProperty("xtest.workdir"), "watchdog.killed").createNewFile();
    }
    
    /** This test is in second testbag. It should not be killed by previous watchdog. */
    public void testShouldNotBeKilled() throws InterruptedException {
        // timeout for prepare testbag is set to 60000. If prepare tesbag watchdog is
        // not finished, it should kill this test.
        Thread.sleep(60000);
    }

    /** This test is in second testbag. It tests NativeKill functionality. */
    public void testNativeKill() throws Exception {
        String xtestWorkdir = System.getProperty("xtest.workdir");
        File idePidFile = new File(xtestWorkdir, "ide.pid");
        LineNumberReader reader = new LineNumberReader(new FileReader(idePidFile));
        String line = reader.readLine();
        assertNotNull("Cannot read PID from file "+idePidFile, line);
        long pid = Long.parseLong(line);
        boolean result = NativeKill.dumpProcess(pid);
        assertTrue("NativeKill.dumpProcess("+pid+") failed.", result);
        /* cannot test killProcess because it kills the test prematurely
        // sleep a bit, so resources can be released
        Thread.sleep(2000);
        result = NativeKill.killProcess(pid);
        // it return false because it kills its own JVM
        assertFalse("NativeKill.killProcess("+pid+") failed.", result);
         */
    }
}
