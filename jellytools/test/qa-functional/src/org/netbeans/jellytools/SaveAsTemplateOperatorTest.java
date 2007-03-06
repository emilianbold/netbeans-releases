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

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.junit.NbTestSuite;

/** Test of org.netbeans.jellytools.SaveAsTemplateOperator.
 */
public class SaveAsTemplateOperatorTest extends JellyTestCase {

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new SaveAsTemplateOperatorTest("testInvoke"));
        suite.addTest(new SaveAsTemplateOperatorTest("testTree"));
        suite.addTest(new SaveAsTemplateOperatorTest("testLblSelectTheCategory"));
        suite.addTest(new SaveAsTemplateOperatorTest("testGetRootNode"));
        suite.addTest(new SaveAsTemplateOperatorTest("testSelectTemplate"));
        return suite;
    }
    
    protected void setUp() {
        System.out.println("### "+getName()+" ###");
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public SaveAsTemplateOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method. */
    public void testInvoke() {
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sampleClass1 = new Node(sample1, "SampleClass1.java");  // NOI18N
        SaveAsTemplateOperator.invoke(sampleClass1);
    }
    
    /** Test of tree method. */
    public void testTree() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        sato.tree();
    }
    
    /** Test of lblSelectTheCategory method. */
    public void testLblSelectTheCategory() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String labelText = sato.lblSelectTheCategory().getText();
        String expectedText = Bundle.getString("org.openide.loaders.Bundle",
                                               "CTL_SaveAsTemplate");
        assertEquals("Wrong label found.", expectedText, labelText);
    }
    
    /** Test of getRootNode method. */
    public void testGetRootNode() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String text = sato.getRootNode().getText();
        String expectedText = "Templates"; // NOI18N
        assertEquals("Wrong root node.", expectedText, text);
    }
    
    /** Test of selectTemplate method. */
    public void testSelectTemplate() {
        SaveAsTemplateOperator sato = new SaveAsTemplateOperator();
        String mainClass = Bundle.getString("org.netbeans.modules.java.project.Bundle",
                                               "Templates/Classes/Main.java");
        // "Java Classes|Java Main Class"
        String templatePath = Bundle.getString("org.netbeans.modules.java.project.Bundle", 
                                               "Templates/Classes")+
                              "|"+mainClass;
        sato.selectTemplate(templatePath);
        String selected = sato.tree().getSelectionPath().getLastPathComponent().toString();
        sato.close();
        assertEquals("Path \""+templatePath+"\" not selected.", mainClass, selected);
    }
}
