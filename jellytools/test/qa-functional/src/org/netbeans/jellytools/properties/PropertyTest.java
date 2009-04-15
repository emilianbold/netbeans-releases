/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.properties;

import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.OptionsOperator;
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
    
    
    public static final String[] tests = new String[] {
        "testGetName",
        "testGetValue",
        "testGetShortDescription",
        "testOpenEditor",
        "testSetDefaultValue",
        "testGetRendererName",
        "testCanEditAsText",
        "testIsEnabled",
        "testClose"/*,
        there is no print settings! "testSetValue"*/
    };
    
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
        return (NbTestSuite) createModuleTest(PropertyTest.class, 
        tests);
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
        openDataProjects("SampleProject");
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
    //TODO write a new setValue test
    /*
    public void testSetValue() {
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.switchToClassicView();
        // "IDE Configuration|System|Print Settings"
        String printSettingsPath = 
                Bundle.getString("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration")+"|"+
                Bundle.getString("org.netbeans.core.ui.resources.Bundle", "UI/Services/IDEConfiguration/System")+"|"+
                Bundle.getString("org.netbeans.core.ui.resources.Bundle", "Services/org-openide-text-PrintSettings.settings");
        PropertySheetOperator printPso = optionsOperator.getPropertySheet(printSettingsPath);
        try{
            // test boolean property
            
            // find "Wrap Lines" property
            Property booleanProperty = new Property(printPso, "Wrap Lines");
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
            
            Property portProperty = new Property(printPso, "Page Footer Format");
            oldValue = portProperty.getValue();
            String expected = "Page {1}"; // NOI18N
            portProperty.setValue(expected);
            String newValue = portProperty.getValue();
            portProperty.setValue(oldValue);
            assertEquals("Wrong property value was set.", expected, newValue);
        } finally {
            printPso.close();
        }
    }*/
}
