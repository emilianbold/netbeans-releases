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

package org.netbeans.modules.jmx.test.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.modules.jmx.test.helpers.Attribute;
import org.netbeans.modules.jmx.test.helpers.JMXTestCase;
import org.netbeans.modules.jmx.test.helpers.Operation;
import static org.netbeans.modules.jmx.test.helpers.JellyConstants.*;

/**
 * Starting class for actions tests.
 */
public class ActionsTestCase extends JMXTestCase {
    
    protected String popupPath = null;
    
    // Java file class names
    protected static String SIMPLE_1 = "Simple1";
    protected static String SIMPLE_2 = "Simple2";
    protected static String SIMPLE_2_MBEAN = "Simple2MBean";
    protected static String SIMPLE_3 = "Simple3";
    protected static String SIMPLE_4 = "Simple4";
    protected static String SIMPLE_5 = "Simple5";
    protected static String SIMPLE_5_INTF = "Simple5Intf";
    protected static String STANDARD_1 = "Standard1";
    protected static String STANDARD_1_MBEAN = "Standard1MBean";
    protected static String DYNAMIC_1 = "Dynamic1";
    protected static String DYNAMIC_2 = "Dynamic2";
    protected static String DYNAMIC_3 = "Dynamic3";
    protected static String DYNAMIC_4 = "Dynamic4";
    protected static String DYNAMIC_4_SUPPORT = "Dynamic4Support";
    protected static String ADD_ATTRIBUTES_1 = "AddAttributes1";
    protected static String ADD_ATTRIBUTES_1_MBEAN = "AddAttributes1MBean";
    protected static String ADD_ATTRIBUTES_1_SUPER = "AddAttributes1Super";
    protected static String ADD_OPERATIONS_1 = "AddOperations1";
    protected static String ADD_OPERATIONS_1_MBEAN = "AddOperations1MBean";
    protected static String ADD_OPERATIONS_1_SUPER = "AddOperations1Super";
    protected static String ADD_OPERATIONS_1_SUPER_MBEAN = "AddOperations1SuperMBean";
    protected static String USER_EXCEPTION = "UserException";
    protected static String USER_NOTIFICATION = "UserNotification";
    
    
    protected String packageName = null;
    
    /** Need to be defined because of JUnit */
    public ActionsTestCase(String name) {
        super(name);
        // Initialize dedicated package name
        packageName = "com.foo." + this.getClass().getSimpleName();
    }
    

    //========================= Initialization =================================//
    
    /**
     * Create java files, then update with specified golden file content.
     * The package of the golden file content is updated with the
     * specified packageName.
     * The java file name of the golden file content is updated with the
     * specified className.
     * For other updates, the properties parameter is used if needed.
     */
    protected void createJavaFile(
            String className,
            String packageName,
            String goldenFileName,
            Properties properties) {
        
        // New File wizard
        NewFileWizardOperator nfwo = newFileWizardFromMenu(
                PROJECT_NAME_ACTION_FUNCTIONAL,
                FILE_CATEGORY_JAVA_CLASSES,
                FILE_TYPE_JAVA_CLASS);
        nfwo.next();
        // Name and Location wizard
        NewFileNameLocationStepOperator nfnlso = nameAndLocationWizard(
                className, packageName);
        nfnlso.finish();
        
        String content = getFileContent(getGoldenFile(goldenFileName));
        // Update golden file content package
        content = content.replaceAll(PACKAGE_COM_FOO_BAR, packageName);
        // Update golden file content name
        if (!className.equals(goldenFileName)) {
            content = content.replaceAll(goldenFileName, className);
        }
        // Update golden file content with specified properties
        Set keys = properties.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String key = (String)it.next();
            String value = properties.getProperty(key);
            content = content.replaceAll(key, value);
        }
        // Update created Java file with updated golden file content
        selectNode(PROJECT_NAME_ACTION_FUNCTIONAL + "|" + SOURCE_PACKAGES + "|" +
                packageName + "|" + className);
        EditorOperator eo = new EditorOperator(className);
        eo.replace(eo.getText(), content);
        eo.save();
    }
    
    /**
     * Create java files, then update with specified golden file content.
     * Use packageName attribute value.
     */
    protected void createJavaFile(
            String className,
            String goldenFileName) {
        createJavaFile(className, packageName, goldenFileName, new Properties());
    }
    
    protected void createJavaFile(
            String className,
            String goldenFileName,
            Properties properties) {
        createJavaFile(className, packageName, goldenFileName, properties);
    }
    
    /**
     * Create java files, then update with specified golden file content.
     * Use packageName attribute value and same name
     * for both created java file and golden file.
     */
    protected void createJavaFile(String className) {
        createJavaFile(className, packageName, className, new Properties());
    }
    
    //==================MBean execution wizard methods ======================//
    
    /**
     * Add MBean attributes
     * The MBean attributes are added to the existing attribute list
     */
    protected void addMBeanAttributes(
            NbDialogOperator ndo,
            JTableOperator jto,
            ArrayList<Attribute> attrList) {
        
        super.addMBeanAttributes(ndo, jto, ATTRIBUTE_ACCESS_BOX, attrList);
    }
    
    /**
     * Add MBean operations
     * The MBean operations are added to the existing operation list
     */
    protected void addMBeanOperations(
            NbDialogOperator ndo,
            JTableOperator jto,
            ArrayList<Operation> opList) {
        
        super.addMBeanOperations(ndo, jto, OPERATION_ADD_BUTTON_FROM_ACTION, opList);
    }
}
