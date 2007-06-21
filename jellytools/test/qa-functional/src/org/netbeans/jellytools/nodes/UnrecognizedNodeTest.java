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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.UnrecognizedNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class UnrecognizedNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public UnrecognizedNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new UnrecognizedNodeTest("testVerifyPopup"));
        suite.addTest(new UnrecognizedNodeTest("testOpen"));
        suite.addTest(new UnrecognizedNodeTest("testCut"));
        suite.addTest(new UnrecognizedNodeTest("testCopy"));
        suite.addTest(new UnrecognizedNodeTest("testDelete"));
        suite.addTest(new UnrecognizedNodeTest("testRename"));
        suite.addTest(new UnrecognizedNodeTest("testSaveAsTemplate"));
        suite.addTest(new UnrecognizedNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static UnrecognizedNode unrecognizedNode = null;
    
    /** Finds node before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        // find node
        if(unrecognizedNode == null) {
            unrecognizedNode = new UnrecognizedNode(new SourcePackagesNode("SampleProject"), "sample1|unrecognized");  // NOI18N
        }
    }
    
    /** Test verifyPopup  */
    public void testVerifyPopup() {
        unrecognizedNode.verifyPopup();
    }
    
    /** Test open */
    public void testOpen() {
        unrecognizedNode.open();
        new EditorOperator("unrecognized").closeDiscard();  // NOI18N
    }
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        unrecognizedNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        unrecognizedNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete  */
    public void testDelete() {
        unrecognizedNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test rename */
    public void testRename() {
        unrecognizedNode.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        unrecognizedNode.properties();
        Utils.closeProperties("unrecognized"); // NOI18N
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        unrecognizedNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}
