/*
 *
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


/*
 * JUnitTestRunnerLauncher.java
 *
 * Created on October 20, 2003, 6:28 PM
 */

package org.netbeans.xtest.plugin.jvm;

import org.netbeans.xtest.testrunner.JUnitTestRunner;

/**
 *
 * @author  mb115822
 */
public class JUnitTestRunnerLauncher {

    private static final boolean DEBUG = true;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // just start the tests\
        if (DEBUG) System.out.println("VM started");
        try {
            JUnitTestRunner testRunner = new JUnitTestRunner(null,System.out);
            testRunner.runTests();
        } catch (Throwable t) {
            System.out.println("Error - during test run caught exception: "+t.getMessage());
            t.printStackTrace();
            System.exit(-1);
        }
        if (DEBUG) System.out.println("VM finished");
        System.exit(0);
    }
    
}
