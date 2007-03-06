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
package org.netbeans.jellytools.actions;

import junit.textui.TestRunner;
/**
 * Test of org.netbeans.jellytools.actions.SaveAllAction.
 * @author Jiri.Skrivanek@sun.com
 */
public class SaveAllActionTest extends SaveActionTest {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public SaveAllActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        suite.addTest(new SaveAllActionTest("testPerformMenu"));
        suite.addTest(new SaveAllActionTest("testPerformAPI"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test performMenu method. */
    public void testPerformMenu() {
        new SaveAllAction().performMenu();
    }
    
    /** Test performAPI method. */
    public void testPerformAPI() {
        new SaveAllAction().performAPI();
    }
    
}
