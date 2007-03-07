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
package org.netbeans.jellytools.modules.j2ee.nodes;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.nodes.J2eeServerNode
 */
public class J2eeServerNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public J2eeServerNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new J2eeServerNodeTest("testVerifyPopup"));
        suite.addTest(new J2eeServerNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static J2eeServerNode serverNode;
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(serverNode == null) {
            RuntimeTabOperator.invoke();
            serverNode = new J2eeServerNode("Sun");
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        serverNode.verifyPopup();
    }
    
    /** Test properties */
    public void testProperties() {
        serverNode.properties();
        final String SERVER_MANAGER = Bundle.getString(
                "org.netbeans.modules.j2ee.deployment.devmodules.api.Bundle",
                "TXT_ServerManager");
        new NbDialogOperator(SERVER_MANAGER).close();
    }
}
