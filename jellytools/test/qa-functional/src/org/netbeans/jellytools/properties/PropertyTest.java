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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.properties;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.properties.Property.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class PropertyTest extends JellyTestCase {
    
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
        suite.addTest(new PropertyTest("testGetName"));
        suite.addTest(new PropertyTest("testGetValue"));
        suite.addTest(new PropertyTest("testGetShortDescription"));
        suite.addTest(new PropertyTest("testOpenEditor"));
        suite.addTest(new PropertyTest("testSetDefaultValue"));
        suite.addTest(new PropertyTest("testGetRendererName"));
        suite.addTest(new PropertyTest("testCanEditAsText"));
        suite.addTest(new PropertyTest("testIsEnabled"));
        suite.addTest(new PropertyTest("testClose"));
        suite.addTest(new PropertyTest("testSetValue"));
        return suite;
    }
    
    private static Property property;
    private static Property propertyAllFiles;
    private static PropertySheetOperator pso;
    //"Name" property
    private static final String SAMPLE_PROPERTY_NAME = Bundle.getString(
                                                                "org.openide.loaders.Bundle",
                                                                "PROP_name");
    //  "All Files"
    private static final String ALL_FILES_LABEL = Bundle.getString("org.openide.loaders.Bundle",
                                                                   "PROP_files");
    private static final String SAMPLE_NODE_NAME = "SampleClass1.java";
    
    /** Open property sheet and find sample property. */
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
        if(property == null) {
            // opens properties window
            Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
            Node sampleClass1 = new Node(sample1, SAMPLE_NODE_NAME);
            new PropertiesAction().performAPI(sampleClass1);
            pso = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                                            SAMPLE_NODE_NAME);
            property = new Property(pso, SAMPLE_PROPERTY_NAME);
            propertyAllFiles = new Property(pso, ALL_FILES_LABEL);
        }
    }
    
    /** Clean up after each test case. */
    protected void tearDown() {
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public PropertyTest(String testName) {
        super(testName);
    }
    
    /** Test of getName method */
    public void testGetName() {
        assertEquals("Wrong property name.", SAMPLE_PROPERTY_NAME, property.getName());
    }
    
    /** Test of getValue method */
    public void testGetValue() {
        assertEquals("Wrong property value.", SAMPLE_NODE_NAME.replaceFirst("\\.java", ""), property.getValue());
    }

    /** Test of getShortDescription method */
    public void testGetShortDescription() {
        String desc = Bundle.getString("org.openide.loaders.Bundle", "HINT_name");
        assertEquals("Wrong property value.", desc, property.getShortDescription());
    }
    
    /** Test of openEditor method */
    public void testOpenEditor() {
        propertyAllFiles.openEditor();
        new JDialogOperator(ALL_FILES_LABEL).close();
    }
    
    /** Test of supportsCustomEditor method */
    public void testSupportsCustomEditor() {
        assertTrue("Wrong value from supportCustomEditor", property.supportsCustomEditor());
    }
    
    /** Test of setDefaultValue method */
    public void testSetDefaultValue() {
        // is is still needed? In UI there is no handle for it.
        property.setDefaultValue();
    }
    
    /** Test of getRendererName method */
    public void testGetRendererName() {
        assertEquals("Renderer", Property.STRING_RENDERER, property.getRendererName());
        Node folderNode = new Node(FilesTabOperator.invoke().getProjectNode("SampleProject"), "src"); //NOI18N
        new PropertiesAction().performAPI(folderNode);
        PropertySheetOperator pso1 = new PropertySheetOperator(
                PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT,
                "src"); // NOI18N
        // "Sort Mode"
        String sortModeLabel = Bundle.getString("org.openide.loaders.Bundle", "PROP_sort");
        Property p1 = new Property(pso1, sortModeLabel);
        assertEquals("Renderer", Property.COMBOBOX_RENDERER, p1.getRendererName());
        pso1.close();
    }
    
    /** Test of canEditAsText method */
    public void testCanEditAsText() {
        assertTrue("Property Encoding can be edited as text.", property.canEditAsText());
        assertFalse("Property All Files cannot be edited as text.", propertyAllFiles.canEditAsText());
    }
    
    /** Test of isEnabled method */
    public void testIsEnabled() {
        assertTrue("Property Encoding should be enabled.", property.isEnabled()); // NOI18N
        assertFalse("Property All Files should be disabled.", propertyAllFiles.isEnabled());
    }
    
    /** Close tested property sheet. */
    public void testClose() {
        pso.close();
    }
    
    /** Test of setValue method */
    public void testSetValue() {
        // opens properties window for Runtime|HTTP server node
        Node node = RuntimeTabOperator.invoke().getRootNode();
        // "HTTP Server
        String httpServerLabel = Bundle.getString("org.netbeans.modules.httpserver.Bundle", "LBL_HTTPServerSettings");
        node = new Node(node, httpServerLabel);
        new PropertiesAction().performAPI(node);
        PropertySheetOperator httpPso = new PropertySheetOperator(PropertySheetOperator.MODE_PROPERTIES_OF_ONE_OBJECT, httpServerLabel);
        try{
            // test boolean property
            
            // find "Show Grant Access Dialog" property
            String propertyName = Bundle.getString("org.netbeans.modules.httpserver.Bundle", "PROP_showGrantAccess");
            Property booleanProperty = new Property(httpPso, propertyName);
            String oldValue = booleanProperty.getValue();
            // set to false
            booleanProperty.setValue(1);
            boolean passed = booleanProperty.getValue().equalsIgnoreCase("false"); //NOI18N
            booleanProperty.setValue(oldValue);
            assertTrue("Setting value by index failed.", passed);
            // set to false
            booleanProperty.setValue("false");   // NOI18N
            passed = booleanProperty.getValue().equalsIgnoreCase("false"); //NOI18N
            booleanProperty.setValue(oldValue);
            assertTrue("Setting boolean value by string value failed.", passed);
            
            // test text property
            
            // "Port"
            String portLabel = Bundle.getString("org.netbeans.modules.httpserver.Bundle", "PROP_Port");
            Property portProperty = new Property(httpPso, portLabel);
            oldValue = portProperty.getValue();
            String expected = "8888"; // NOI18N
            portProperty.setValue(expected);
            String newValue = portProperty.getValue();
            portProperty.setValue(oldValue);
            assertEquals("Wrong property value was set.", expected, newValue);
        } finally {
            httpPso.close();
        }
    }
}
