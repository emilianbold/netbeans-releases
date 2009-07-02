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

import java.io.File;
import java.util.ArrayList;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.JMXTestCase;
import org.netbeans.modules.jmx.test.helpers.Operation;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Starting class for mbeanwizard tests.
 */
public class MBeanWizardTestCase extends JMXTestCase {
    
    // Default wizard generated names
    protected static final String STANDARD_MBEAN_DEFAULT_NAME = "NewMBeanImpl";
    protected static final String MXBEAN_DEFAULT_NAME = "NewMXBeanImpl";
    protected static final String MBEAN_FROM_EXISTING_JAVA_CLASS_DEFAULT_NAME =
            "NewFromExistingClass";
    protected static final String STANDARD_MBEAN_WITH_METADATA_DEFAULT_NAME =
            "NewStandardMBeanImpl";
    protected static final String ATTRIBUTE_DEFAULT_NAME = "NewAttribute";
    protected static final String OPERATION_DEFAULT_NAME = "newOperation";
    protected static final String PARAMETER_DEFAULT_NAME = "parameter";
    protected static final String EXCEPTION_DEFAULT_CLASS = "java.lang.Exception";
    
    // Java class names
    protected static final String EMPTY_JAVA_CLASS_NAME = "EmptyJavaClass";
    protected static final String ATTRIBUTE_WRAPPER_NAME_1 = "AttributeWrapper1";
    protected static final String ATTRIBUTE_WRAPPER_NAME_2 = "AttributeWrapper2";
    protected static final String OPERATION_WRAPPER_NAME_1 = "OperationWrapper1";
    protected static final String OPERATION_WRAPPER_NAME_2 = "OperationWrapper2";
    
    // MBean attributes names and descriptions
    protected static final String MBEAN_ATTRIBUTE_NAME_1 = "FirstAttribute";
    protected static final String MBEAN_ATTRIBUTE_DESCRIPTION_1 =
            "First Attribute description";
    protected static final String MBEAN_ATTRIBUTE_NAME_2 = "SecondAttribute";
    protected static final String MBEAN_ATTRIBUTE_DESCRIPTION_2 =
            "Second Attribute description";
    
    // MBean operations names and descriptions
    protected static final String MBEAN_OPERATION_NAME_1 = "FirstOperation";
    protected static final String MBEAN_OPERATION_DESCRIPTION_1 =
            "First Operation Description";
    protected static final String MBEAN_OPERATION_NAME_2 = "SecondOperation";
    protected static final String MBEAN_OPERATION_DESCRIPTION_2 =
            "Second Operation Description";
    protected static final String MBEAN_OPERATION_NAME_3 = "ThirdOperation";
    protected static final String MBEAN_OPERATION_DESCRIPTION_3 =
            "Third Operation Description";
    protected static final String MBEAN_OPERATION_NAME_4 = "FourthOperation";
    protected static final String MBEAN_OPERATION_DESCRIPTION_4 =
            "Fourth Operation Description";
    
    // MBean parameters names and descriptions
    protected static final String MBEAN_PARAMETER_NAME_1 = "firstParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_1 =
            "First Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_2 = "secondParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_2 =
            "Second Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_3 = "thirdParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_3 =
            "Third Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_4 = "fourthParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_4 =
            "Fourth Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_5 = "fifthParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_5 =
            "Fifth Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_6 = "sixthParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_6 =
            "Sixth Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_7 = "seventhParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_7 =
            "Seventh Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_8 = "eighthParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_8 =
            "Eighth Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_9 = "ninethParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_9 =
            "Nineth Parameter Description";
    protected static final String MBEAN_PARAMETER_NAME_10 = "tenthParameter";
    protected static final String MBEAN_PARAMETER_DESCRIPTION_10 =
            "Tenth Parameter Description";
    
    // MBean exceptions names and descriptions
    protected static final String MBEAN_EXCEPTION_CLASS_1 =
            "java.lang.NullPointerException";
    protected static final String MBEAN_EXCEPTION_DESCRIPTION_1 =
            "First Exception description";
    
    
    /** Need to be defined because of JUnit */
    public MBeanWizardTestCase(String name) {
        super(name);
    }
    
    //========================= Initialization =================================//
    
    /**
     * Check if the wrapper java class has been already created
     * Otherwise, just create it
     */
    protected void initWrapperJavaClass(
            String projectName,
            String packageName,
            String className) {
        SourcePackagesNode spn = new SourcePackagesNode(projectName);
        if (spn.isChildPresent(packageName) &&
                (new Node(spn, packageName)).isChildPresent(className)) {
            System.out.println("Wrapper java class " + className + " already exists");
        } else {
            System.out.println("Create new wrapper java class " + className);
            createWrapperJavaClass(className);
        }
    }
    
    /**
     * Wrapper java class
     */
    protected void createWrapperJavaClass(String javaClassName) {
        // Create wrapper java file
        // Will be used when creating MBean from existing java file
        // New File wizard
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                FILE_CATEGORY_JAVA,
                FILE_TYPE_JAVA_CLASS);
        nfwo.next();
        // Name and Location wizard
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                javaClassName, PACKAGE_COM_FOO_BAR);
        nfnlso.finish();
    }
    
    //========================= WizardExecution =================================//
    
    /**
     * New file JMX wizard execution.
     * Depending on the JMX file type, the wizard execution will have different
     * behavior/fields.
     */
    protected void wizardExecution(String fileType, MBean mbean) {
        
        // New File wizard execution
        // -------------------------
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                FILE_CATEGORY_JMX,
                fileType);
        nfwo.next();
        
        // Name and Location wizard execution
        // ----------------------------------
        NewJavaFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                mbean.getName(),
                mbean.getPackage(),
                mbean.getDescription(),
                mbean.getClassToWrap(),
                mbean.isObjectWrappedAsMXBean());
        // Check Name and Location wizard
        System.out.println("Check name and location wizard");
        checkNameAndLocationWizard(nfnlso, mbean);
        // Get the generated files before switching to next wizard
        String mbeanCreatedClassFile = nfnlso.txtCreatedFile().getText();
        String mbeanCreatedInterfaceFile = getTextFieldContent(
                CREATED_FILE_TEXT_FIELD, nfnlso);
        
        // Attributes and Operations wizard execution
        // ------------------------------------------
        if ( fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS) ||
                fileType.equals(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA) ) {
            
            ArrayList<Attribute> attributes = mbean.getAttributes();
            ArrayList<Operation> operations = mbean.getOperations();
            
            nfnlso.next();
            // Check MBean attributes wizard
            System.out.println("Check MBean attributes wizard");
            checkMBeanAttributesWizard(nfnlso, fileType);
            // Add MBean attributes
            System.out.println("Add MBean attributes wizard");
            addMBeanAttributes(nfnlso, attributes, fileType);
            sleep(2000);
            
            nfnlso.next();
            // Check MBean operations wizard
            System.out.println("Check MBean operations wizard");
            checkMBeanOperationsWizard(nfnlso, fileType);
            // Add MBean operations
            System.out.println("Add MBean operations wizard");
            addMBeanOperations(nfnlso, operations, fileType);
            sleep(2000);
        }
        nfnlso.finish();
        
        // Check generated files
        System.out.println("Check created files");
        checkCreatedFiles(mbeanCreatedClassFile, mbeanCreatedInterfaceFile, mbean);
    }
    
    //==================MBean execution wizard methods ======================//
    
    /**
     * Add MBean attributes
     * The MBean attributes are added to the existing attribute list
     */
    protected void addMBeanAttributes(
            NbDialogOperator ndo,
            ArrayList<Attribute> attrList,
            String fileType) {
        
        String tableOperator = null;
        String accessBox = null;
        
        // Depending on the JMX file type, the wizard component names differ
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            tableOperator = WRAPPER_ATTRIBUTE_TABLE;
            accessBox = WRAPPER_ATTRIBUTE_ACCESS_BOX;
        } else {
            tableOperator = ATTRIBUTE_TABLE;
            accessBox = ATTRIBUTE_ACCESS_BOX;
        }
        
        JTableOperator jto = getTableOperator(tableOperator, ndo);
        super.addMBeanAttributes(ndo, jto, accessBox, attrList);
    }
    
    /**
     * Add MBean operations
     * The MBean operations are added to the existing operation list
     */
    protected void addMBeanOperations(
            NbDialogOperator ndo,
            ArrayList<Operation> opList,
            String fileType) {
        
        String tableOperator = null;
        
        // Depending on the JMX file type, the wizard component names differ
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            tableOperator = WRAPPER_OPERATION_TABLE;
        } else {
            tableOperator = OPERATION_TABLE_FROM_MENU;
        }
        
        JTableOperator jto = getTableOperator(tableOperator, ndo);
        super.addMBeanOperations(ndo, jto, OPERATION_ADD_BUTTON_FROM_MENU, opList);
    }
    
    //========================= Panel discovery ==================================//
    
    /**
     * Basic Name and Location wizard check.
     */
    protected void checkNameAndLocationWizard(
            NewJavaFileNameLocationStepOperator nfnlso, MBean mbean) {
        // Check description field
        assertEquals(mbean.getDescription(),
                getTextFieldContent(MBEAN_DESCRIPTION_TEXT_FIELD, nfnlso));
        // Check class to wrap field
        if (mbean.getClassToWrap() != null) {
            assertEquals(mbean.getClassToWrap(),
                    getTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD, nfnlso));
        }
        // Check object wrapped as MXBean field
        if (mbean.isObjectWrappedAsMXBean()) {
            assertTrue(getCheckBoxOperator(
                    OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX, nfnlso).isSelected());
        }
    }
    
    /**
     * Basic Attributes wizard check.
     */
    protected void checkMBeanAttributesWizard(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {
        
        String tableOperator = null;
        String removeButton = null;
        
        // Depending on the JMX file type, the wizard component names differ
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            // MBean from existing java class
            tableOperator = WRAPPER_ATTRIBUTE_TABLE;
            removeButton = WRAPPER_ATTRIBUTE_REMOVE_BUTTON;
        } else {
            // Standard MBean with metadata
            tableOperator = ATTRIBUTE_TABLE;
            removeButton = ATTRIBUTE_REMOVE_BUTTON;
        }
        
        assertTrue(getTableOperator(tableOperator, nfnlso).isEnabled());
        assertTrue(getButtonOperator(ATTRIBUTE_ADD_BUTTON, nfnlso).isEnabled());
        assertFalse(getButtonOperator(removeButton, nfnlso).isEnabled());
    }
    
    /**
     * Basic Operations wizard check.
     */
    protected void checkMBeanOperationsWizard(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {
        
        String tableOperator = null;
        String removeButton = null;
        
        // Depending on the JMX file type, the wizard component names differ
        if (fileType.equals(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS)) {
            // MBean from existing java class
            tableOperator = WRAPPER_OPERATION_TABLE;
            removeButton = WRAPPER_OPERATION_REMOVE_BUTTON;
        } else {
            // Standard MBean with metadata
            // Standard MBean with metadata
            // Standard MBean with metadata
            // Standard MBean with metadata
            tableOperator = OPERATION_TABLE_FROM_MENU;
            removeButton = OPERATION_REMOVE_BUTTON;
        }
        
        assertTrue(getTableOperator(tableOperator, nfnlso).isEnabled());
        assertTrue(getButtonOperator(OPERATION_ADD_BUTTON_FROM_MENU, nfnlso).isEnabled());
        assertFalse(getButtonOperator(removeButton, nfnlso).isEnabled());
    }
    
    
    /**
     * Check the created files with expected golden files.
     */
    protected void checkCreatedFiles(
            String mbeanCreatedClassFile,
            String mbeanCreatedInterfaceFile,
            MBean mbean) {
        
        String mbeanClassName = mbean.getName();
        String mbeanInterfaceName = mbeanCreatedInterfaceFile.substring(
                mbeanCreatedInterfaceFile.lastIndexOf(File.separatorChar) + 1,
                mbeanCreatedInterfaceFile.lastIndexOf('.'));
        
        assertTrue(compareFiles(new File(mbeanCreatedClassFile),
                getGoldenFile(mbeanClassName)));
        assertTrue(compareFiles(new File(mbeanCreatedInterfaceFile),
                getGoldenFile(mbeanInterfaceName)));
    }
}
