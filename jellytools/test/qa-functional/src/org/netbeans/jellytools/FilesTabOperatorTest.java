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

/** Test FilesTabOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FilesTabOperatorTest extends JellyTestCase {

    public FilesTabOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite();
        // suites have to be in particular order
        suite.addTest(new FilesTabOperatorTest("testInvoke"));
        suite.addTest(new FilesTabOperatorTest("testTree"));
        suite.addTest(new FilesTabOperatorTest("testGetProjectNode"));
        suite.addTest(new FilesTabOperatorTest("testVerify"));
        return suite;
    }
    
    /** Print out test name. */
    public void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    private static FilesTabOperator filesOper;
    
    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        FilesTabOperator.invoke().close();
        filesOper = FilesTabOperator.invoke();
    }
    
    /**
     * Test of tree method.
     */
    public void testTree() {
        RuntimeTabOperator.invoke();
        // has to make tab visible
        filesOper.tree();
    }
    
    /**
     * Test of getRootNode method.
     */
    public void testGetProjectNode() {
        filesOper.getProjectNode("SampleProject");   // NOI18N
    }
    
    /**
     * Test of verify method.
     */
    public void testVerify() {
        filesOper.verify();
    }
}
