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
package org.netbeans.jellytools;

import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestSuite;

/** Test FavoritesOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FavoritesOperatorTest extends JellyTestCase {

    public FavoritesOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new FavoritesOperatorTest("testInvoke"));
        suite.addTest(new FavoritesOperatorTest("testTree"));
        suite.addTest(new FavoritesOperatorTest("testVerify"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    private static FavoritesOperator favoritesOper;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        FavoritesOperator.invoke().close();
        favoritesOper = FavoritesOperator.invoke();
    }
    
    /**
     * Test of tree method.
     */
    public void testTree() {
        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        // has to make tab visible
        favoritesOper.tree();
    }
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        favoritesOper.verify();
        favoritesOper.close();
    }
}
