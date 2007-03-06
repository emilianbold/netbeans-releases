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
package org.netbeans.jellytools.properties;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.properties.PropertySheetOperator.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PropertySheetOperatorTest extends JellyTestCase {
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new PropertySheetOperatorTest("testInvoke"));
        suite.addTest(new PropertySheetOperatorTest("testTblSheet"));
        suite.addTest(new PropertySheetOperatorTest("testGetDescriptionHeader"));
        suite.addTest(new PropertySheetOperatorTest("testGetDescription"));
        suite.addTest(new PropertySheetOperatorTest("testSortByName"));
        suite.addTest(new PropertySheetOperatorTest("testSortByCategory"));
        suite.addTest(new PropertySheetOperatorTest("testShowDescriptionArea"));
        suite.addTest(new PropertySheetOperatorTest("testHelp"));
        suite.addTest(new PropertySheetOperatorTest("testVerify"));
        // have to be the last
        suite.addTest(new PropertySheetOperatorTest("testClose"));
        return suite;
    }

    private static String SAMPLE_NODE_NAME = "SampleClass1.java";
    private static PropertySheetOperator pso;
     
    /** Open sample property sheet and create PropertySheetOperator */
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");  // NOI18N
        if(pso == null && !getName().equals("testInvoke")) {    // NOI18N
            // opens properties window
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            Node sampleClass1 = new Node(sample1, SAMPLE_NODE_NAME);
            new PropertiesAction().performAPI(sampleClass1);
            pso = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                                            SAMPLE_NODE_NAME);
        }
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertySheetOperatorTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Test of invoke method. */
    public void testInvoke() {
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        new Node(sample1, SAMPLE_NODE_NAME).select();
        PropertySheetOperator.invoke();
        new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, 
                                  SAMPLE_NODE_NAME).close();
    }
    
    /** Test of tblSheet method */
    public void testTblSheet() {
        pso.tblSheet();
    }
    
    /** Test of btHelp method */
    public void testBtHelp() {
        pso.btHelp();
    }
    
    /** Test of getDescriptionHeader method */
    public void testGetDescriptionHeader() {
        pso.tblSheet().selectCell(0, 0);
        String expected = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
        assertEquals("Wrong description header was found.", expected, pso.getDescriptionHeader());
    }
    
    /** Test of getDescription method */
    public void testGetDescription() {
        pso.tblSheet().selectCell(0, 0);
        String expected = Bundle.getString("org.openide.nodes.Bundle", "HINT_Properties");
        assertEquals("Wrong description was found.", expected, pso.getDescription());
    }
    
    /** Test of sortByName method */
    public void testSortByName() {
        int oldCount = pso.tblSheet().getRowCount();
        pso.sortByName();
        assertTrue("Sort by name failed.", oldCount > pso.tblSheet().getRowCount());
    }

    /** Test of sortByCategory method */
    public void testSortByCategory() {
        int oldCount = pso.tblSheet().getRowCount();
        pso.sortByCategory();
        assertTrue("Sort by category failed.", oldCount < pso.tblSheet().getRowCount());
    }
    
    /** Test of showDescriptionArea method */
    public void testShowDescriptionArea() {
        pso.showDescriptionArea();
        // try to find description header label
        Object label = pso.findSubComponent(new JLabelOperator.JLabelFinder());
        assertNull("Description area was not hidden.", label);
        pso.showDescriptionArea();
        // try to find description header label
        label = pso.findSubComponent(new JLabelOperator.JLabelFinder());
        assertNotNull("Description area was not shown.", label);
    }
    
    /** Test of help method */
    public void testHelp() {
        OptionsOperator oo = OptionsOperator.invoke();
        oo.switchToClassicView();
        // select root
        PropertySheetOperator pso = oo.getPropertySheet("");
        try {
            pso.help();
            new HelpOperator().close();
        } finally {
            oo.close();
        }
    }
    
    /** Test of verify method */
    public void testVerify() {
        pso.verify();
    }

    /** Close property sheet. */
    public void testClose() {
        pso.close();
    }
    
}
