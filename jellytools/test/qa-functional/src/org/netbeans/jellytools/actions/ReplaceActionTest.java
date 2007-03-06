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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test org.netbeans.jellytools.actions.ReplaceAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class ReplaceActionTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ReplaceActionTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ReplaceActionTest("testPerformMenu"));
        suite.addTest(new ReplaceActionTest("testPerformAPI"));
        suite.addTest(new ReplaceActionTest("testPerformShortcut"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static final String SAMPLE_CLASS_1 = "SampleClass1";
    private static final String replaceTitle = Bundle.getString("org.netbeans.editor.Bundle", "replace-title");
    private static EditorOperator eo;
    
    /** Opens sample class and finds EditorOperator instance */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(eo == null) {
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            JavaNode sampleClass1 = new JavaNode(sample1, SAMPLE_CLASS_1);
            sampleClass1.open();
            eo = new EditorOperator(SAMPLE_CLASS_1);
        }
    }

    /** Close open Replace dialog. */
    public void tearDown() {
        new NbDialogOperator(replaceTitle).close();
        // close editor after last test case
        if(getName().equals("testPerformShortcut")) {
            eo.close();
        }
    }
    
    /** Test performMenu */
    public void testPerformMenu() {
        new ReplaceAction().performMenu(eo);
    }
    
    /** Test performAPI */
    public void testPerformAPI() {
        new ReplaceAction().performAPI(eo);
    }
    
    /** Test performShortcut */
    public void testPerformShortcut() {
        new ReplaceAction().performShortcut(eo);
    }
    
}
