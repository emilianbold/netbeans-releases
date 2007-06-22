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

package org.netbeans.modules.jmx.test.helpers;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.drivers.tables.JTableMouseDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Starting class for JMX plugin tests.
 */
public abstract class JMXTestCase extends JellyTestCase {
    
    /** Creates a new instance of JMXTestCase */
    public JMXTestCase(String name) {
        super(name);
    }
    
    // ==================================================================
    // GENERAL
    // ==================================================================
    
    /**
     * Select the specified node.
     */
    public Node selectNode(String nodeName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        Node node = new Node(pto.tree(), nodeName);
        node.select();
        return node;
    }
    
    // ==================================================================
    // PROJECT
    // ==================================================================
    
    /**
     * Create a new project.
     */
    public NewProjectWizardOperator newProject(
            String category,
            String type,
            String name) {
        NewProjectWizardOperator project = NewProjectWizardOperator.invoke();
        project.selectCategory(category);
        project.selectProject(type);
        project.next();
        NewFileNameLocationStepOperator projectName =
                new NewFileNameLocationStepOperator();
        projectName.setObjectName(name);
        project.finish();
        setWaitFocusTimeout(project, 10000);
        return project;
    }
    
    /**
     * Compile the specified project.
     */
    public void compileProject(String name) {
        ProjectRootNode prn = (ProjectRootNode)selectNode(name);
        prn.buildProject();
    }
    
    /**
     * Test the specified project.
     */
    public void testProject(String name) {
        ProjectRootNode prn = (ProjectRootNode)selectNode(name);
        prn.performPopupAction("Test Project");
    }
    
    // ==================================================================
    // FILE
    // ==================================================================
    
    /**
     * New File wizard from menu.
     */
    public NewFileWizardOperator newFileWizardFromMenu(
            String projectName,
            String category,
            String type) {
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        if (projectName != null) {
            nfwo.selectProject(projectName);
        }
        nfwo.selectCategory(category);
        nfwo.selectFileType(type);
        setWaitFocusTimeout(nfwo, 10000);
        return nfwo;
    }
    
    /**
     * New File wizard from node.
     */
    public NewFileWizardOperator newFileWizardFromNode(
            String nodeName,
            String category,
            String type) {
        Node prn = null;
        if (nodeName != null) {
            prn = selectNode(nodeName);
        }
        // WARNING : do not call NewFileWizardOperator.invoke(prn, category, type)
        // Indeed, while NewFileWizardOperator.invoke() method just invoke the
        // New File wizard, this method performs an unwanted next action at the end.
        new NewFileAction().perform(prn);
        NewFileWizardOperator nfwo = new NewFileWizardOperator();
        nfwo.selectCategory(category);
        nfwo.selectFileType(type);
        setWaitFocusTimeout(nfwo, 10000);
        return nfwo;
    }
    
    public NewFileNameLocationStepOperator nameAndLocationWizard(
            String objectName,
            String packageName,
            String description,
            String classToWrap,
            boolean objectWrappedAsMXBean) {
        NewFileNameLocationStepOperator wizard =
                new NewFileNameLocationStepOperator();
        
        if (objectName != null) {
            wizard.setObjectName(objectName);
        }
        if (packageName != null) {
            wizard.selectSourcePackagesLocation();
            wizard.setPackage(packageName);
        }
        if (description != null) {
            setTextFieldContent(MBEAN_DESCRIPTION_TEXT_FIELD,
                    wizard, description);
        }
        if (classToWrap != null) {
            setTextFieldContent(CLASS_TO_WRAP_TEXT_FIELD,
                    wizard, classToWrap);
        }
        if (objectWrappedAsMXBean == true) {
            setCheckBoxSelection(OBJECT_WRAPPED_AS_MXBEAN_CHECK_BOX,
                    wizard, true);
        }
        setWaitFocusTimeout(wizard, 10000);
        return wizard;
    }
    
    /**
     * Name and Location wizard.
     */
    public NewFileNameLocationStepOperator nameAndLocationWizard(
            String objectName,
            String packageName) {
        return nameAndLocationWizard(objectName, packageName, null, null, false);
    }
    
    // ==================================================================
    // MBEAN ATTRIBUTES, OPERATIONS AND NOTIFICATIONS
    // ==================================================================
    
    /**
     * Add new attributes
     * The MBean attributes are added to the existing attribute list
     * Need to specify the access box component name because
     * the value differs between simple and wrapped attribute
     */
    public void addMBeanAttributes(
            NbDialogOperator ndo,
            JTableOperator jto,
            String accessBox,
            ArrayList<Attribute> attrList) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = jto.getRowCount();
        
        if (attrList != null) {
            for (Attribute attribute : attrList) {
                
                clickButton(ATTRIBUTE_ADD_BUTTON, ndo);
                waitNoEvent(5000);
                
                // Non default attribute name
                if (attribute.getName() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(ATTRIBUTE_NAME_COLUMN_NAME),
                            attribute.getName());
                }
                // Non default attribute type
                if (attribute.getType() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(ATTRIBUTE_TYPE_COLUMN_NAME),
                            attribute.getType());
                }
                // Non default attribute access
                if (attribute.getAccess() != null) {
                    jtmd.selectCell(jto, rowIndex,
                            jto.findColumn(ATTRIBUTE_ACCESS_COLUMN_NAME));
                    selectComboBoxItem(accessBox, jto, attribute.getAccess());
                }
                // Non default attribute description
                if (attribute.getDescription() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(ATTRIBUTE_DESCRIPTION_COLUMN_NAME),
                            attribute.getDescription());
                }
                
                rowIndex++;
            }
        }
    }
    
    /**
     * Add new operations
     * The MBean operations are added to the existing operation list
     * Need to specify the operation add button component name because
     * the value differs between wizard created from menu and wizard
     * created from action
     */
    public void addMBeanOperations(
            NbDialogOperator ndo,
            JTableOperator jto,
            String addButton,
            ArrayList<Operation> opList) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = jto.getRowCount();
        
        if (opList != null) {
            for (Operation operation : opList) {
                
                clickButton(addButton, ndo);
                waitNoEvent(5000);
                
                // Non default operation name
                if (operation.getName() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(OPERATION_NAME_COLUMN_NAME),
                            operation.getName());
                }
                // Non default operation return type
                if (operation.getReturnType() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(OPERATION_RETURN_TYPE_COLUMN_NAME),
                            operation.getReturnType());
                }
                if (operation.getParameters() != null) {
                    jto.editCellAt(rowIndex,
                            jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME));
                    addMBeanOperationParameters(jto, operation);
                }
                if (operation.getExceptions() != null) {
                    jto.editCellAt(rowIndex,
                            jto.findColumn(OPERATION_EXCEPTIONS_COLUMN_NAME));
                    addMBeanOperationExceptions(jto, operation);
                }
                // Non default operation description
                if (operation.getDescription() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(OPERATION_DESCRIPTION_COLUMN_NAME),
                            operation.getDescription());
                }
                
                rowIndex++;
            }
        }
    }
    
    /**
     * Add new operation parameters
     */
    public void addMBeanOperationParameters(
            JTableOperator jto,
            Operation operation) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = 0;
        
        clickButton(OPERATION_ADD_PARAM_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(PARAMETER_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(PARAMETER_TABLE, ndo);
        
        for (Parameter parameter : operation.getParameters()) {
            
            clickButton(PARAMETER_ADD_BUTTON, ndo);
            waitNoEvent(5000);
            
            // Non default parameter name
            if (parameter.getName() != null) {
                jtmd.editCell(jto2, rowIndex,
                        jto2.findColumn(PARAMETER_NAME_COLUMN_NAME),
                        parameter.getName());
            }
            // Non default parameter type
            if (parameter.getType() != null) {
                jtmd.editCell(jto2, rowIndex,
                        jto2.findColumn(PARAMETER_TYPE_COLUMN_NAME),
                        parameter.getType());
            }
            // Non default parameter description
            if (parameter.getDescription() != null) {
                jtmd.editCell(jto2, rowIndex,
                        jto2.findColumn(PARAMETER_DESCRIPTION_COLUMN_NAME),
                        parameter.getDescription());
            }
            rowIndex++;
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    /**
     * Add new operation exceptions
     */
    public void addMBeanOperationExceptions(
            JTableOperator jto,
            Operation operation) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = 0;
        
        clickButton(OPERATION_ADD_EXCEP_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(EXCEPTION_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(EXCEPTION_TABLE, ndo);
        
        for (Exception exception : operation.getExceptions()) {
            
            clickButton(EXCEPTION_ADD_BUTTON, ndo);
            waitNoEvent(5000);
            
            // Non default exception class name
            if (exception.getClassName() != null) {
                jtmd.editCell(jto2, rowIndex,
                        jto2.findColumn(EXCEPTION_CLASS_COLUMN_NAME),
                        exception.getClassName());
            }
            // Non default exception description
            if (exception.getDescription() != null) {
                jtmd.editCell(jto2, rowIndex,
                        jto2.findColumn(EXCEPTION_DESCRIPTION_COLUMN_NAME),
                        exception.getDescription());
            }
            rowIndex++;
        }
        // Close the popup
        //clickButton(CLOSE_JBUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    /**
     * Add new notifications
     * The MBean notifications are added to the existing notification list
     */
    public void addMBeanNotifications(
            NbDialogOperator ndo,
            JTableOperator jto,
            ArrayList<Notification> notifList) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = jto.getRowCount();
        
        if (notifList != null) {
            for (Notification notification : notifList) {
                
                clickButton(NOTIFICATION_ADD_BUTTON, ndo);
                waitNoEvent(5000);

                // Non default notification class name
                if (notification.getClassName() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(NOTIFICATION_CLASS_COLUMN_NAME),
                            notification.getClassName());
                }
                // Non default notification description
                if (notification.getDescription() != null) {
                    jtmd.editCell(jto, rowIndex,
                            jto.findColumn(NOTIFICATION_DESCRIPTION_COLUMN_NAME),
                            notification.getDescription());
                }
                if (notification.getTypes() != null) {
                    jto.editCellAt(rowIndex,
                            jto.findColumn(NOTIFICATION_TYPE_COLUMN_NAME));
                    addMBeanNotificationTypes(jto, notification);
                }
                
                rowIndex++;
            }
        }
    }
    
    /**
     * Add new notifications types
     */
    public void addMBeanNotificationTypes(
            JTableOperator jto,
            Notification notification) {
        
        JTableMouseDriver jtmd = new JTableMouseDriver();
        int rowIndex = 0;
        
        clickButton(NOTIFICATION_ADD_TYPE_BUTTON, jto);
        waitNoEvent(5000);
        
        NbDialogOperator ndo = new NbDialogOperator(TYPE_DIALOG_TITLE);
        JTableOperator jto2 = getTableOperator(TYPE_TABLE, ndo);
        
        for (NotificationType notifType : notification.getTypes()) {
            
            clickButton(TYPE_ADD_BUTTON, ndo);
            waitNoEvent(5000);
            
            // Non default type
            if (notifType.getType() != null) {
                jtmd.editCell(jto2, rowIndex, 0, notifType.getType());
            }
            rowIndex++;
        }
        // Close the popup
        //clickButton(TYPE_CLOSE_BUTTON, ndo);
        ndo.ok();
        waitNoEvent(5000);
    }
    
    /**
     * Check attributes values
     */
    public void checkMBeanAttributes(
            JTableOperator jto, ArrayList<Attribute> attrList) {
        
        // Empty list
        if (attrList == null || attrList.isEmpty()) {
            assertTrue(jto.getRowCount() == 0);
        }
        // Not empty list
        else {
            assertTrue(jto.getRowCount() == attrList.size());
            for (Attribute attribute : attrList) {
                String name = attribute.getName();
                int rowIndex = jto.findCellRow(name);
                assertEquals(attribute.getName(), jto.getValueAt(
                        rowIndex, jto.findColumn(ATTRIBUTE_NAME_COLUMN_NAME)));
                assertEquals(attribute.getType(), jto.getValueAt(
                        rowIndex, jto.findColumn(ATTRIBUTE_TYPE_COLUMN_NAME)));
                assertEquals(attribute.getAccess(), jto.getValueAt(
                        rowIndex, jto.findColumn(ATTRIBUTE_ACCESS_COLUMN_NAME)));
                assertEquals(attribute.getDescription(), jto.getValueAt(
                        rowIndex, jto.findColumn(ATTRIBUTE_DESCRIPTION_COLUMN_NAME)));
            }
        }
    }
    
    /**
     * Check operations values
     */
    public void checkMBeanOperations(
            JTableOperator jto, boolean isEditable, ArrayList<Operation> opList) {
        
        // Empty list
        if (opList == null || opList.isEmpty()) {
            assertTrue(jto.getRowCount() == 0);
        }
        // Not empty list
        else {
            assertTrue(jto.getRowCount() == opList.size());
            for (Operation operation : opList) {
                String name = operation.getName();
                int rowIndex = jto.findCellRow(name);
                assertEquals(operation.getName(), jto.getValueAt(
                        rowIndex, jto.findColumn(OPERATION_NAME_COLUMN_NAME)));
                assertEquals(operation.getReturnType(), jto.getValueAt(
                        rowIndex, jto.findColumn(OPERATION_RETURN_TYPE_COLUMN_NAME)));
                assertEquals(operation.getDescription(), jto.getValueAt(
                        rowIndex, jto.findColumn(OPERATION_DESCRIPTION_COLUMN_NAME)));
                checkMBeanOperationParameters(jto, isEditable, operation);
                checkMBeanOperationExceptions(jto, isEditable, operation);
            }
        }
    }
    
    /**
     * Check parameters values
     * If the parameters field is not editable,
     * simply check the parameters text field content
     * Otherwise, edit the parameters field and check the parameters table
     */
    public void checkMBeanOperationParameters(
            JTableOperator jto, boolean isEditable, Operation operation) {
        
        ArrayList<Parameter> paramList = operation.getParameters();
        int rowIndex = jto.findCellRow(operation.getName());
        int columnIndex = jto.findColumn(OPERATION_PARAMETERS_COLUMN_NAME);
        
        // Parameters field is editable
        if (isEditable) {
            // Open parameters dialog operator
            jto.editCellAt(rowIndex, columnIndex);
            clickButton(OPERATION_ADD_PARAM_BUTTON, jto);
            waitNoEvent(5000);
            
            NbDialogOperator ndo = new NbDialogOperator(PARAMETER_DIALOG_TITLE);
            JTableOperator jto2 = getTableOperator(PARAMETER_TABLE, ndo);
            
            // Empty list
            if (paramList == null || paramList.isEmpty()) {
                assertTrue(jto2.getRowCount() == 0);
            }
            // Not empty list
            else {
                assertTrue(jto2.getRowCount() == paramList.size());
                rowIndex = 0;
                for (Parameter p : paramList) {
                    assertEquals(p.getName(), jto2.getValueAt(
                            rowIndex, jto2.findColumn(PARAMETER_NAME_COLUMN_NAME)));
                    assertEquals(p.getType(), jto2.getValueAt(
                            rowIndex, jto2.findColumn(PARAMETER_TYPE_COLUMN_NAME)));
                    assertEquals(p.getDescription(), jto2.getValueAt(
                            rowIndex, jto2.findColumn(PARAMETER_DESCRIPTION_COLUMN_NAME)));
                    rowIndex++;
                }
            }
            
            // Close the popup
            //clickButton(CLOSE_JBUTTON, ndo);
            ndo.ok();
            waitNoEvent(5000);
        }
        // Parameters field is not editable
        else {
            String parameters = "";
            // Not empty list
            if (paramList != null && !paramList.isEmpty()) {
                int count = 1;
                for (Parameter p : paramList) {
                    parameters += p.getType() + " " + p.getName();
                    if (count < paramList.size()) {
                        parameters += ",";
                        count++;
                    }
                }
            }
            JTextField jtf = getTableCellTextField(jto, rowIndex, columnIndex);
            assertEquals(parameters, jtf.getText());
        }
    }
    
    /**
     * Check exceptions values
     * If the exceptions field is not editable,
     * simply check the exceptions text field content
     * Otherwise, edit the exceptions field and check the exceptions table
     */
    public void checkMBeanOperationExceptions(
            JTableOperator jto, boolean isEditable, Operation operation) {
        
        ArrayList<Exception> exceptionList = operation.getExceptions();
        int rowIndex = jto.findCellRow(operation.getName());
        int columnIndex = jto.findColumn(OPERATION_EXCEPTIONS_COLUMN_NAME);
        
        // Exceptions field is editable
        if (isEditable) {
            // Open parameters dialog operator
            jto.editCellAt(rowIndex, columnIndex);
            clickButton(OPERATION_ADD_EXCEP_BUTTON, jto);
            waitNoEvent(5000);
            
            NbDialogOperator ndo = new NbDialogOperator(EXCEPTION_DIALOG_TITLE);
            JTableOperator jto2 = getTableOperator(EXCEPTION_TABLE, ndo);
            
            // Empty list
            if (exceptionList == null || exceptionList.isEmpty()) {
                assertTrue(jto2.getRowCount() == 0);
            }
            // Not empty list
            else {
                assertTrue(jto2.getRowCount() == exceptionList.size());
                rowIndex = 0;
                for (Exception e : exceptionList) {
                    assertEquals(e.getClassName(), jto2.getValueAt(
                            rowIndex, jto2.findColumn(EXCEPTION_CLASS_COLUMN_NAME)));
                    assertEquals(e.getDescription(), jto2.getValueAt(
                            rowIndex, jto2.findColumn(EXCEPTION_DESCRIPTION_COLUMN_NAME)));
                    rowIndex++;
                }
            }
            
            // Close the popup
            //clickButton(CLOSE_JBUTTON, ndo);
            ndo.ok();
            waitNoEvent(5000);
        }
        // Exceptions field is not editable
        else {
            String exceptions = "";
            // Not empty list
            if (exceptionList != null && !exceptionList.isEmpty()) {
                int count = 1;
                for (Exception e : exceptionList) {
                    exceptions += e.getClassName();
                    if (count < exceptionList.size()) {
                        exceptions += ",";
                        count++;
                    }
                }
            }
            JTextField jtf = getTableCellTextField(jto, rowIndex, columnIndex);
            assertEquals(exceptions, jtf.getText());
        }
    }
    
    // ==================================================================
    // TEXT FIELD OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a text field component, places an operator on
     * it and finally returns it
     * @param componentName the name of the text field to search for
     * @param co the Container Operator
     * @return JTextFieldOperator a jemmy operator on that text field
     */
    public JTextFieldOperator getTextFieldOperator(
            String componentName, ContainerOperator co) {
        JTextField jtf = (JTextField)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JTextFieldOperator(jtf);
    }
    
    public String getTextFieldContent(String component, ContainerOperator co) {
        JTextFieldOperator jtfo = getTextFieldOperator(component, co);
        return jtfo.getText();
    }
    
    public void setTextFieldContent(
            String component, ContainerOperator co, String text) {
        JTextFieldOperator jtfo = getTextFieldOperator(component, co);
        jtfo.clearText();
        waitNoEvent(5000);
        jtfo.typeText(text);
    }
    
    // ==================================================================
    // CHECK BOX OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a check box component, places an operator on
     * it and finally returns it
     * @param componentName the name of the check box to search for
     * @param co the Container Operator
     * @return JCheckBoxOperator a jemmy operator on that check box
     */
    public JCheckBoxOperator getCheckBoxOperator(
            String componentName, ContainerOperator co) {
        JCheckBox jcb = (JCheckBox)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JCheckBoxOperator(jcb);
    }
    
    public void setCheckBoxSelection(
            String componentName, ContainerOperator co, boolean newValue) {
        JCheckBoxOperator cbo = getCheckBoxOperator(componentName, co);
        cbo.changeSelection(newValue);
    }
    
    // ==================================================================
    // COMBO BOX OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a combo box component, places an operator on
     * it and finally returns it
     * @param componentName the name of the combo box to search for
     * @param co the Container Operator
     * @return JCheckBoxOperator a jemmy operator on that combo box
     */
    public JComboBoxOperator getComboBoxOperator(
            String componentName,  ContainerOperator co) {
        JComboBox jcb = (JComboBox)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JComboBoxOperator(jcb);
    }
    
    public String getComboBoxItem(String componentName,  ContainerOperator co) {
        JComboBoxOperator jcb = getComboBoxOperator(componentName, co);
        return (String)jcb.getSelectedItem();
    }
    
    /**
     * To be used when the combo box contains a list of items but
     * is also editable
     */
    public void setComboBoxItem(
            String componentName, ContainerOperator co, String newItem) {
        JComboBoxOperator jcbo = getComboBoxOperator(componentName, co);
        jcbo.setSelectedItem(newItem);
    }
    
    /**
     * To be used when the combo box contains a list of items but
     * is not editable
     */
    public void selectComboBoxItem(
            String componentName, ContainerOperator co, String newItem) {
        JComboBoxOperator jcbo = getComboBoxOperator(componentName, co);
        jcbo.selectItem(newItem);
    }
    
    // ==================================================================
    // BUTTON OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a button component, places an operator on
     * it and finally returns it
     * @param componentName the name of the button to search for
     * @param co the Container Operator
     * @return JButtonOperator a jemmy operator on that button
     */
    public JButtonOperator getButtonOperator(
            String componentName, ContainerOperator co) {
        JButton jb = (JButton)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JButtonOperator(jb);
    }
    
    public void clickButton(String componentName, ContainerOperator co) {
        JButtonOperator jbo = getButtonOperator(componentName, co);
        jbo.press();
        jbo.release();
    }
    
    //    public void clickButton(String componentName, ContainerOperator co) {
    //        JButtonOperator jbo = getButtonOperator(componentName, co);
    //        if (co instanceof JTableOperator) {
    //            jbo.press();
    //            jbo.release();
    //        } else {
    //            jbo.clickMouse();
    //        }
    //    }
    
    // ==================================================================
    // RADIO BUTTON OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a button component, places an operator on
     * it and finally returns it
     * @param componentName the name of the button to search for
     * @param co the Container Operator
     * @return JRadioButtonOperator a jemmy operator on that button
     */
    public JRadioButtonOperator getRadioButtonOperator(
            String componentName, ContainerOperator co) {
        JRadioButton jrb = (JRadioButton)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JRadioButtonOperator(jrb);
    }
    
    public void setRadioButtonSelection(
            String component, ContainerOperator co, boolean newValue) {
        JRadioButtonOperator jrb = getRadioButtonOperator(component, co);
        jrb.changeSelection(newValue);
    }
    
    // ==================================================================
    // TABLE OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a table component, places an operator on
     * it and finally returns it
     * @param componentName the name of the JTable to search for
     * @param co the Container Operator
     * @return JButtonOperator a jemmy operator on that JTable
     */
    public JTableOperator getTableOperator(
            String componentName, ContainerOperator co) {
        JTable table = (JTable)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JTableOperator(table);
    }
    
    public void selectTableCell(
            String componentName, ContainerOperator co,
            int rowIndex, int columnIndex) {
        JTableOperator jto = getTableOperator(componentName, co);
        jto.selectCell(rowIndex, columnIndex);
    }
    
    public Object getTableCellValue(
            JTableOperator jto, int rowIndex, int columnIndex) {
        return jto.getCellEditor(rowIndex, columnIndex).getCellEditorValue();
    }
    
    public JTextField getTableCellTextField(
            JTableOperator jto, int rowIndex, int columnIndex) {
        Container container = (Container)jto.getRenderedComponent(rowIndex, columnIndex);
        JTextField jtf = null;
        for (int i = 0; i < container.getComponentCount(); i ++) {
            Component component = container.getComponent(i);
            if (component instanceof JTextField)
                jtf = (JTextField)component;
        }
        return jtf;
    }
    
    // ==================================================================
    // LABEL OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a label component, places an operator on
     * it and finally returns it
     * @param componentName the name of the JTable to search for
     * @param co the Container Operator
     * @return JButtonOperator a jemmy operator on that JTable
     */
    public JLabelOperator getLabelOperator(
            String componentName, ContainerOperator co) {
        JLabel label = (JLabel)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JLabelOperator(label);
    }
    
    public JLabel getLabel(String text, Container co) {
        JLabel result = null;
        // Check for the specified component itself
        if (co instanceof JLabel && ((JLabel)co).getText().equals(text)) {
            // We have found the JLabel object with the specified label
            result = ((JLabel)co);
        }
        // Check for the sub components
        else {
            Component[] components = co.getComponents();
            for (Component component : components) {
                if ((result = getLabel(text, (Container)component)) != null) {
                    break;
                }
            }
        }
        return result;
    }
    
    // ==================================================================
    // TEXT AREA OPERATOR
    // ==================================================================
    
    /**
     * Method which searches for a text area component, places an operator on
     * it and finally returns it
     * @param componentName the name of the JTable to search for
     * @param co the Container Operator
     * @return JButtonOperator a jemmy operator on that JTable
     */
    public JTextAreaOperator getTextAreaOperator(
            String componentName, ContainerOperator co) {
        JTextArea jta = (JTextArea)co.findSubComponent(
                new NameComponentChooser(componentName));
        return new JTextAreaOperator(jta);
    }
    
    // ==================================================================
    // ACTIONS
    // ==================================================================
    
    /**
     * Select the specified menu item from the specified node.
     */
    public JMenuItemOperator showMenuItem(Node node, String path) {
        JPopupMenuOperator jpmo = node.callPopup();
        JMenuItemOperator jmio = jpmo.showMenuItem(path);
        return jmio;
    }
    
    /**
     * Select the specified menu item from the specified component operator.
     */
    public JMenuItemOperator showMenuItem(ComponentOperator co, String path) {
        co.clickForPopup();
        JPopupMenuOperator jpmo = new JPopupMenuOperator(co);
        JMenuItemOperator jmio = jpmo.showMenuItem(path);
        return jmio;
    }
    
    /**
     * Perform action on the specified node
     * Seems to not work as expected when performing some checks before
     * (for instance, when checking action.isEnabled(node))
     * Use showMenuItem(node).push() instead
     */
    protected void performAction(String menuPath, String popupPath, Node node) {
        new Action(menuPath, popupPath).perform(node);
    }
    
    /**
     * Perform action on the specified component operator
     * Seems to not work as expected when performing some checks before
     * (for instance, when checking action.isEnabled(co))
     * Use showMenuItem(co).push() instead
     */
    protected void performAction(String menuPath, String popupPath, ComponentOperator co) {
        new Action(menuPath, popupPath).perform(co);
    }
    
    // ==================================================================
    // OTHER UTILITIES
    // ==================================================================
    
    /**
     * Returns the specified golden file.
     * This golden file must be located under
     *   directory containing "this" java file/data
     */
    public File getGoldenFile(String filename) {
        return new File(getClass().getResource("data").getFile() +
                File.separator + filename);
    }
    
    /**
     * Returns the specified file content.
     */
    public String getFileContent(File file) {
        
        StringBuffer content = new StringBuffer();
        FileInputStream is = null;
        int ch;
        
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            fail("File " + file.toString() + " not found.");
        }
        try {
            while( (ch = is.read( )) != -1 ) {
                content.append((char)ch);
            }
        } catch (IOException e) {
            fail("Error while copy file content.");
        }
        return content.toString();
    }
    
    /**
     * Returns true if the 2 specified files are equals, false otherwise.
     * The properties parameter is used to updated the expected file
     * if needed.
     */
    public boolean compareFiles(
            File createdFile,
            File expectedFile,
            Properties properties) {
        
        // Check the created file exists
        assertTrue(createdFile.exists());
        
        String expectedFileContent = getFileContent(expectedFile);
        String createdFileContent = getFileContent(createdFile);
        
        if (compareFileContents(createdFileContent, expectedFileContent, properties)) {
            System.out.println("Created file \n\t" + createdFile +
                    "\nequals expected \n\t" + expectedFile);
            return true;
        } else {
            System.out.println("Created file \n\t" + createdFile +
                    "\ndiffers from expected \n\t" + expectedFile);
            return false;
        }
    }
    
    /**
     * Returns true if the 2 specified files are equals, false otherwise.
     * No properties parameter is needed.
     */
    public boolean compareFiles(File createdFile, File expectedFile) {
        return compareFiles(createdFile, expectedFile, new Properties());
    }
    
    /**
     * Returns true if the 2 specified contents are equals, false otherwise.
     * The properties parameter is used to updated the expected file content
     * if needed.
     */
    public boolean compareFileContents(
            String createdFileContent,
            String expectedFileContent,
            Properties properties) {
        
        // Update expected file content with specified properties
        Set keys = properties.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String key = (String)it.next();
            String value = properties.getProperty(key);
            expectedFileContent = expectedFileContent.replaceAll(key, value);
        }
        
        BufferedReader expectedBufferedReader = new BufferedReader(
                new StringReader(expectedFileContent));
        BufferedReader createdBufferedReader = new BufferedReader(
                new StringReader(createdFileContent));
        
        // Compare created file with expected file
        String createdLine = null, expectedLine = null;
        int lineNumber = 0;
        try {
            while (true) {
                lineNumber ++;
                expectedLine = expectedBufferedReader.readLine();
                createdLine = createdBufferedReader.readLine();
                // The end of expected stream has not been reached
                if (expectedLine != null) {
                    // Compare lines
                    if (!expectedLine.endsWith("<current Date and Time>") &&
                            !expectedLine.endsWith("<author>") &&
                            !expectedLine.equals(createdLine)) {
                        // If the difference concerns indentation,
                        // just display a warning
                        if (expectedLine.trim().equals(createdLine.trim())) {
                            // Indentation differences are meaningfull
                            // for non empty lines
                            if (!expectedLine.trim().equals("")) {
                                System.out.println("Created line " + lineNumber +
                                        " indentation differs from expected");
                                System.out.println("Created line :\n\t" + createdLine);
                                System.out.println("instead of expected :\n\t" + expectedLine);
                            }
                        } else {
                            System.out.println("Created line " + lineNumber +
                                    " differs from expected");
                            System.out.println("Created line :\n\t" + createdLine);
                            System.out.println("instead of expected :\n\t" + expectedLine);
                            return false;
                        }
                    }
                }
                // The end of expected stream has been reached
                // while the end of created stream has not been reached
                else if (createdLine != null) {
                    System.out.println("Created line " + lineNumber +
                            " differs from expected");
                    System.out.println("Created line :\n\t" + createdLine);
                    System.out.println("instead of expected :\n\t" + expectedLine);
                    return false;
                }
                // The end of both expected and created stream has been reached
                else {
                    return true;
                }
            }
        } catch (IOException e) {
            fail("Error while reading line.");
        }
        
        return true;
    }
    
    /**
     * Returns true if the 2 specified contents are equals, false otherwise.
     * No properties parameter is needed.
     */
    public boolean compareFileContents(
            String createdFileContent, String expectedFileContent) {
        return compareFileContents(createdFileContent, expectedFileContent, new Properties());
    }
    
    /**
     * See jellytools issue # 30423
     * Tests sometimes fails with message "Wait component has focus" :
     * 1) enlarge timeout
     * 2) wait a little before next action (calling waitNoEvent)
     */
    public void setWaitFocusTimeout(ComponentOperator co, long timeout) {
        co.getTimeouts().setTimeout("ComponentOperator.WaitFocusTimeout", timeout);
    }
    
    /**
     * See jellytools issue # 30423
     * Tests sometimes fails with message "Wait component has focus" :
     * 1) enlarge timeout
     * 2) wait a little before next action (calling waitNoEvent)
     *    It is recommended to wait at least 1000 ms
     */
    public void waitNoEvent(long timeout) {
        new EventTool().waitNoEvent(timeout);
    }

    public void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        }
        catch (InterruptedException e) {}
    }
}
