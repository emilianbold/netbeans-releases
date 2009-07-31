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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.test.mbeanwizard;

import java.util.ArrayList;
import javax.swing.JLabel;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create JMX MBean files.
 * Check :
 * - wizards default values
 * - wizards behavior
 * - wizards robustness
 */
public class MBeanAttributesWizard extends MBeanWizardTestCase {
    
    // Depending on the MBean type,
    // the wizard component names differ
    private String tableOperator = null;
    private String removeButton = null;
    private String accessBox = null;
    
    // When creating attributes with default values, the default name is set to
    // ATTRIBUTE_DEFAULT_NAME + an attribute creation counter
    // This counter is not reset when the attribute list is cleared
    private int numOfCreatedAttributes = 0;
    
    /** Need to be defined because of JUnit */
    public MBeanAttributesWizard(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MBeanAttributesWizard("createMBean1"));
        suite.addTest(new MBeanAttributesWizard("createMBean2"));
        return suite;
    }
    
    public void setUp() {
        // Select project node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL);
        // Initialize the wrapper java class
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                EMPTY_JAVA_CLASS_NAME);
    }
    
    //========================= JMX CLASS =================================//
    
    /**
     * MBean from existing java class
     */
    public void createMBean1() {
        
        System.out.println("==========  createMBean1  ==========");
        
        // Initialize private variables
        tableOperator = WRAPPER_ATTRIBUTE_TABLE;
        removeButton = WRAPPER_ATTRIBUTE_REMOVE_BUTTON;
        accessBox = WRAPPER_ATTRIBUTE_ACCESS_BOX;
        // Test attributes wizard
        testAttributesWizard(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS);
    }
    
    /**
     * StandardMBean with metadata
     */
    public void createMBean2() {
        
        System.out.println("==========  createMBean2  ==========");
        
        // Initialize private variables
        tableOperator = ATTRIBUTE_TABLE;
        removeButton = ATTRIBUTE_REMOVE_BUTTON;
        accessBox = ATTRIBUTE_ACCESS_BOX;
        // Test attributes wizard
        testAttributesWizard(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA);
    }
    
    //========================= Test Wizard ==================================//
    
    /**
     * Test attributes wizard
     */
    private void testAttributesWizard(String fileType) {

        System.out.println("File type is " + fileType);
        
        ArrayList<Attribute> attrList = null;
        JTableOperator jto = null;
        
        // New File wizard execution
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                fileType);
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(null, null);
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso,
                    PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME);
        }
        nfnlso.next();
        
        jto = getTableOperator(tableOperator, nfnlso);
        
        // Check attributes wizard components
        checkMBeanAttributesWizardComponents(nfnlso, null, fileType);
        // Check attributes wizard default values (empty attribute list)
        checkMBeanAttributes(jto, null);
        
        // Add default attributes values
        attrList = constructDefaultMBeanAttributes();
        updateMBeanAttributes(nfnlso, jto, attrList, fileType);
        // Check attributes wizard components
        checkMBeanAttributesWizardComponents(nfnlso, attrList, fileType);
        // Check attributes wizard default values
        checkMBeanAttributes(jto, attrList);
        
        // Add custom attributes values
        // Perform back/next actions
        // Check no data has been lost
        attrList = constructMBeanAttributes();
        updateMBeanAttributes(nfnlso, jto, attrList, fileType);
        nfnlso.back();
        nfnlso.next();
        // Check attributes wizard components
        checkMBeanAttributesWizardComponents(nfnlso, attrList, fileType);
        // Check attributes wizard values
        checkMBeanAttributes(jto, attrList);
        nfnlso.next();
        nfnlso.back();
        // Check attributes wizard components
        checkMBeanAttributesWizardComponents(nfnlso, attrList, fileType);
        // Check attributes wizard values
        checkMBeanAttributes(jto, attrList);
        
        // Add an attribute whose name starts with non capital letter
        // and contains forbidden characters
        // Check attribute name updated
        Attribute wrongAttribute = new Attribute("my.;Attribute/,:", "int", READ_WRITE, "");
        attrList = new ArrayList<Attribute>();
        attrList.add(wrongAttribute);
        updateMBeanAttributes(nfnlso, jto, attrList, fileType);
        // Check attributes wizard values
        Attribute goodAttribute = new Attribute("MyAttribute", "int", READ_WRITE, "");
        attrList = new ArrayList<Attribute>();
        attrList.add(goodAttribute);
        checkMBeanAttributes(jto, attrList);
        
        // Reset to empty attribute list
        updateMBeanAttributes(nfnlso, jto, null, fileType);
        // Check attributes wizard components
        checkMBeanAttributesWizardComponents(nfnlso, null, fileType);
        // Check attributes wizard values
        checkMBeanAttributes(jto, null);
        
        // Check warnings
        checkMBeanAttributesWizardWarnings(nfnlso, jto, fileType);
        
        nfnlso.cancel();
    }
    
    //========================= Check Wizard ==================================//
    
    /**
     * Check attributes wizard components are enabled/disabled
     */
    private void checkMBeanAttributesWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            ArrayList<Attribute> attrList,
            String fileType) {
        
        // Check attributes Wizard title
        assertEquals("New " + fileType, nfnlso.getTitle());
        
        // Check table
        assertNotNull(getTableOperator(tableOperator, nfnlso));
        assertTrue(getTableOperator(tableOperator, nfnlso).isEnabled());
        
        // Check buttons
        assertTrue(getButtonOperator(ATTRIBUTE_ADD_BUTTON, nfnlso).isEnabled());
        if (attrList == null || attrList.isEmpty()) {
            assertFalse(getButtonOperator(removeButton, nfnlso).isEnabled());
        } else {
            assertTrue(getButtonOperator(removeButton, nfnlso).isEnabled());
        }
        assertTrue(nfnlso.btBack().isEnabled());
        assertTrue(nfnlso.btNext().isEnabled());
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            assertFalse(nfnlso.btFinish().isEnabled());
        } else {
            assertTrue(nfnlso.btFinish().isEnabled());
        }
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
    }
    
    /**
     * Check attributes wizard warnings :
     * - same attribute name
     */
    private void checkMBeanAttributesWizardWarnings(
            NewJavaFileNameLocationStepOperator nfnlso,
            JTableOperator jto,
            String fileType) {
        
        // Add 2 attributes with the same name
        Attribute attribute = new Attribute("DuplicatedAttribute", "int", READ_WRITE, "");
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attribute);
        list.add(attribute);
        updateMBeanAttributes(nfnlso, jto, list, fileType);
        // Check warning message is displayed
        JLabel jl = getLabel(SAME_ATTRIBUTE_WARNING, nfnlso.getContentPane());
        assertNotNull(jl);
        // Check next and finish buttons are disabled
        assertFalse(nfnlso.btFinish().isEnabled());
        assertFalse(nfnlso.btNext().isEnabled());
        // Remove the first attribute with the same name
        jto.selectCell(jto.findCellRow("DuplicatedAttribute"),
                jto.findColumn(ATTRIBUTE_NAME_COLUMN_NAME));
        pressAndRelease(removeButton, nfnlso);
        waitNoEvent(5000);
        // Check warning message is not displayed anymore
        jl = getLabel(SAME_ATTRIBUTE_WARNING, nfnlso.getContentPane());
        assertNull(jl);
        // Check next and finish buttons are not disabled anymore
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            assertFalse(nfnlso.btFinish().isEnabled());
        } else {
            assertTrue(nfnlso.btFinish().isEnabled());
        }
        assertTrue(nfnlso.btNext().isEnabled());
    }
    
    /**
     * Update attributes wizard values
     */
    protected void updateMBeanAttributes(
            NbDialogOperator ndo,
            JTableOperator jto,
            ArrayList<Attribute> attrList,
            String fileType) {
        
        // First reset attribute table
        while (jto.getRowCount() != 0) {
            selectTableCell(tableOperator, ndo, 0, 0);
            pressAndRelease(removeButton, ndo);
            waitNoEvent(5000);
        }
        
        // Then add attributes
        addMBeanAttributes(ndo, attrList, fileType);
        
        // Finally update the Attribute objects with default values (when needed)
        // to use them at the checking values test step
        if (attrList != null) {
            String attrDefaultName = null;
            for (Attribute attribute : attrList) {
                
                attrDefaultName = ATTRIBUTE_DEFAULT_NAME + numOfCreatedAttributes;
                
                // Set default attribute name
                if (attribute.getName() == null) {
                    attribute.setName(attrDefaultName);
                }
                // Set default attribute type
                if (attribute.getType() == null) {
                    attribute.setType("java.lang.String");
                }
                // Set default attribute access
                if (attribute.getAccess() == null) {
                    attribute.setAccess(READ_WRITE);
                }
                // Set default attribute description
                if (attribute.getDescription() == null) {
                    attribute.setDescription(attrDefaultName + " Description");
                }
                numOfCreatedAttributes++;
            }
        }
    }
    
    private ArrayList<Attribute> constructDefaultMBeanAttributes() {
        Attribute attribute1 = new Attribute(null, null, null, null);
        Attribute attribute2 = new Attribute(null, null, null, null);
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attribute1);
        list.add(attribute2);
        return list;
    }
    
    private ArrayList<Attribute> constructMBeanAttributes() {
        Attribute attribute1 = new Attribute(
                MBEAN_ATTRIBUTE_NAME_1,
                "int", READ_ONLY,
                MBEAN_ATTRIBUTE_DESCRIPTION_1);
        Attribute attribute2 = new Attribute(
                MBEAN_ATTRIBUTE_NAME_2,
                "java.util.Date", READ_WRITE,
                MBEAN_ATTRIBUTE_DESCRIPTION_2);
        ArrayList<Attribute> list = new ArrayList<Attribute>();
        list.add(attribute1);
        list.add(attribute2);
        return list;
    }
}



