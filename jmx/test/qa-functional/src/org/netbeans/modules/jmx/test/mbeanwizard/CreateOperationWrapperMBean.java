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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.mbeanwizard;

import java.util.ArrayList;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.MBean;
import org.netbeans.modules.jmx.test.helpers.Operation;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;


/**
 * Create new JMX MBean files :
 * - MBean from existing java file
 * - MBean from existing java file wrapped as MXBean
 * The java file wrapper contains operations.
 */
public class CreateOperationWrapperMBean extends MBeanWizardTestCase {
    
    // MBean names
    private static final String OPERATION_WRAPPED_MBEAN_NAME_1 = "OperationWrappedMBean1";
    private static final String OPERATION_WRAPPED_MBEAN_NAME_2 = "OperationWrappedMBean2";
    private static final String OPERATION_WRAPPED_MBEAN_NAME_3 = "OperationWrappedMBean3";
    private static final String OPERATION_WRAPPED_MBEAN_NAME_4 = "OperationWrappedMBean4";
    private static final String OPERATION_WRAPPED_MBEAN_NAME_5 = "OperationWrappedMBean5";
    private static final String OPERATION_WRAPPED_MBEAN_NAME_6 = "OperationWrappedMBean6";
    
    // Operation names used when updating wrapper java class
    private static String OPERATION_1 = "operation1";
    private static String OPERATION_2 = "operation2";
    private static String OPERATION_3 = "operation3";
    private static String GENERIC_OPERATION_1 = "genericOperation1";
    private static String GENERIC_OPERATION_2 = "genericOperation2";
    private static String GENERIC_OPERATION_3 = "genericOperation3";
    
    // Depending on the MBean name, the attributes wizard execution will differ
    private String mbeanName = null;
    
    /** Need to be defined because of JUnit */
    public CreateOperationWrapperMBean(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean1"));
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean2"));
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean3"));
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean4"));
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean5"));
        suite.addTest(new CreateOperationWrapperMBean("createWrappedMBean6"));
        return suite;
    }
    
    public void setUp() {
        // Select project node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL);
        // Initialize the wrapper java classes
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                OPERATION_WRAPPER_NAME_1);
        initWrapperJavaClass(
                PROJECT_NAME_MBEAN_FUNCTIONAL,
                PACKAGE_COM_FOO_BAR,
                OPERATION_WRAPPER_NAME_2);
    }
    
    //========================= JMX CLASS =================================//
    
    /**
     * MBean from existing java class exposing all operations
     */
    public void createWrappedMBean1() {
        
        System.out.println("============  createWrappedMBean1  ============");
        
        String description = "MBean from existing java class with all operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_1;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class exposing no operations
     */
    public void createWrappedMBean2() {
        
        System.out.println("============  createWrappedMBean2  ============");
        
        String description = "MBean from existing java class with minimal operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_2;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class exposing generic operations
     */
    public void createWrappedMBean3() {
        
        System.out.println("============  createWrappedMBean3  ============");
        
        String description = "MBean from existing java class with generic operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_3;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean exposing all operations
     */
    public void createWrappedMBean4() {
        
        System.out.println("============  createWrappedMBean4  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with all operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_4;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * exposing no operations
     */
    public void createWrappedMBean5() {
        
        System.out.println("============  createWrappedMBean5  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with minimal operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_5;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * exposing generic operations
     */
    public void createWrappedMBean6() {
        
        System.out.println("============  createWrappedMBean6  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with generic operations";
        mbeanName = OPERATION_WRAPPED_MBEAN_NAME_6;
        MBean myMBean = new MBean(
                mbeanName,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, myMBean);
    }
    
    //========================= JAVA CLASS generation ===========================//
    
    /**
     * Wrapper java class
     */
    protected void createWrapperJavaClass(String javaClassName) {
        
        super.createWrapperJavaClass(javaClassName);
        
        // Select class node
        selectNode(PROJECT_NAME_MBEAN_FUNCTIONAL + "|" + SOURCE_PACKAGES + "|" +
                PACKAGE_COM_FOO_BAR + "|" + javaClassName);
        // Update the created java file
        updateWrapperJavaClass(javaClassName);
    }
    
    private void updateWrapperJavaClass(String fileName) {
        EditorOperator eo = new EditorOperator(fileName);
        
        // Update WrapperJavaClass1
        if (fileName.equals(OPERATION_WRAPPER_NAME_1)) {
            // Add extension to the java class
            eo.setCaretPosition("public class " + fileName + " ", false);
            eo.insert("extends Super" + fileName + " ");
            // Add methods to the java class
            // Set caret position after the first occurence of "}",
            // that is after the constructor declaration
            eo.setCaretPosition("}", false);
            eo.insert(addOperation1() +
                    addOperation2() +
                    addOperation3_String());
            // Add inner class declaration
            eo.insert("}\n\n");
            eo.insert("class Super" + fileName + " {\n" +
                    addOperation3_Object());
        }
        // Update WrapperJavaClass2
        else if (fileName.equals(OPERATION_WRAPPER_NAME_2)) {
            // Add generics to the java class declaration
            eo.setCaretPosition("public class " + fileName, false);
            eo.insert("<Z,Q> ");
            // Add methods to the java class
            // Set caret position after the first occurence of "}",
            // that is after the constructor declaration
            eo.setCaretPosition("}", false);
            eo.insert(addGenericOperation1() +
                    addGenericOperation2() +
                    addGenericOperation3());
        }
        
        eo.save();
    }
    
    private String addOperation1() {
        return  "\n\tpublic void " + OPERATION_1 + "() " +
                "throws java.lang.IllegalStateException {" +
                "\n\t}\n";
    }
    
    private String addOperation2() {
        return  "\n\tpublic boolean " + OPERATION_2 + "(java.util.List l) {" +
                "\n\t\treturn false;" +
                "\n\t}\n";
    }
    
    private String addOperation3_String() {
        return  "\n\tpublic Integer " + OPERATION_3 + "(String[] s, int t) {" +
                "\n\t\treturn new Integer(0);" +
                "\n\t}\n";
    }
    
    private String addOperation3_Object() {
        return  "\n\tpublic Integer " + OPERATION_3 + "(Object[] s, int t) {" +
                "\n\t\treturn new Integer(0);" +
                "\n\t}\n";
    }
    
    private String addGenericOperation1() {
        return "\n\tpublic Z " + GENERIC_OPERATION_1 + "(Q param1, String param2) {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addGenericOperation2() {
        return "\n\tpublic <T> T " + GENERIC_OPERATION_2 + "() {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    private String addGenericOperation3() {
        return "\n\tpublic String " + GENERIC_OPERATION_3 + "(String param1) {" +
                "\n\t\treturn null;" +
                "\n\t}\n";
    }
    
    //========================= Panel discovery ==================================//
    
    /**
     * Overloaded method to select/unselect wrapped operations
     * instead of creating new ones.
     */
    protected void addMBeanOperations(
            NewFileNameLocationStepOperator nfnlso,
            ArrayList<Operation> operList,
            String fileType) {
        
        JTableOperator jto = getTableOperator(WRAPPER_OPERATION_TABLE, nfnlso);
        
        System.out.println("Select/unselect and update MBeans operations");
        
        // Depending on the MBean, performs the following :
        // - check/uncheck operations to keep
        // - modify parameters
        // - modify exceptions
        // - modify description
        
        // Expose all operations
        if (mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_1) ||
                mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_4)) {
            
            // Add description on parameters for OPERATION_2 and OPERATION_3
            jto.editCellAt(jto.findCellRow(OPERATION_2),
                    jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME));
            updateMBeanOperationParameter(jto, 0, null, null, MBEAN_PARAMETER_DESCRIPTION_1);
            jto.editCellAt(jto.findCellRow(OPERATION_3),
                    jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME));
            updateMBeanOperationParameter(jto, 1, null, null, MBEAN_PARAMETER_DESCRIPTION_2);
            
            // Add description on exceptions for OPERATION_1
            jto.editCellAt(jto.findCellRow(OPERATION_1),
                    jto.findColumn(OPERATION_EXCEPTIONS_COLUMN_NAME));
            updateMBeanOperationException(jto, 0, null, MBEAN_EXCEPTION_DESCRIPTION_1);
        }
        // Expose minimal operations
        else if (mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_2) ||
                mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_5)) {
            
            // Unselect OPERATION_1, OPERATION_2 and OPERATION_3
            unselectOperation(jto, OPERATION_1);
            unselectOperation(jto, OPERATION_2);
            unselectOperation(jto, OPERATION_3);
        }
        // Expose generic operations
        else if(mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_3) ||
                mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_6)) {
            // Nothing to do
            // We just want to check that generation is successfull
        }
    }
    
    /**
     * Overloaded method to check selected/unselected wrapped operations
     */
    protected void checkMBeanOperationsWizard(
            NewFileNameLocationStepOperator nfnlso, String fileType) {
        
        JTableOperator jto = getTableOperator(WRAPPER_OPERATION_TABLE, nfnlso);
        
        System.out.println("Check selected/unselected MBeans operations");
        
        // Expose all operations
        if (mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_1) ||
                mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_4)) {
            
            verifyOperationSelection(jto, OPERATION_1, true);
            verifyOperationSelection(jto, OPERATION_2, true);
            verifyOperationSelection(jto, OPERATION_3, true);
        }
        // Expose minimal operations
        else if (mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_2) ||
                mbeanName.equals(OPERATION_WRAPPED_MBEAN_NAME_5)) {
            
            verifyOperationSelection(jto, OPERATION_1, false);
            verifyOperationSelection(jto, OPERATION_2, false);
            verifyOperationSelection(jto, OPERATION_3, false);
        }
        super.checkMBeanAttributesWizard(nfnlso, fileType);
    }
    
    
    protected void updateMBeanOperationParameter(
            JTableOperator jto, int rowIndex,
            String name, String type, String description) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        
        clickButton(OPERATION_ADD_PARAM_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(PARAMETER_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(PARAMETER_TABLE, ndo);
        
        clickButton(PARAMETER_ADD_BUTTON, ndo);
        waitNoEvent(5000);
        
        if (name != null) {
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_NAME_COLUMN_NAME), name);
        }
        if (type != null) {
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_TYPE_COLUMN_NAME), type);
        }
        if (description != null) {
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_DESCRIPTION_COLUMN_NAME), description);
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    protected void updateMBeanOperationException(
            JTableOperator jto, int rowIndex,
            String className, String description) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        
        clickButton(OPERATION_ADD_EXCEP_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(EXCEPTION_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(EXCEPTION_TABLE, ndo);
        
        clickButton(EXCEPTION_ADD_BUTTON, ndo);
        waitNoEvent(5000);
        
        if (className != null) {
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(EXCEPTION_CLASS_COLUMN_NAME), className);
        }
        if (description != null) {
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(EXCEPTION_DESCRIPTION_COLUMN_NAME), description);
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    
    private void unselectOperation(JTableOperator jto, String name) {
        int rowIndex  = jto.findCellRow(name);
        int columnIndex = jto.findColumn(OPERATION_INCLUDE_COLUMN_NAME);
        jto.selectCell(rowIndex, columnIndex);
    }
    
    private void verifyOperationSelection(
            JTableOperator jto, String name,  boolean selected) {
        int rowIndex  = jto.findCellRow(name);
        int columnIndex = jto.findColumn(OPERATION_INCLUDE_COLUMN_NAME);
        if (selected) {
            assertTrue((Boolean)getTableCellValue(jto, rowIndex, columnIndex));
        } else {
            assertFalse((Boolean)getTableCellValue(jto, rowIndex, columnIndex));
        }
    }
}
