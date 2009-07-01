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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.jmx.test.helpers.MBean;
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
    
    // Depending on the MBean, the operations wizard execution will differ
    private MBean mbean = null;
    
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
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_1,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class exposing no operations
     */
    public void createWrappedMBean2() {
        
        System.out.println("============  createWrappedMBean2  ============");
        
        String description = "MBean from existing java class with minimal operations";
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_2,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class exposing generic operations
     */
    public void createWrappedMBean3() {
        
        System.out.println("============  createWrappedMBean3  ============");
        
        String description = "MBean from existing java class with generic operations";
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_3,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2,
                false, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean exposing all operations
     */
    public void createWrappedMBean4() {
        
        System.out.println("============  createWrappedMBean4  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with all operations";
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_4,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * exposing no operations
     */
    public void createWrappedMBean5() {
        
        System.out.println("============  createWrappedMBean5  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with minimal operations";
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_5,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    /**
     * MBean from existing java class wrapped as MXBean
     * exposing generic operations
     */
    public void createWrappedMBean6() {
        
        System.out.println("============  createWrappedMBean6  ============");
        
        String description = "MBean from existing java class wrapped as MXBean " +
                "with generic operations";
        mbean = new MBean(
                OPERATION_WRAPPED_MBEAN_NAME_6,
                FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS,
                PACKAGE_COM_FOO_BAR,
                description,
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2,
                true, null, null, null);
        wizardExecution(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS, mbean);
    }
    
    //========================= Panel discovery ==================================//
    
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
        
        // Operations wizard execution
        // ------------------------------------------
        nfnlso.next();
        nfnlso.next();
        // Check MBean operations wizard
        System.out.println("Check MBean operations wizard " +
                "before operations selection");
        checkMBeanOperationsWizardBeforeSelection(nfnlso, fileType);
        // Select/unselect MBean operations
        System.out.println("Select/unselect and update MBean operations");
        updateMBeanOperations(nfnlso);
        sleep(2000);
        // Check MBean operations wizard
        System.out.println("Check MBean operations wizard " +
                "after operations selection");
        checkMBeanOperationsWizardAfterSelection(nfnlso, fileType);

        sleep(2000);
        nfnlso.finish();
        
        // Check generated files
        System.out.println("Check created files");
        checkCreatedFiles(mbeanCreatedClassFile, mbeanCreatedInterfaceFile, mbean);
    }
    
    /**
     * Select/unselect and update wrapped operations.
     */
    private void updateMBeanOperations(
            NewJavaFileNameLocationStepOperator nfnlso) {
        
        JTableOperator jto = getTableOperator(WRAPPER_OPERATION_TABLE, nfnlso);
        // Give the focus to the table operator
        giveAPIFocus(jto);
        
        // Depending on the MBean, performs the following :
        // - check/uncheck operations to keep
        // - modify parameters
        // - modify exceptions
        // - modify description
        
        // Expose all operations
        if (mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_1) ||
                mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_4)) {
            
            // Add description on parameters for OPERATION_2
            jto.editCellAt(jto.findCellRow(OPERATION_2),
                    jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME));
            updateMBeanOperationParameter(jto, 0, null, null, MBEAN_PARAMETER_DESCRIPTION_2);
            
            // Add description on exceptions for OPERATION_1
            jto.editCellAt(jto.findCellRow(OPERATION_1),
                    jto.findColumn(OPERATION_EXCEPTIONS_COLUMN_NAME));
            updateMBeanOperationException(jto, 0, null, MBEAN_EXCEPTION_DESCRIPTION_1);
        }
        // Expose minimal operations
        else if (mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_2) ||
                mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_5)) {
            
            // Unselect OPERATION_1 and OPERATION_2
            unselectOperation(jto, OPERATION_1);
            unselectOperation(jto, OPERATION_2);
        }
        // Expose generic operations
        else if(mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_3) ||
                mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_6)) {
            // Nothing to do
            // We just want to check that generation is successfull
        }
    }
    
    /**
     * Check Operations wizard before MBean operations selection
     */
    private void checkMBeanOperationsWizardBeforeSelection(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {
 
        JTableOperator jto = getTableOperator(WRAPPER_OPERATION_TABLE, nfnlso);
        int rowCount = 0;

        System.out.println("Check all MBean operations are selected");

        // Wrapped java class is OPERATION_WRAPPER_NAME_1
        if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1)) {
            rowCount = jto.getRowCount();
            if (rowCount != 4) {
                System.out.println("(ERROR) Found " + rowCount + " operations" +
                        " instead of expected 4");
            }
            assertEquals(4, rowCount);
            verifyOperationSelection(jto, OPERATION_1, true);
            verifyOperationSelection(jto, OPERATION_2, true);
            // Cannot verify overloaded OPERATION_3 selection
            // As there is 2 operations defined for OPERATION_3 (using different parameters),
            // we need to retreive the row index using other value than the operation name
            // I've tried with parameters value ("java.lang.Object[] param0,int param1"
            // and "java.lang.String[] param0,int param1") but it does not work
        } 
        // Wrapped java class is OPERATION_WRAPPER_NAME_2
        else if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2)) {
            rowCount = jto.getRowCount();
            if (rowCount != 3) {
                System.out.println("(ERROR) Found " + rowCount + " operations" +
                        " instead of expected 3");
            }
            assertEquals(3, rowCount);
            verifyOperationSelection(jto, GENERIC_OPERATION_1, true);
            verifyOperationSelection(jto, GENERIC_OPERATION_2, true);
            verifyOperationSelection(jto, GENERIC_OPERATION_3, true);
        }
        super.checkMBeanOperationsWizard(nfnlso, fileType);
    }

    /**
     * Check Operations wizard after MBean operations selection
     */
    protected void checkMBeanOperationsWizardAfterSelection(
            NewJavaFileNameLocationStepOperator nfnlso, String fileType) {

        JTableOperator jto = getTableOperator(WRAPPER_OPERATION_TABLE, nfnlso);
        int rowCount = 0;

        System.out.println("Check MBean operations have been selected/unselected");

        // Wrapped java class is OPERATION_WRAPPER_NAME_1
        if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_1)) {
            rowCount = jto.getRowCount();
            if (rowCount != 4) {
                System.out.println("(ERROR) Found " + rowCount + " operations" +
                        " instead of expected 4");
            }
            assertEquals(4, rowCount);
            // Expose all operations
            if (mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_1) ||
                    mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_4)) {
                verifyOperationSelection(jto, OPERATION_1, true);
                verifyOperationSelection(jto, OPERATION_2, true);
                // Cannot verify overloaded OPERATION_3 selection
                // As there is 2 operations defined for OPERATION_3 (using different parameters),
                // we need to retreive the row index using other value than the operation name
                // I've tried with parameters value ("java.lang.Object[] param0,int param1"
                // and "java.lang.String[] param0,int param1") but it does not work
            } 
            // Expose minimal operations
            else if (mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_2) ||
                    mbean.getName().equals(OPERATION_WRAPPED_MBEAN_NAME_5)) {
                verifyOperationSelection(jto, OPERATION_1, false);
                verifyOperationSelection(jto, OPERATION_2, false);
                // Cannot verify overloaded OPERATION_3 selection
                // As there is 2 operations defined for OPERATION_3 (using different parameters),
                // we need to retreive the row index using other value than the operation name
                // I've tried with parameters value ("java.lang.Object[] param0,int param1"
                // and "java.lang.String[] param0,int param1") but it does not work
            }
        } 
        // Wrapped java class is OPERATION_WRAPPER_NAME_2
        else if (mbean.getClassToWrap().equals(
                PACKAGE_COM_FOO_BAR + "." + OPERATION_WRAPPER_NAME_2)) {
            rowCount = jto.getRowCount();
            if (rowCount != 3) {
                System.out.println("(ERROR) Found " + rowCount + " operations" +
                        " instead of expected 3");
            }
            assertEquals(3, rowCount);
            verifyOperationSelection(jto, GENERIC_OPERATION_1, true);
            verifyOperationSelection(jto, GENERIC_OPERATION_2, true);
            verifyOperationSelection(jto, GENERIC_OPERATION_3, true);
        }
        super.checkMBeanOperationsWizard(nfnlso, fileType);
    }
    
    private void updateMBeanOperationParameter(
            JTableOperator jto, int rowIndex,
            String name, String type, String description) {
        
        pressAndRelease(OPERATION_ADD_PARAM_BUTTON, jto);
        sleep(2000);
        JTableMouseDriver jtmd = new JTableMouseDriver();
        
        NbDialogOperator ndo = new NbDialogOperator(PARAMETER_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(PARAMETER_TABLE, ndo);  
            
        if (name != null) {
            jto2.clickForEdit(rowIndex, 
                    jto2.findColumn(PARAMETER_NAME_COLUMN_NAME));
            // Give the focus to the table operator
            giveAPIFocus(jto2);
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_NAME_COLUMN_NAME), name);
        }
        if (type != null) {
            jto2.clickForEdit(rowIndex, 
                    jto2.findColumn(PARAMETER_TYPE_COLUMN_NAME));
            // Give the focus to the table operator
            giveAPIFocus(jto2);
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_TYPE_COLUMN_NAME), type);
        }
        if (description != null) {
            jto2.clickForEdit(rowIndex,
                    jto2.findColumn(PARAMETER_DESCRIPTION_COLUMN_NAME));
            // Give the focus to the table operator
            giveAPIFocus(jto2);
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(PARAMETER_DESCRIPTION_COLUMN_NAME), description);
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        sleep(2000);
    }
    
    private void updateMBeanOperationException(
            JTableOperator jto, int rowIndex,
            String className, String description) {
        
        pressAndRelease(OPERATION_ADD_EXCEP_BUTTON, jto);
        sleep(2000);
        JTableMouseDriver jtmd = new JTableMouseDriver();
        
        NbDialogOperator ndo = new NbDialogOperator(EXCEPTION_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(EXCEPTION_TABLE, ndo);
        
        if (className != null) {
            jto2.clickForEdit(rowIndex, 
                    jto2.findColumn(EXCEPTION_CLASS_COLUMN_NAME));
            // Give the focus to the table operator
            giveAPIFocus(jto2);
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(EXCEPTION_CLASS_COLUMN_NAME), className);
        }
        if (description != null) {
            jto2.clickForEdit(rowIndex, 
                    jto2.findColumn(EXCEPTION_DESCRIPTION_COLUMN_NAME));
            // Give the focus to the table operator
            giveAPIFocus(jto2);
            jtmd.editCell(jto2, rowIndex,
                    jto2.findColumn(EXCEPTION_DESCRIPTION_COLUMN_NAME), description);
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        sleep(2000);
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
            //SL: constructor is not always generated by NetBeans,
            //SL: so rather set caret after the first "{"
            //SL: which is the beginning of class body.
            // eo.setCaretPosition("}", false);
            eo.setCaretPosition("{", false);
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
            //SL: constructor is not always generated by NetBeans,
            //SL: so rather set caret after the first "{"
            //SL: which is the beginning of class body.
            // eo.setCaretPosition("}", false);
            eo.setCaretPosition("{", false);
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
}
