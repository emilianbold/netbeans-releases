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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.SaveAsTemplateOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.nodes.PropertiesNode
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @author Jiri.Skrivanek@sun.com
 */
public class PropertiesNodeTest extends JellyTestCase {
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public PropertiesNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new PropertiesNodeTest("testVerifyPopup"));
        suite.addTest(new PropertiesNodeTest("testOpen"));
        suite.addTest(new PropertiesNodeTest("testEdit"));
        suite.addTest(new PropertiesNodeTest("testCut"));
        suite.addTest(new PropertiesNodeTest("testCopy"));
        suite.addTest(new PropertiesNodeTest("testPaste"));
        suite.addTest(new PropertiesNodeTest("testDelete"));
        suite.addTest(new PropertiesNodeTest("testRename"));
        suite.addTest(new PropertiesNodeTest("testAddLocale"));
        suite.addTest(new PropertiesNodeTest("testSaveAsTemplate"));
        suite.addTest(new PropertiesNodeTest("testProperties"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    protected static PropertiesNode propertiesNode = null;
    
    /** Finds node before each test case. */
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
        if(propertiesNode == null) {
            propertiesNode = new PropertiesNode(new SourcePackagesNode("SampleProject"), "sample1|properties.properties");  // NOI18N
        }
    }
    
    /** Test verifyPopup */
    public void testVerifyPopup() {
        propertiesNode.verifyPopup();
    }
    
    /** Test open */
    public void testOpen() {
        propertiesNode.open();
        new TopComponentOperator("properties").close();
    }
    
    /** Test edit */
    public void testEdit() {
        propertiesNode.edit();
        new TopComponentOperator("properties").close();
    }
    
    /** Test paste */
    public void testPaste() throws Exception {
        // "default language"
        String defaultLabel = Bundle.getString("org.netbeans.modules.properties.Bundle", "LAB_defaultLanguage");
        new CopyAction().perform(new Node(propertiesNode, defaultLabel));
        final int i = propertiesNode.getChildren().length;
        propertiesNode.paste();
        // waits for a new node
        new Waiter(new Waitable() {
            public Object actionProduced(Object oper) {
                return propertiesNode.getChildren().length > i ? Boolean.TRUE : null;
            }
            public String getDescription() {
                return("Wait pasted node is present."); // NOI18N
            }
        }).waitAction(null);
        assertEquals(i+1, propertiesNode.getChildren().length);
        new SaveAllAction().performAPI();
    }
    
    /** Test addLocale  */
    public void testAddLocale() {
        propertiesNode.addLocale();
        // "New Locale"
        String newLocaleTitle = Bundle.getString("org.netbeans.modules.properties.Bundle", "CTL_NewLocaleTitle");
        new JDialogOperator(newLocaleTitle).close();
    }
    
    
    /** Test cut */
    public void testCut() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        propertiesNode.cut();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test copy */
    public void testCopy() {
        Object clipboard1 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        propertiesNode.copy();
        Utils.testClipboard(clipboard1);
    }
    
    /** Test delete */
    public void testDelete() {
        propertiesNode.delete();
        Utils.closeConfirmDialog();
    }
    
    /** Test rename */
    public void testRename() {
        propertiesNode.rename();
        Utils.closeRenameDialog();
    }
    
    /** Test properties */
    public void testProperties() {
        propertiesNode.properties();
        Utils.closeProperties("properties");
    }
    
    /** Test saveAsTemplate */
    public void testSaveAsTemplate() {
        propertiesNode.saveAsTemplate();
        new SaveAsTemplateOperator().close();
    }
}
