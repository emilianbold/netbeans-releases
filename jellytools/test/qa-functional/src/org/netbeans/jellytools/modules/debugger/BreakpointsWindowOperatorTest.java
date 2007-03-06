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

package org.netbeans.jellytools.modules.debugger;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/**
 *  Test of BreakpointsWindowOperator.
 *
 * @author Martin.Schovanek@sun.com
 */
public class BreakpointsWindowOperatorTest extends JellyTestCase {

    public BreakpointsWindowOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BreakpointsWindowOperatorTest("testInvoke"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /**
     * Test of invoke method
     */
    public void testInvoke() {
        BreakpointsWindowOperator bwo = BreakpointsWindowOperator.invoke();
        bwo.close();
    }
}
