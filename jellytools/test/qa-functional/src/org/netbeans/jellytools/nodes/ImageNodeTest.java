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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.ImageNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class ImageNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ImageNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new ImageNodeTest("testVerifyPopup"));
        suite.addTest(new ImageNodeTest("testOpen"));
        suite.addTest(new ImageNodeTest("testCut"));
        suite.addTest(new ImageNodeTest("testCopy"));
        suite.addTest(new ImageNodeTest("testDelete"));
        suite.addTest(new ImageNodeTest("testRename"));
        suite.addTest(new ImageNodeTest("testSaveAsTemplate"));
        suite.addTest(new ImageNodeTest("testProperties"));
        return suite;
    }
    
    protected static ImageNode imageNode = null;
    
    /** Find node. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(imageNode == null) {
            imageNode = new ImageNode(new SourcePackagesNode("SampleProject"),
                                      "sample1|image.gif"); // NOI18N
        }
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        imageNode.verifyPopup();
    }
    
    /** Test open */
    public void testOpen() {
        imageNode.open();
        new TopComponentOperator("image").close();  // NOI18N
    }
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        imageNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        imageNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete */
    public void testDelete() {
        imageNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test rename */
    public void testRename() {
        imageNode.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        imageNode.properties();
        Utils.closeProperties("image"); // NOI18N
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        imageNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
    
}
