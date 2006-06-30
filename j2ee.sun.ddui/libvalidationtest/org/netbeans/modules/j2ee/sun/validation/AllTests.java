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
