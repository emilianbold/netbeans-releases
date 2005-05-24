/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.validation;

import junit.framework.*;


/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */


public class AllTests{
    /* A class implementation comment can go here. */
    
    public static Test suite() {
        
        TestSuite suite = new TestSuite("Tools Test Suite");            //NOI18N
        
        //
        // Add one entry for each test class
        // or test suite.
        //
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.BundleReaderTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.DisplayTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.util.UtilsTest.class);
        suite.addTestSuite(
            org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintTest.class);
        ///suite.addTestSuite(
            ///org.netbeans.modules.j2ee.sun.validation.constraints.ConstraintUtilsTest.class);
        
        //
        // For a master test suite, use this pattern.
        // (Note that here, it's recursive!)
        //
        //suite.addTest(<ANOTHER_Test_Suite>.suite());
        
        return suite;
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}
