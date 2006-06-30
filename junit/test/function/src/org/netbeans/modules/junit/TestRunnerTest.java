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

package org.netbeans.modules.junit;

import org.openide.TopManager;
import junit.framework.*;
import junit.runner.*;
import java.util.*;
import java.io.*;
import junit.framework.*;

public class TestRunnerTest extends TestCase {

    public TestRunnerTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TestRunnerTest.class);

        return suite;
    }
    
    /** Test of getLoader method, of class org.netbeans.modules.junit.TestRunner. */
    public void testGetLoader() {
        System.out.println("testGetLoader");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addError method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddError() {
        System.out.println("testAddError");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of addFailure method, of class org.netbeans.modules.junit.TestRunner. */
    public void testAddFailure() {
        System.out.println("testAddFailure");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of endTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testEndTest() {
        System.out.println("testEndTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of startTest method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStartTest() {
        System.out.println("testStartTest");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of doRun method, of class org.netbeans.modules.junit.TestRunner. */
    public void testDoRun() {
        System.out.println("testDoRun");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of runFailed method, of class org.netbeans.modules.junit.TestRunner. */
    public void testRunFailed() {
        System.out.println("testRunFailed");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
    /** Test of start method, of class org.netbeans.modules.junit.TestRunner. */
    public void testStart() {
        System.out.println("testStart");
        
        // Add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    
}
