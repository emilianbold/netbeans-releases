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

/** Test of org.netbeans.jellytools.nodes.HTMLNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class HTMLNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public HTMLNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new HTMLNodeTest("testVerifyPopup"));
        suite.addTest(new HTMLNodeTest("testOpen"));
        suite.addTest(new HTMLNodeTest("testCut"));
        suite.addTest(new HTMLNodeTest("testCopy"));
        suite.addTest(new HTMLNodeTest("testDelete"));
        suite.addTest(new HTMLNodeTest("testRename"));
        suite.addTest(new HTMLNodeTest("testSaveAsTemplate"));
        suite.addTest(new HTMLNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static HTMLNode htmlNode = null;
    
    /** Find node. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(htmlNode == null) {
            htmlNode = new HTMLNode(new SourcePackagesNode("SampleProject"),
                                    "sample1|html.html"); // NOI18N
        }
    }

    /** Test verifyPopup  */
    public void testVerifyPopup() {
        htmlNode.verifyPopup();
    }
    
    /** Test open */
    public void testOpen() {
        htmlNode.open();
        new EditorOperator("html").closeDiscard();  // NOI18N
    }
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        htmlNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        htmlNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete */
    public void testDelete() {
        htmlNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test rename */
    public void testRename() {
        htmlNode.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        htmlNode.properties();
        Utils.closeProperties("html"); // NOI18N
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        htmlNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
    
}
