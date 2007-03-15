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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.xtest.plugin.jvm;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** Test if JVMExecuteWatchdog really kills the test after timeout set
 * in testbag definition.
 *
 * @author Jiri Skrivanek
 */
public class JVMExecuteWatchdogTest extends NbTestCase {
    
    /** Create instance of test.
    * @param testName name of test case
    */
    public JVMExecuteWatchdogTest(String testName) {
        super(testName);
    }
    
    /** Create test suite.
    @return test suite.
    */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(JVMExecuteWatchdogTest.class);
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Dummy test case to be killed by watchdog.
    * @throws Exception
    */
    public void testWatchdog() throws Exception {
        Thread.sleep(120000);
    }
}
