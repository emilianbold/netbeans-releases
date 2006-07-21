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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

/** Test of XTestIDEHandler which should log error messages from IDE
 * and report them as test errors.
 */
public class XTestIDEHandlerTest extends NbTestCase {
    
    /** Need to be defined because of JUnit */
    public XTestIDEHandlerTest(String name) {
        super(name);
    }

    /** Create test suite. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        // order is important
        suite.addTest(new XTestIDEHandlerTest("testNotSevere"));
        suite.addTest(new XTestIDEHandlerTest("testWarning"));
        suite.addTest(new XTestIDEHandlerTest("testSevere"));
        suite.addTest(new XTestIDEHandlerTest("testErrorInTest"));
        suite.addTest(new XTestIDEHandlerTest("testErrorInTestSevereRemembered"));
        suite.addTest(new XTestIDEHandlerTest("testSevereWithFail"));
        return suite;
    }
    
    /** Set up. */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }

    /** Test all messages with lower level than WARNING and all messages
     * without exception are not reported as error. */
    public void testNotSevere() {
        Logger.getAnonymousLogger().log(Level.WARNING, "Warning message without exception");
        Logger.getAnonymousLogger().log(Level.SEVERE, "Severe message without exception.");
        Logger.getAnonymousLogger().log(Level.INFO, "Info message with exception", new Exception("Info"));
    }
    
    /** Test WARNING messages are reported as error. */
    public void testWarning() {
        Logger.getAnonymousLogger().log(Level.WARNING, "Warning message with exception", new Exception("testWarning"));
    }
    
    /** Test SEVERE messages are reported as error. */
    public void testSevere() {
        Logger.getAnonymousLogger().log(Level.SEVERE, "Severe message with exception", new Exception("testSevere"));
    }

    /** Test error in the test precede exception from IDE. */
    public void testErrorInTest() {
        Logger.getAnonymousLogger().log(Level.SEVERE, "Severe message with exception", new Exception("testErrorInTest"));
        // this causes error in test which should be reported instead of above errors
        int i = 9/0;
    }

    /** Test exception from IDE should be remembered if cannot be logged in previous test case. */
    public void testErrorInTestSevereRemembered() {
    }
    
    /** Test severe exception precedes failure in test. */
    public void testSevereWithFail() {
        Logger.getAnonymousLogger().log(Level.SEVERE, "Severe message with exception", new Exception("testSevereWithFail1"));
        Logger.getAnonymousLogger().log(Level.SEVERE, "Severe message with exception", new Exception("testSevereWithFail12"));
        fail("Should report first severe exception instead of this fail.");
    }
}