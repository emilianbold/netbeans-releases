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
import org.netbeans.modules.jmx.test.helpers.Operation;
import org.netbeans.modules.jmx.test.helpers.Parameter;
import org.netbeans.modules.jmx.test.helpers.Exception;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Create JMX MBean files.
 * Check :
 * - wizards default values
 * - wizards behavior
 * - wizards robustness
 */
public class MBeanOperationsWizard extends MBeanWizardTestCase {
    
    // Depending on the MBean type,
    // the wizard component names differ
    private String tableOperator = null;
    private String removeButton = null;
    
    // When creating operations with default values, the default name is set to
    // OPERATION_DEFAULT_NAME + an operation creation counter
    // This counter is not reset when the operation list is cleared
    private int numOfCreatedOperations = 0;
    
    /** Need to be defined because of JUnit */
    public MBeanOperationsWizard(String name) {
        super(name);
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MBeanOperationsWizard("createMBean1"));
        suite.addTest(new MBeanOperationsWizard("createMBean2"));
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
        tableOperator = WRAPPER_OPERATION_TABLE;
        removeButton = WRAPPER_OPERATION_REMOVE_BUTTON;
        // Test operations wizard
        testOperationsWizard(FILE_TYPE_MBEAN_FROM_EXISTING_JAVA_CLASS);
    }
    
    /**
     * StandardMBean with metadata
     */
    public void createMBean2() {
        
        System.out.println("==========  createMBean2  ==========");

        // Initialize private variables
        tableOperator = OPERATION_TABLE_FROM_MENU;
        removeButton = OPERATION_REMOVE_BUTTON;
        // Test operations wizard
        testOperationsWizard(FILE_TYPE_STANDARD_MBEAN_WITH_METADATA);
    }
    
    //========================= Test Wizard ==================================//
    
    /**
     * Test operations wizard
     */
    private void testOperationsWizard(String fileType) {
        
        System.out.println("File type is " + fileType);
        
        ArrayList<Operation> opList = null;
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
        nfnlso.next();
        
        jto = getTableOperator(tableOperator, nfnlso);
        
        // Check operations wizard components
        checkMBeanOperationsWizardComponents(nfnlso, null, fileType);
        // Check operations wizard default values (empty operation list)
        checkMBeanOperations(jto, true, null);
        
        // Add default operations values
        opList = constructDefaultMBeanOperations();
        updateMBeanOperations(nfnlso, jto, opList, fileType);
        // Check attributes wizard components
        checkMBeanOperationsWizardComponents(nfnlso, opList, fileType);
        // Check attributes wizard default values
        checkMBeanOperations(jto, true, opList);
        
        // Add custom operations values
        // Perform back/next actions
        // Check no data has been lost
        opList = constructMBeanOperations();
        updateMBeanOperations(nfnlso, jto, opList, fileType);
        nfnlso.back();
        nfnlso.next();
        // Check operations wizard components
        checkMBeanOperationsWizardComponents(nfnlso, opList, fileType);
        // Check operations wizard values
        checkMBeanOperations(jto, true, opList);
        
        // Add an operation whose name starts with non capital letter
        // and contains forbidden characters
        // Check operation name updated
        Operation wrongOperation = new Operation("my.;Operation/,:", "int", "");
        opList = new ArrayList<Operation>();
        opList.add(wrongOperation);
        updateMBeanOperations(nfnlso, jto, opList, fileType);
        // Check operations wizard values
        Operation goodOperation = new Operation("myOperation", "int", "");
        opList = new ArrayList<Operation>();
        opList.add(goodOperation);
        checkMBeanOperations(jto, true, opList);
        
        // Reset to empty operation list
        updateMBeanOperations(nfnlso, jto, null, fileType);
        // Check operations wizard components
        checkMBeanOperationsWizardComponents(nfnlso, null, fileType);
        // Check operations wizard values
        checkMBeanOperations(jto, true, null);
        
        // Check warnings
        checkMBeanOperationsWizardWarnings(nfnlso, jto, fileType);
        
        nfnlso.cancel();
    }
    
    //========================= Check Wizard ==================================//
    
    /**
     * Check operations wizard components are enabled/disabled
     */
    private void checkMBeanOperationsWizardComponents(
            NewJavaFileNameLocationStepOperator nfnlso,
            ArrayList<Operation> opList,
            String fileType) {
        
        // Check operations Wizard title
        assertEquals("New " + fileType, nfnlso.getTitle());
        
        // Check table
        assertNotNull(getTableOperator(tableOperator, nfnlso));
        assertTrue(getTableOperator(tableOperator, nfnlso).isEnabled());
        
        // Check buttons
        assertTrue(getButtonOperator(OPERATION_ADD_BUTTON_FROM_MENU, nfnlso).isEnabled());
        if (opList == null || opList.isEmpty()) {
            assertFalse(getButtonOperator(removeButton, nfnlso).isEnabled());
        } else {
            assertTrue(getButtonOperator(removeButton, nfnlso).isEnabled());
        }
        assertTrue(nfnlso.btBack().isEnabled());
        assertFalse(nfnlso.btNext().isEnabled());
        assertTrue(nfnlso.btFinish().isEnabled());
        assertTrue(nfnlso.btCancel().isEnabled());
        assertTrue(nfnlso.btHelp().isEnabled());
        
        // Check parameters and exceptions
        if (opList != null) {
            JTableOperator jto = getTableOperator(tableOperator, nfnlso);
            for (Operation operation : opList) {
                checkMBeanOperationParametersWizardComponents(jto, operation);
                checkMBeanOperationExceptionsWizardComponents(jto, operation);
            }
        }
    }
    
    /**
     * Check parameters wizard components are enabled/disabled
     */
    private void checkMBeanOperationParametersWizardComponents(
            JTableOperator jto,
            Operation operation) {
        
        ArrayList<Parameter> paramList = operation.getParameters();
        
        // Open parameters dialog operator
        jto.editCellAt(jto.findCellRow(operation.getName()),
                jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME));
        pressAndRelease(OPERATION_ADD_PARAM_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(PARAMETER_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(PARAMETER_TABLE, ndo);
        
        // Check parameter Wizard title
        assertEquals(PARAMETER_DIALOG_TITLE, ndo.getTitle());
        
        // Check table
        assertNotNull(jto2);
        assertTrue(jto2.isEnabled());
        
        // Check buttons
        assertTrue(getButtonOperator(PARAMETER_ADD_BUTTON, ndo).isEnabled());
        if (paramList == null || paramList.isEmpty()) {
            assertFalse(getButtonOperator(PARAMETER_REMOVE_BUTTON, ndo).isEnabled());
        } else {
            assertTrue(getButtonOperator(PARAMETER_REMOVE_BUTTON, ndo).isEnabled());
        }
        assertTrue(ndo.btOK().isEnabled());
        assertTrue(ndo.btCancel().isEnabled());
        
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    /**
     * Check exceptions wizard components are enabled/disabled
     */
    private void checkMBeanOperationExceptionsWizardComponents(
            JTableOperator jto,
            Operation operation) {
        
        ArrayList<Exception> exceptionList = operation.getExceptions();
        
        // Open exceptions dialog operator
        jto.editCellAt(jto.findCellRow(operation.getName()),
                jto.findColumn(OPERATION_EXCEPTIONS_COLUMN_NAME));
        pressAndRelease(OPERATION_ADD_EXCEP_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(EXCEPTION_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(EXCEPTION_TABLE, ndo);
        
        // Check exception Wizard title
        assertEquals(EXCEPTION_DIALOG_TITLE, ndo.getTitle());
        
        // Check table
        assertNotNull(jto2);
        assertTrue(jto2.isEnabled());
        
        // Check buttons
        assertTrue(getButtonOperator(EXCEPTION_ADD_BUTTON, ndo).isEnabled());
        if (exceptionList == null || exceptionList.isEmpty()) {
            assertFalse(getButtonOperator(EXCEPTION_REMOVE_BUTTON, ndo).isEnabled());
        } else {
            assertTrue(getButtonOperator(EXCEPTION_REMOVE_BUTTON, ndo).isEnabled());
        }
        assertTrue(ndo.btOK().isEnabled());
        assertTrue(ndo.btCancel().isEnabled());
        
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    /**
     * Check operations wizard warnings :
     * - same operation name
     */
    private void checkMBeanOperationsWizardWarnings(
            NewJavaFileNameLocationStepOperator nfnlso,
            JTableOperator jto,
            String fileType) {
        
        // Add 2 operations with the same name
        Operation operation = new Operation("DuplicatedOperation", "int", "");
        ArrayList<Operation> opList = new ArrayList<Operation>();
        opList.add(operation);
        opList.add(operation);
        updateMBeanOperations(nfnlso, jto, opList, fileType);
        // Check warning message is displayed
        JLabel jl = getLabel(SAME_OPERATION_WARNING, nfnlso.getContentPane());
        assertNotNull(jl);
        // Check finish button is disabled
        assertFalse(nfnlso.btFinish().isEnabled());
        // Remove the first operation with the same name
        jto.selectCell(jto.findCellRow("DuplicatedOperation"),
                jto.findColumn(OPERATION_NAME_COLUMN_NAME));
        pressAndRelease(removeButton, nfnlso);
        waitNoEvent(5000);
        // Check warning message is not displayed anymore
        jl = getLabel(SAME_OPERATION_WARNING, nfnlso.getContentPane());
        assertNull(jl);
        // Check finish button is not disabled anymore
        assertTrue(nfnlso.btFinish().isEnabled());
    }
    
    /**
     * Update operations wizard values
     */
    private void updateMBeanOperations(
            NbDialogOperator ndo,
            JTableOperator jto,
            ArrayList<Operation> opList,
            String fileType) {
        
        // First reset operation table
        while (jto.getRowCount() != 0) {
            selectTableCell(tableOperator, ndo, 0, 0);
            pressAndRelease(removeButton, ndo);
            waitNoEvent(5000);
        }
        
        // Then add operations
        addMBeanOperations(ndo, opList, fileType);
        
        // Finally update the Operation objects with default values (when needed)
        // to use them at the checking values test step
        if (opList != null) {
            String opDefaultName = null;
            String paramDefaultName = null;
            for (Operation operation : opList) {
                
                opDefaultName = OPERATION_DEFAULT_NAME + numOfCreatedOperations;
                
                // Set default operation name
                if (operation.getName() == null) {
                    operation.setName(opDefaultName);
                }
                // Set default operation return type
                if (operation.getReturnType() == null) {
                    operation.setReturnType("void");
                }
                // Set default operation description
                if (operation.getDescription() == null) {
                    operation.setDescription(opDefaultName + " Description");
                }
                
                // Set default parameters values
                if (operation.getParameters() != null) {
                    int paramIndex = 0;
                    for (Parameter parameter : operation.getParameters()) {
                        
                        paramDefaultName = PARAMETER_DEFAULT_NAME + paramIndex;
                        
                        // Set default parameter name
                        if (parameter.getName() == null) {
                            parameter.setName(paramDefaultName);
                        }
                        // Set default parameter type
                        if (parameter.getType() == null) {
                            parameter.setType("java.lang.String");
                        }
                        // Set default parameter description
                        if (parameter.getDescription() == null) {
                            parameter.setDescription(paramDefaultName + " Description");
                        }
                        paramIndex++;
                    }
                }
                
                // Set default exceptions values
                if (operation.getExceptions() != null) {
                    for (Exception exception : operation.getExceptions()) {
                        
                        // Set default exception class name
                        if (exception.getClassName() == null) {
                            exception.setClassName(EXCEPTION_DEFAULT_CLASS);
                        }
                        // Set default exception description
                        if (exception.getDescription() == null) {
                            exception.setDescription("Exception Description");
                        }
                    }
                }
                numOfCreatedOperations++;
            }
        }
    }
    
    private ArrayList<Operation> constructDefaultMBeanOperations() {
        
        // Parameter construction
        Parameter parameter = new Parameter(null, null, null);
        
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(parameter);
        
        // Exception construction
        Exception exception = new Exception(null, null);
        ArrayList<Exception> exceptions = new ArrayList<Exception>();
        exceptions.add(exception);
        
        // Operation construction
        Operation operation1 = new Operation(null, null, null, null, null);
        Operation operation2 = new Operation(null, null, parameters, null, null);
        Operation operation3 = new Operation(null, null, null, exceptions, null);
        Operation operation4 = new Operation(null, null, parameters, exceptions, null);
        ArrayList<Operation> list = new ArrayList<Operation>();
        list.add(operation1);
        list.add(operation2);
        list.add(operation3);
        list.add(operation4);
        return list;
    }
    
    private ArrayList<Operation> constructMBeanOperations() {
        
        // Parameter construction
        Parameter parameter = new Parameter(
                MBEAN_PARAMETER_NAME_1,
                "java.lang.String",
                MBEAN_PARAMETER_DESCRIPTION_1);
        
        ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(parameter);
        
        // Exception construction
        Exception exception1 = new Exception(
                MBEAN_EXCEPTION_CLASS_1, MBEAN_EXCEPTION_DESCRIPTION_1);
        ArrayList<Exception> exceptions = new ArrayList<Exception>();
        exceptions.add(exception1);
        
        // Operation construction
        Operation operation1 = new Operation(
                MBEAN_OPERATION_NAME_1,
                "void", null, null,
                MBEAN_OPERATION_DESCRIPTION_1);
        Operation operation2 = new Operation(
                MBEAN_OPERATION_NAME_2,
                "int", parameters, null,
                MBEAN_OPERATION_DESCRIPTION_2);
        Operation operation3 = new Operation(
                MBEAN_OPERATION_NAME_3,
                "int", null, exceptions,
                MBEAN_OPERATION_DESCRIPTION_3);
        Operation operation4 = new Operation(
                MBEAN_OPERATION_NAME_4,
                "int", parameters, exceptions,
                MBEAN_OPERATION_DESCRIPTION_4);
        ArrayList<Operation> list = new ArrayList<Operation>();
        list.add(operation1);
        list.add(operation2);
        list.add(operation3);
        list.add(operation4);
        return list;
    }
}

