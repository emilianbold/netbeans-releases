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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.CleanProjectAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class CompileActionTest extends JellyTestCase {

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CompileActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new CompileActionTest("testPerformPopup"));
        suite.addTest(new CompileActionTest("testPerformMenu"));
        suite.addTest(new CompileActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static Node node;
    private static MainWindowOperator.StatusTextTracer statusTextTracer;
    
    public void setUp() {
        if(node ==null) {
            node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        }
        if(statusTextTracer == null) {
            statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        }
        statusTextTracer.start();
    }

    public void tearDown() {
        // wait status text "Building SampleProject (compile-single)"
        statusTextTracer.waitText("compile-single", true); // NOI18N
        // wait status text "Finished building SampleProject (compile-single).
        statusTextTracer.waitText("compile-single", true); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test performPopup method. */
    public void testPerformPopup() {
        new CompileAction().performPopup(node);
    }
    
    /** Test performMenu method. */
    public void testPerformMenu() {
        new CompileAction().performMenu(node);
    }
    
    /** Test performShortcut method. */
    public void testPerformShortcut() {
        new CompileAction().performShortcut(node);
    }
    
}
