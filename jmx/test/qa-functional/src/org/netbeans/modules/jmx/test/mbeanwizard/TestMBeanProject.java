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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.mbeanwizard;

import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.JMXTestCase;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Select PROJECT_NAME_MBEAN_FUNCTIONAL_TEST project node,
 * then call the Test Project target.
 */
public class TestMBeanProject extends JMXTestCase {

    /** Need to be defined because of JUnit */
    public TestMBeanProject(String name) {
        super(name);
    }


    public static NbTestSuite suite() {
        
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new TestMBeanProject("finish"));
        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Functional test which selects the node PROJECT_NAME_MBEAN_FUNCTIONAL_TEST
     * and call the Test Project target.
     */
    public void finish() {
        testProject(PROJECT_NAME_MBEAN_FUNCTIONAL);
    }
}
