/*
 *
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
