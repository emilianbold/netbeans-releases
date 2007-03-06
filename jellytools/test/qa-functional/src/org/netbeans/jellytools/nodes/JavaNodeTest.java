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
package org.netbeans.jellytools.nodes;

import java.awt.Toolkit;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;

/** Test of org.netbeans.jellytools.nodes.JavaNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class JavaNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public JavaNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new JavaNodeTest("testVerifyPopup"));
        suite.addTest(new JavaNodeTest("testOpen"));
        suite.addTest(new JavaNodeTest("testCut"));
        suite.addTest(new JavaNodeTest("testCopy"));
        suite.addTest(new JavaNodeTest("testDelete"));
        suite.addTest(new JavaNodeTest("testSaveAsTemplate"));
        suite.addTest(new JavaNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static JavaNode javaNode = null;
    
    /** Finds node before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(javaNode == null) {
            javaNode = new JavaNode(new FilesTabOperator().getProjectNode("SampleProject"),
                                      "src|sample1|SampleClass1.java"); // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        javaNode.verifyPopup();
    }
    
    /** Test open */
    public void testOpen() {
        javaNode.open();
        new EditorOperator("SampleClass1.java").closeDiscard();  // NOI18N
    }
    
    /** Test cut  */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        javaNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        javaNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete */
    public void testDelete() {
        javaNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        javaNode.properties();
        Utils.closeProperties("SampleClass1.java"); // NOI18N
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        javaNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
    
}
