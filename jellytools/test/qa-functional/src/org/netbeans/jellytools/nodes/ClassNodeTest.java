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
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.actions.CompileAction;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.ClassNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class ClassNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ClassNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ClassNodeTest("testVerifyPopup"));
        suite.addTest(new ClassNodeTest("testCut"));
        suite.addTest(new ClassNodeTest("testCopy"));
        suite.addTest(new ClassNodeTest("testDelete"));
        suite.addTest(new ClassNodeTest("testSaveAsTemplate"));
        suite.addTest(new ClassNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static ClassNode classNode = null;

    /** Finds data node before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        // find class node
        if(classNode == null) { // NOI18N
            Node sampleClass1Node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
            MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
            statusTextTracer.start();
            new CompileAction().perform(sampleClass1Node);
            // wait status text "Building SampleProject (compile-single)"
            statusTextTracer.waitText("compile-single", true); // NOI18N
            // wait status text "Finished building SampleProject (compile-single).
            statusTextTracer.waitText("compile-single", true); // NOI18N
            statusTextTracer.stop();
            // create exactly (full match) and case sensitively comparing comparator to distinguish build and build.xml node
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            Node filesProjectNode = new FilesTabOperator().getProjectNode("SampleProject");
            filesProjectNode.setComparator(comparator);
            classNode = new ClassNode(filesProjectNode, "build|classes|sample1|SampleClass1.class"); // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        classNode.verifyPopup();
    }
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        classNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        classNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete */
    public void testDelete() {
        classNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        classNode.properties();
        Utils.closeProperties("SampleClass1.class");
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        classNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}
