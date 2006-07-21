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

package org.netbeans.xtest.plugin.ide.test;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.ErrorManager;

/** Test of XTestErrorManager which should log error messages from IDE
 * and report them as test errors.
 */
public class XTestErrorManagerTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public XTestErrorManagerTest(String name) {
        super(name);
    }

    /** Create suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new XTestErrorManagerTest("testInformational"));
        suite.addTest(new XTestErrorManagerTest("testException"));
        suite.addTest(new XTestErrorManagerTest("testError"));
        suite.addTest(new XTestErrorManagerTest("testErrorInTest"));
        suite.addTest(new XTestErrorManagerTest("testErrorInTestExceptionRemembered"));
        suite.addTest(new XTestErrorManagerTest("testErrorWithFail"));
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }
    
    /** Test informational messages are not reported. */
    public void testInformational() {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new Exception("testInformational"));
    }
    
    /** Test exception severity messages are reported as error. */
    public void testException() {
        ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, new Exception("testException"));
    }
    
    /** Test error severity messages are reported as error. */
    public void testError() {
        ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("testError"));
    }
    
    /** Test error in the test precede exception from IDE. */
    public void testErrorInTest() {
        ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("testErrorInTest"));
        System.out.println("testError should add error");
        // this causes error in test which should be reported instead of above errors
        int i = 9/0;
    }
    
    /** Test exception from IDE should be remembered if cannot be logged in previous test case. */
    public void testErrorInTestExceptionRemembered() {
    }
    
    /** Test exception from IDE precede failure in test. */
    public void testErrorWithFail() {
        ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("testErrorWithFail1"));
        ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("testErrorWithFail2"));
        fail("Should report first error instead of this fail.");
    }
}