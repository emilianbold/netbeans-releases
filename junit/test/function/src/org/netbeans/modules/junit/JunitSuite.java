/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;
import junit.framework.*;

public class JunitSuite extends TestCase {

    public JunitSuite(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        //--JUNIT:
        //This block was automatically generated and can be regenerated again.
        //Do NOT change lines enclosed by the --JUNIT: and :JUNIT-- tags.
        TestSuite suite = new TestSuite("JunitSuite");
        suite.addTest(org.netbeans.modules.junit.RunTestActionTest.suite());
        suite.addTest(org.netbeans.modules.junit.TestsActionTest.suite());
        suite.addTest(org.netbeans.modules.junit.JUnitSettingsBeanInfoTest.suite());
        suite.addTest(org.netbeans.modules.junit.JUnitProgressTest.suite());
        suite.addTest(org.netbeans.modules.junit.TestRunnerTest.suite());
        suite.addTest(org.netbeans.modules.junit.CreateTestActionTest.suite());
        suite.addTest(org.netbeans.modules.junit.JUnitCfgOfCreateTest.suite());
        suite.addTest(org.netbeans.modules.junit.TestCreatorTest.suite());
        suite.addTest(org.netbeans.modules.junit.JUnitSettingsTest.suite());
        suite.addTest(org.netbeans.modules.junit.OpenTestActionTest.suite());
        //:JUNIT--
        //This value MUST ALWAYS be returned from this function.
        return suite;
        
}
    
}
