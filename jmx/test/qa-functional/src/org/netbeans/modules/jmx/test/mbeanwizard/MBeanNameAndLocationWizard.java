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

import javax.swing.JLabel;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.MBean;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create JMX MBean files.
 * Check :
 * - wizards default values
 * - wizards behavior
 * - wizards robustness
 */
public class MBeanNameAndLocationWizard extends MBeanWizardTestCase {
    
    private static final String STANDARD_MBEAN_NEW_FILE_DESCRIPTION =
            "\nCreates a new JMX Standard MBean.";
    private static final String MXBEAN_NEW_FILE_DESCRIPTION =
            "\nCreates a new JMX MXBean.";
    private static final String MBEAN_FROM_EXISTING_JAVA_CLASS_NEW_FILE_DESCRIPTION =
            "\nCreates a new JMX MBean from an existing Java Class.";
    private static final String STANDARD_MBEAN_WITH_METADATA_NEW_FILE_DESCRIPTION =
            "\nCreates a new JMX MBean that extends javax.management.StandardMBean class.";
    
    /** Need to be defined because of JUnit */
    public MBeanNameAndLocationWizard(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MBeanNameAndLocationWizard("createMBean1"));
        suite.addTest(new MBeanNameAndLocationWizard("createMBean2"));
        suite.addTest(new MBeanNameAndLocationWizard("createMBean3"));
        suite.addTest(new MBeanNameAndLocationWizard("createMBean4"));
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
     * StandardMBean
     */
    public void createMBean1() {
        
        System.out.println("==========  createMBean1  ==========");
        
        testNameAndLocationWizard(
                FILE_TYPE_STANDARD_MBEAN,
                STANDARD_MBEAN_DEFAULT_NAME);
    }
    
    /**
     * MXBean
     */
    public void createMBean2() {
        
        System.out.println("==========  createMBean2  ==========");
        
        testNameAndLocationWizard(
                FILE_TYPE_MXBEAN,
                MXBEAN_DEFAULT_NAME);
    }
    
    /**
     * MBean from existing java class
     */
    public void createMBean3() {
        
        System.out.println("==========  createMBean3  ==========");
        
        testNameAndLocationWizard(
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                MBEAN_FROM_EXISTING_JAVA_CLASS_DEFAULT_NAME);
    }
    
    /**
     * StandardMBean with metadata
     */
    public void createMBean4() {
        
        System.out.println("==========  createMBean4  ==========");
        
        testNameAndLocationWizard(
                FILE_TYPE_STANDARD_MBEAN_WITH_METADATA,
                STANDARD_MBEAN_WITH_METADATA_DEFAULT_NAME);
    }
    
    //========================= Test Wizard ==================================//
    
    /**
     * Test name and location wizard
     */
    private void testNameAndLocationWizard(
            String fileType,
            String mbeanDefaultClassName) {
        
        System.out.println("File type is " + fileType);
        
        MBean mbean = null;
        
        // New File wizard execution
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                fileType);
        // Check new file wizard components
        checkNewFileWizardComponents(nfwo);
        // Check new file wizard values
        checkNewFileWizardValues(nfwo, fileType);
        nfwo.next();
        
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(null, null);
        
        // Check name and location wizard components
        checkNameAndLocationWizardComponents(nfnlso, fileType, null);
        // Check name and location wizard default values
        mbean = new MBean(mbeanDefaultClassName, null, "",
                mbeanDefaultClassName + " Description", "", false,
                null, null, null);
        checkNameAndLocationWizardValues(
                nfnlso, fileType, PROJECT_NAME_MBEAN_FUNCTIONAL, mbean);
        
        // Update some values
        // Perform back/next actions
        // Check no data has been lost
        mbean = new MBean("MyNewClassName", null, PACKAGE_COM_FOO_BAR,
                "My New Description",
                PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME, true,
                null, null, null);
        updateNameAndLocationWizardValues(nfnlso, fileType, mbean);
        nfnlso.back();
        nfnlso.next();
        // Check name and location wizard components
        checkNameAndLocationWizardComponents(nfnlso, fileType, mbean);
        // Check name and location wizard values
        checkNameAndLocationWizardValues(
                nfnlso, fileType, PROJECT_NAME_MBEAN_FUNCTIONAL, mbean);
        if ( fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS) ||
                fileType.equals(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA) ) {
            nfnlso.next();
            nfnlso.back();
            // Check name and location wizard components
            checkNameAndLocationWizardComponents(nfnlso, fileType, mbean);
            // Check name and location wizard values
            checkNameAndLocationWizardValues(
                    nfnlso, fileType, PROJECT_NAME_MBEAN_FUNCTIONAL, mbean);
        }
        
        // Check warnings
        checkNameAndLocationWizardWarnings(nfnlso, fileType);
        
        nfnlso.cancel();
    }
    
    //========================= Check Wizard ==================================//
    
    /**
     * Check new file wizard components are enabled/disabled
     */
    private void checkNewFileWizardComponents(NewFileWizardOperator nfwo) {
        
        // Check text fields
        assertFalse(nfwo.txtDescription().isEditable());
        
        // Checks buttons
        assertFalse(nfwo.btBack().isEnabled());
        assertTrue(nfwo.btNext().isEnabled());
        assertFalse(nfwo.btFinish().isEnabled());
        assertTrue(nfwo.btCancel().isEnabled());
        assertFalse(nfwo.btHelp().isEnabled());
    }
    
    /**
     * Check new file wizard values
     */
    private void checkNewFileWizardValues(
            NewFileWizardOperator nfwo,
            String fileType) {
        
        String description = null;
        
        // Check New File Wizard description
        if (fileType.equals(FILE_TYPE_STANDARD_MBEAN)) {
            description = STANDARD_MBEAN_NEW_FILE_DESCRIPTION;
        } else if (fileType.equals(FILE_TYPE_MXBEAN)) {
            description = MXBEAN_NEW_FILE_DESCRIPTION;
        } else if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            description = MBEAN_FROM_EXISTING_JAVA_CLASS_NEW_FILE_DESCRIPTION;
        } else if (fileType.equals(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA)) {
            description = STANDARD_MBEAN_WITH_METADATA_NEW_FILE_DESCRIPTION;
        }
        assertEquals(description, nfwo.txtDescription().getDisplayedText());
        
    }
    
    /**
     * Check name and location wizard components are enabled/disabled
     */
    private void checkNameAndLocationWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            String fileType,
            MBean mbean) {
        
        // Check text fields
        assertTrue(nfnlso.txtObjectName().isEnabled());
        assertTrue(nfnlso.txtObjectName().isEditable());
        assertTrue(nfnlso.txtProject().isEnabled());
        assertFalse(nfnlso.txtProject().isEditable());
        assertTrue(nfnlso.txtCreatedFile().isEnabled());
        assertFalse(nfnlso.txtCreatedFile().isEditable());
        assertTrue(getTextFieldOperator(
                MBEAN_DESCRIPTION_TEXT_FIELD, nfnlso).isEnabled());
        assertTrue(getTextFieldOperator(
                MBEAN_DESCRIPTION_TEXT_FIELD, nfnlso).isEditable());
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            assertTrue(getTextFieldOperator(
                    CLASS_TO_WRAP_TEXT_FIELD, nfnlso).isEnabled());
            assertTrue(getTextFieldOperator(
                    CLASS_TO_WRAP_TEXT_FIELD, nfnlso).isEditable());
            assertTrue(getCheckBoxOperator(
                    OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX, nfnlso).isEnabled());
        }
        
        // Check buttons
        assertTrue(nfnlso.btBack().isEnabled());
        // MBean from existing java class
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            if (mbean == null || mbean.getClassToWrap() == "") {
                assertFalse(nfnlso.btNext().isEnabled());
            } else {
                assertTrue(nfnlso.btNext().isEnabled());
            }
            assertFalse(nfnlso.btFinish().isEnabled());
            assertTrue(getButtonOperator(CLASS_TO_WRAP_BROWSE_BUTTON, nfnlso).isEnabled());
        }
        // Standard MBean with metadata
        else if (fileType.equals(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA)) {
            assertTrue(nfnlso.btNext().isEnabled());
            assertFalse(nfnlso.btFinish().isEnabled());
        }
        // Standard MBean and MXBean
        else {
            assertFalse(nfnlso.btNext().isEnabled());
            assertTrue(nfnlso.btFinish().isEnabled());
        }
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
    }
    
    /**
     * Check name and location wizard values
     */
    private void checkNameAndLocationWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso,
            String fileType,
            String projectName,
            MBean mbean) {
        
        String createdFile = null;
        String interfaceFile = null;
        
        // Initialize created file and interface file values
        createdFile = mbean.getName() + ".java";
        if (fileType.equals(FILE_TYPE_MXBEAN)) {
            interfaceFile = mbean.getName() + "MXBean.java";
        } else {
            interfaceFile = mbean.getName() + "MBean.java";
        }
        
        // Check Name and Location Wizard title
        assertEquals("New " + fileType, nfnlso.getTitle());
        // Check Project value
        assertEquals(projectName, nfnlso.txtProject().getText());
        // Check Class Name value
        assertEquals(mbean.getName(), nfnlso.txtObjectName().getText());
        // Check Package value
        assertEquals(mbean.getPackage(), nfnlso.cboPackage().getSelectedItem());
        // Check Created File value
        assertTrue(nfnlso.txtCreatedFile().getText().endsWith(createdFile));
        // Check Interface File value
        assertTrue(getTextFieldContent(
                CREATED_FILE_TEXT_FIELD, nfnlso).endsWith(interfaceFile));
        // Check Description value
        assertEquals(mbean.getDescription(), getTextFieldContent(
                MBEAN_DESCRIPTION_TEXT_FIELD, nfnlso));
        
        // Check Class to Wrap if needed
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            assertEquals(mbean.getClassToWrap(), getTextFieldContent(
                    CLASS_TO_WRAP_TEXT_FIELD, nfnlso));
            if (mbean.isObjectWrappedAsMXBean()) {
                assertTrue(getCheckBoxOperator(
                        OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX, nfnlso).isSelected());
            } else {
                assertFalse(getCheckBoxOperator(
                        OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX, nfnlso).isSelected());
            }
        }
    }
    
    /**
     * Check name and location wizard warnings :
     * - unknown class to wrap
     * - default package
     */
    private void checkNameAndLocationWizardWarnings(
            NewJavaFileNameLocationStepOperator nfnlso,
            String fileType) {
        
        JLabel jl = null;
        
        // Check class to wrap warning message
        // This message precedes the default package warning message
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            // Set the class to wrap to empty string
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso, "");
            // Check warning message is displayed
            jl = getLabel(SPECIFY_CLASS_TO_WRAP_WARNING, nfnlso.getContentPane());
            assertNotNull(jl);
            // Check next and finish buttons are disabled
            assertFalse(nfnlso.btFinish().isEnabled());
            assertFalse(nfnlso.btNext().isEnabled());
            // Set the class to unknown java class
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso,
                    PACKAGE_COM_FOO_BAR + ".UnknownJavaClass");
            // Check warning message is displayed
            jl = getLabel(CLASS_TO_WRAP_DOES_NOT_EXIST_WARNING, nfnlso.getContentPane());
            assertNotNull(jl);
            // Check next and finish buttons are disabled
            assertFalse(nfnlso.btFinish().isEnabled());
            assertFalse(nfnlso.btNext().isEnabled());
            // Set the class to empty java class
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso,
                    PACKAGE_COM_FOO_BAR + "." + EMPTY_JAVA_CLASS_NAME);
            // Check warning messages are not displayed anymore
            jl = getLabel(SPECIFY_CLASS_TO_WRAP_WARNING, nfnlso.getContentPane());
            assertNull(jl);
            jl = getLabel(CLASS_TO_WRAP_DOES_NOT_EXIST_WARNING, nfnlso.getContentPane());
            assertNull(jl);
            // Check next button is not disabled anymore
            assertTrue(nfnlso.btNext().isEnabled());
        }
        
        // Check default package warning message
        // Set the package to empty string
        nfnlso.setPackage("");
        // Check warning message is displayed
        jl = getLabel(DEFAULT_PACKAGE_WARNING, nfnlso.getContentPane());
        assertNotNull(jl);
        // Set the package to com.foo.bar
        nfnlso.setPackage(PACKAGE_COM_FOO_BAR);
        // Check warning message is not displayed anymore
        jl = getLabel(DEFAULT_PACKAGE_WARNING, nfnlso.getContentPane());
        assertNull(jl);
    }
    
    /**
     * Update name and location wizard values
     */
    private void updateNameAndLocationWizardValues(
            NewJavaFileNameLocationStepOperator nfnlso,
            String fileType,
            MBean mbean) {
        
        nfnlso.setObjectName(mbean.getName());
        nfnlso.setPackage(mbean.getPackage());
        setTextFieldContent(MBEAN_DESCRIPTION_TEXT_FIELD, nfnlso, mbean.getDescription());
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso, mbean.getClassToWrap());
            setCheckBoxSelection(OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX,
                    nfnlso, mbean.isObjectWrappedAsMXBean());
        }
    }
}

